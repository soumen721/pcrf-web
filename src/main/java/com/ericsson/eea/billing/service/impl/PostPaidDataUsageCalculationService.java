package com.ericsson.eea.billing.service.impl;

import static com.ericsson.eea.billing.util.BillingConstant.VALID_INFO_TYPE_POSTPAID;
import static com.ericsson.eea.billing.util.BillingUtils.filterDataPassOnFUPChange;
import static com.ericsson.eea.billing.util.BillingUtils.getFilteredDataPassBasedOnBillCycle;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.jboss.logging.Logger;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingCycle;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.pcrf.model.DataUsageDetails;

public class PostPaidDataUsageCalculationService implements DataUsageCalculationService {
	private static final Logger log = Logger.getLogger(PostPaidDataUsageCalculationService.class);
	
    @Override
    public SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response) {

        SubscriberInfo info = response.getMessage().getSubscriberInfo();
        log.info("MSISDN ID ==> " + info.getMsisdn());
        List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

        List<DataPass> filteredDataPasses = BillingUtils.getFilteredDataPass(dataPasses, type -> VALID_INFO_TYPE_POSTPAID.contains(type.getInfoType()));

        // For Current Period
        log.info("*************************	Current Period	****************************\n");
        LocalDateTime billCycleEndDate = LocalDate.now().atStartOfDay();
        LocalDateTime billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> currentCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        log.info("Final Pass after FUP remove Current Cycle");
        currentCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails currentCycleDataUsage = printDataUsage(currentCycleDataPasses, info, BillingCycle.CURRENT, billCycleStartDate, billCycleEndDate);

        // For Previous Period
        log.info("*************************	Previous Period	****************************\n");
        billCycleEndDate = billCycleEndDate.minusMonths(1);
        billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> previousCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        log.info("Final Pass after FUP remove -- Previous Cycle");
        previousCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails previousCycleDataUsage = printDataUsage(previousCycleDataPasses, info, BillingCycle.PREVIOUS, billCycleStartDate, billCycleEndDate);

        // For Penultimate Period
        log.info("*************************	Penultimate Period	****************************\n");
        billCycleEndDate = billCycleEndDate.minusMonths(1);
        billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
        List<DataPass> penultimateCycleDataPasses = filterDataPassOnFUPChange(
                getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

        log.info("Final Pass after FUP remove -- Previous Cycle");
        penultimateCycleDataPasses.forEach(BillingUtils::printLog);

        DataUsageDetails penultimateCycleDataUsage = printDataUsage(penultimateCycleDataPasses, info, BillingCycle.PENULTIMATE, billCycleStartDate, billCycleEndDate);

        SubscriberBillingInfo billingInfo = SubscriberBillingInfo.builder()
                // Basic
                .billingUpdateTime((long) info.getLastCheckedDate().getMillisecond())

                // Current Period
                .dataAvail(currentCycleDataUsage.getDataAvail()).dataUsed(currentCycleDataUsage.getDataUsed())
                .zeroRatedDataUsed(currentCycleDataUsage.getZeroRatedDataUsed())

                // Previous Period
                .lbcDataAvail(previousCycleDataUsage.getDataAvail()).lbcDataUsed(previousCycleDataUsage.getDataUsed())
                .lbcZeroRatedDataUsed(previousCycleDataUsage.getZeroRatedDataUsed())

                // Penultimate Period
                .pbcDataAvail(penultimateCycleDataUsage.getDataAvail())
                .pbcDataUsed(penultimateCycleDataUsage.getDataUsed())
                .pbcZeroRatedDataUsed(penultimateCycleDataUsage.getZeroRatedDataUsed()).build();

        BillingUtils.printDataUsage(billingInfo);
        log.info(
                "\n++++++++++++++++++++++++++++++++ SubscriberBillingInfo Postpaid  ++++++++++++++++++++++++++++++++++++++++");
        log.info(billingInfo.toString());
        log.info(
                "++++++++++++++++++++++++++++++++ SubscriberBillingInfo Postpaid    ++++++++++++++++++++++++++++++++++++++++");

        return billingInfo;
    }

    private DataUsageDetails printDataUsage(List<DataPass> dataPasses, final SubscriberInfo info, BillingCycle billingCycle, LocalDateTime billingStartDate, LocalDateTime billingEndDate) {

        double dataAvail;
        double dataUsed;
        double dataUsedShared = 0D;
        double zeroRatedDataUsed = 0D;

        double dataUsedUnlimited = 0D;
        double dataUsedSharedGrp = 0D;
        double dataUsedNonShared = 0D;
        double dataRemaining = 0D;
        double dataUsageCPass = 0D;

        log.info("Calculation begin here=============");

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
            //Calculate for Not Unlimited Pass
            dataUsedShared = dataPasses.stream()
                    .filter(p -> BillingUtils.isSharedPass(info, p))
                    .map(e -> e.getShareDetails().getSharerDataUsage())
                    .flatMap(Collection::stream)
                    .filter(sha -> info.getMsisdn().equals(sha.getMsisdn()))
                    .mapToDouble(SharerDataUsage::getUsedVolume)
                    .sum();

            if (BillingCycle.CURRENT == billingCycle) {
                Optional<DataPass> currentDataPass = dataPasses.stream()
                        .filter(e -> "C".equals(e.getInfoType()))
                        .findFirst();
                if (currentDataPass.isPresent()) {
                    dataUsageCPass = (double) currentDataPass.get().getFup() - currentDataPass.get().getVolume();
                }
            }

            dataUsedSharedGrp = dataUsageCPass + getUsageDetails(dataPasses, e -> BillingUtils.isSharedPass(info, e));

            dataUsedNonShared = dataUsageCPass + getUsageDetails(dataPasses, e -> !BillingUtils.isSharedPass(info, e));

            dataAvail = dataPasses.stream()
                    .filter(e -> Arrays.asList("C", "E", "S").contains(e.getInfoType()))
                    .mapToDouble(DataPass::getFup)
                    .sum();

        }

        if (BillingCycle.CURRENT == billingCycle) {
            zeroRatedDataUsed = getZeroRatedUsage(dataPasses, "ZR");
        } else if (BillingCycle.PREVIOUS == billingCycle) {
            zeroRatedDataUsed = getZeroRatedUsage(dataPasses, "EZR");
        }

        dataUsed = (dataUsedUnlimited + dataUsedShared + dataUsedNonShared);
        //dataRemaining = dataAvail - (dataUsedNonShared + dataUsedSharedGrp);
        log.info("*********************	Data Usage	********************");
        log.info("Data Usage Unlimited = " + dataUsedUnlimited);
        log.info("Total Data Used = " + dataUsed);
        log.info("Data Avail = " + dataAvail);
        log.info("Data ZeroRated = " + zeroRatedDataUsed);

        return DataUsageDetails.builder()
                .billingPeriodStartDate(billingStartDate.toEpochSecond(ZoneOffset.UTC))
                .billingPeriodEndDate(billingEndDate.toEpochSecond(ZoneOffset.UTC))
                .dataUsed(dataUsed != 0D ? dataUsed / 1024 : 0D)
                .dataAvail(dataAvail != 0D && dataAvail != -1D ? dataAvail / 1024 : 0D)
                .dataRemaining(dataRemaining != 0D && dataRemaining != -1D ? dataRemaining / 1024 : 0D)
                .zeroRatedDataUsed(zeroRatedDataUsed != 0D ? zeroRatedDataUsed / 1024 : 0D)
                .build();
    }

    private double getUsageDetails(List<DataPass> dataPasses, Predicate<? super DataPass> predicate) {

        return dataPasses.stream()
                .filter(predicate)
                .mapToDouble(DataPass::getVolume)
                .sum();
    }

    private Double getZeroRatedUsage(List<DataPass> dataPasses, String infoType) {

        return dataPasses.stream()
                .filter(e -> infoType.equals(e.getInfoType()))
                .mapToDouble(DataPass::getVolume)
                .sum();
    }

}
