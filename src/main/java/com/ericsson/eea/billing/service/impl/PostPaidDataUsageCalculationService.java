package com.ericsson.eea.billing.service.impl;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingCycle;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.pcrf.model.DataUsageDetails;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.ericsson.eea.billing.util.BillingUtils.filterDataPassOnFUPChange;
import static com.ericsson.eea.billing.util.BillingUtils.getFilteredDataPassBasedOnBillCycle;

public class PostPaidDataUsageCalculationService implements DataUsageCalculationService {

    @Override
    public SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response) throws DatatypeConfigurationException {

        SubscriberInfo info = response.getMessage().getSubscriberInfo();
        System.out.println("MSISDN ID ==> " + info.getMsisdn());
        List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

        List<DataPass> filteredDataPasses = BillingUtils.getFilteredDataPass(dataPasses);

        // For Current Period
        System.out.println("*************************	Current Period	****************************\n");
        LocalDateTime billCycleEndDate = LocalDate.now().atStartOfDay();
        LocalDateTime billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> currentCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        System.out.println("Final Pass after FUP remove Current Cycle");
        currentCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails currentCycleDataUsage = printDataUsage(currentCycleDataPasses, info, BillingCycle.CURRENT, billCycleStartDate, billCycleEndDate);

        // For Previous Period
        System.out.println("*************************	Previous Period	****************************\n");
        billCycleEndDate = billCycleEndDate.minusMonths(1);
        billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> previousCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        System.out.println("Final Pass after FUP remove -- Previous Cycle");
        previousCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails previousCycleDataUsage = printDataUsage(previousCycleDataPasses, info, BillingCycle.PREVIOUS, billCycleStartDate, billCycleEndDate);

        // For Penultimate Period
        System.out.println("*************************	Penultimate Period	****************************\n");
        billCycleEndDate = billCycleEndDate.minusMonths(1);
        billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> penultimateCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        System.out.println("Final Pass after FUP remove -- Previous Cycle");
        penultimateCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails penultimateCycleDataUsage = printDataUsage(penultimateCycleDataPasses, info, BillingCycle.PENULTIMATE, billCycleStartDate, billCycleEndDate);

        SubscriberBillingInfo billingInfo = SubscriberBillingInfo.builder()
                // Basic
                .billingUpdateTime((long) info.getLastCheckedDate().getMillisecond())

                // Current Period
                .dataAvail(currentCycleDataUsage.getDataAvail()).dataUsed(currentCycleDataUsage.getDataUsed())
                .dataUsedShared(currentCycleDataUsage.getDataRemaining())

                // Previous Period
                .lbcDataAvail(previousCycleDataUsage.getDataAvail()).lbcDataUsed(previousCycleDataUsage.getDataUsed())
                .lbcDataUsedShared(previousCycleDataUsage.getDataRemaining())

                // Penultimate Period
                .pbcDataAvail(penultimateCycleDataUsage.getDataAvail())
                .pbcDataUsed(penultimateCycleDataUsage.getDataUsed())
                .pbcDataUsedShared(penultimateCycleDataUsage.getDataRemaining()).build();

        BillingUtils.printDataUsage(billingInfo);
        System.out.println(
                "\n++++++++++++++++++++++++++++++++ SubscriberBillingInfo ++++++++++++++++++++++++++++++++++++++++");
        System.out.println(billingInfo.toString());
        System.out.println(
                "++++++++++++++++++++++++++++++++ SubscriberBillingInfo ++++++++++++++++++++++++++++++++++++++++");

        return billingInfo;
    }

    private DataUsageDetails printDataUsage(List<DataPass> dataPasses, final SubscriberInfo info, BillingCycle billingCycle, LocalDateTime billingStartDate, LocalDateTime billingEndDate) {

        Double dataAvail = 0D;
        Double DataUsed = 0D;
        Double dataUsedShared = 0D;
        Double zeroRatedDataUsed = 0D;

        Double dataUsedUnlimited = 0D;
        Double dataUsedSharedGrp = 0D;
        Double dataUsedNonShared = 0D;
        Double dataRemaining = 0D;

        System.out.println("Calculation begin here=============");

        if (BillingUtils.isUnlimitedUsage(dataPasses)) {
            dataUsedUnlimited = dataPasses.stream()
                    .filter(e -> Arrays.asList("C", "E").contains(e.getInfoType()))
                    .filter(e -> e.getShareDetails() != null)
                    .map(e -> e.getShareDetails().getSharerDataUsage())
                    .flatMap(Collection::stream)
                    .filter(sha -> info.getMsisdn().equals(sha.getMsisdn()))
                    .mapToDouble(SharerDataUsage::getUsedVolume)
                    .sum();

            dataAvail = -1D;
            dataRemaining = -1D;
        } else {

            for (DataPass dataPass : dataPasses) {

                if (info.getTypeOfAccess().contains("L") && dataPass.getShareDetails() != null
                        && dataPass.getShareDetails().getSharerDataUsage().size() > 0) {

                    List<SharerDataUsage> usage = dataPass.getShareDetails().getSharerDataUsage();
                    Optional<SharerDataUsage> vol = usage.stream().filter(e -> e.getMsisdn().equals(info.getMsisdn()))
                            .findFirst();
                    dataUsedShared += vol.get().getUsedVolume();

                    if ("C".equals(dataPass.getInfoType())) {
                        dataUsedSharedGrp = dataUsedSharedGrp + dataPass.getFup() - dataPass.getVolume();
                        System.out.println("Its a Shaed Pass: Share Data \t" + vol.get().getUsedVolume()
                                + "\t| ShareDtaa Group " + (dataPass.getFup() - dataPass.getVolume()));
                    } else if ("E".equals(dataPass.getInfoType())) {
                        dataUsedSharedGrp += dataPass.getVolume();
                        System.out.println("Its a Shaed Pass: Share Data \t" + vol.get().getUsedVolume()
                                + "\t| ShareDtaa Group " + dataPass.getVolume());
                    }

                } else {

                    System.out.println(
                            "Its a Non-Shaed Pass: \t" + dataPass.getInfoType() + "\t|" + dataPass.getExpiryReason());
                    dataUsedNonShared += dataUsedNonShared;
                }

                dataAvail += dataPass.getFup();
            }
        }

        if (BillingCycle.CURRENT == billingCycle) {
            zeroRatedDataUsed = getZeroRatedUsage(dataPasses, "ZR");
        } else if (BillingCycle.PREVIOUS == billingCycle) {
            zeroRatedDataUsed = getZeroRatedUsage(dataPasses, "EZR");
        }

        dataUsedShared = (dataUsedUnlimited + dataUsedShared + dataUsedNonShared);
        dataRemaining = dataAvail - (dataUsedNonShared + dataUsedSharedGrp);
        System.out.println("*********************	Data Usage	********************");
        System.out.println("Data Usage Unlimited = " + dataUsedUnlimited);
        System.out.println("Total Data Used = " + dataUsedShared);
        System.out.println("Data Avail = " + dataAvail);
        System.out.println("Data ZeroRated = " + zeroRatedDataUsed);

        return DataUsageDetails.builder()
                .billingPeriodStartDate(billingStartDate.toEpochSecond(ZoneOffset.UTC))
                .billingPeriodEndDate(billingEndDate.toEpochSecond(ZoneOffset.UTC))
                .dataUsed(dataUsedShared)
                .dataAvail(dataAvail)
                .dataRemaining(dataRemaining)
                .zeroRatedDataUsed(zeroRatedDataUsed)
                .build();
    }

    private Double getZeroRatedUsage(List<DataPass> dataPasses, String infoType) {

        return dataPasses.stream()
                .filter(e -> infoType.equals(e.getInfoType()))
                .mapToDouble(DataPass::getVolume)
                .sum();
    }

}
