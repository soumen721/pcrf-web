package com.ericsson.eea.billing.service.impl;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.pcrf.model.DataUsageDetails;

import javax.xml.datatype.DatatypeConfigurationException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        DataUsageDetails currentCycleDataUsage = printDataUsage(currentCycleDataPasses, info);

        // For Previous Period
        System.out.println("*************************	Previous Period	****************************\n");
        billCycleEndDate = billCycleEndDate.minusMonths(1);
        billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> previousCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        System.out.println("Final Pass after FUP remove -- Previous Cycle");
        previousCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails previousCycleDataUsage = printDataUsage(previousCycleDataPasses, info);

        // For Penultimate Period
        System.out.println("*************************	Penultimate Period	****************************\n");
        billCycleEndDate = billCycleEndDate.minusMonths(1);
        billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> penultimateCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        System.out.println("Final Pass after FUP remove -- Previous Cycle");
        penultimateCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails penultimateCycleDataUsage = printDataUsage(penultimateCycleDataPasses, info);

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

    private DataUsageDetails printDataUsage(List<DataPass> dataPasses, final SubscriberInfo info) {

        long dataUsed_ult = 0L;
        long dataUsed_sha = 0L;
        long dataUsed_shaGrp = 0L;
        long dataUsed_nonSha = 0L;
        long userData = 0L;
        long data_avl = 0L;
        long data_rem = 0L;

        System.out.println("Calculation begin here");
        for (DataPass dataPass : dataPasses) {
            if ("unlimited".equalsIgnoreCase(dataPass.getExpiryReason())) {

                System.out.println("Unlimited Pass :\t" + dataPass.getInfoType() + "\t|" + dataPass.getExpiryReason());
                dataUsed_ult += 0L;
            }
            if (info.getTypeOfAccess().contains("L") && dataPass.getShareDetails() != null
                    && dataPass.getShareDetails().getSharerDataUsage().size() > 0) {

                List<SharerDataUsage> usage = dataPass.getShareDetails().getSharerDataUsage();
                Optional<SharerDataUsage> vol = usage.stream().filter(e -> e.getMsisdn().equals(info.getMsisdn()))
                        .findFirst();
                dataUsed_sha += vol.get().getUsedVolume();

                if ("C".equals(dataPass.getInfoType())) {
                    dataUsed_shaGrp = dataUsed_shaGrp + dataPass.getFup() - dataPass.getVolume();
                    System.out.println("Its a Shaed Pass: Share Data \t" + vol.get().getUsedVolume()
                            + "\t| ShareDtaa Group " + (dataPass.getFup() - dataPass.getVolume()));
                } else if ("E".equals(dataPass.getInfoType())) {
                    dataUsed_shaGrp += dataPass.getVolume();
                    System.out.println("Its a Shaed Pass: Share Data \t" + vol.get().getUsedVolume()
                            + "\t| ShareDtaa Group " + dataPass.getVolume());
                }

            } else {

                System.out.println(
                        "Its a Non-Shaed Pass: \t" + dataPass.getInfoType() + "\t|" + dataPass.getExpiryReason());
                dataUsed_nonSha += dataUsed_nonSha;
            }

            data_avl += dataPass.getFup();
        }

        dataUsed_sha = (dataUsed_ult + dataUsed_sha + dataUsed_nonSha);
        data_rem = data_avl - (dataUsed_nonSha + dataUsed_shaGrp);
        System.out.println("*********************	Data Usage	********************");
        System.out.println("Total Data Used = " + dataUsed_sha);
        System.out.println("Data Avail = " + data_avl);
        System.out.println("Data Remaining = " + data_rem);

        return DataUsageDetails.builder().dataUsed(dataUsed_sha).dataAvail(data_avl).dataRemaining(data_rem).build();
    }

}
