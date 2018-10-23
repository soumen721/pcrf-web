package com.ericsson.eea.billing.service.impl;

import static com.ericsson.eea.billing.util.BillingConstant.VALID_INFO_TYPE_POSTPAID;
import static com.ericsson.eea.billing.util.BillingUtils.filterDataPassOnFUPChange;
import static com.ericsson.eea.billing.util.BillingUtils.getFilteredDataPass;
import static com.ericsson.eea.billing.util.BillingUtils.getFilteredDataPassBasedOnBillCycle;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.DataUsageDetails;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingConstant;
import com.ericsson.eea.billing.util.BillingCycle;
import com.ericsson.eea.billing.util.BillingUtils;

public class PostPaidDataUsageCalculationService implements DataUsageCalculationService {
  private static final Logger log = Logger.getLogger(PostPaidDataUsageCalculationService.class);

  @Override
  public SubscriberBillingInfo calculateDataUsage(
      GetCurrentAndAvailableDataProductsResponse response)
      throws SubscriberBillingRetrievalFailedException, SubscriberBillingInfoNotAvailableException {

    SubscriberInfo info = response.getMessage().getSubscriberInfo();
    log.info("Billing Details for MSISDN ==> " + info.getMsisdn());
    List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

    // Filter for valid pass & Remove passes after FUP_CHANGE pass including FUP change pass
    List<DataPass> filteredDataPasses = filterDataPassOnFUPChange(getFilteredDataPass(dataPasses,
        type -> VALID_INFO_TYPE_POSTPAID.contains(type.getInfoType())));
    log.info("After applying fup_change filter--------------");
    filteredDataPasses.forEach(BillingUtils::printLog);

    log.info(
        "\n************************* Start of Calculation for Postpaid  ****************************");
    log.info("*************************	Current Period	****************************\n");
    final LocalDateTime currentBillCycleEndDate = LocalDate.now(ZoneOffset.UTC).atTime(23, 59, 59)
        .withDayOfMonth(Integer.valueOf(info.getBillCycle() + ""));
    final LocalDateTime currentBillCycleStartDate =
        currentBillCycleEndDate.withHour(0).withMinute(0).withSecond(01).minusMonths(1).plusDays(1);
    List<DataPass> currentCycleDataPasses = getFilteredDataPassBasedOnBillCycle(filteredDataPasses,
        currentBillCycleStartDate, currentBillCycleEndDate);

    log.info("Final Pass for calculation -- Current Cycle");
    currentCycleDataPasses.forEach(BillingUtils::printLog);

    DataUsageDetails currentCycleDataUsage = calculateDataUsageForCycle(currentCycleDataPasses,
        info, BillingCycle.CURRENT, currentBillCycleStartDate, currentBillCycleEndDate);

    log.info("*************************	Previous Period	****************************\n");
    final LocalDateTime previousBillCycleEndDate = currentBillCycleEndDate.minusMonths(1);
    final LocalDateTime previousBillCycleStartDate = currentBillCycleStartDate.minusMonths(1);
    List<DataPass> previousCycleDataPasses = getFilteredDataPassBasedOnBillCycle(filteredDataPasses,
        previousBillCycleStartDate, previousBillCycleEndDate);

    log.info("Final Pass for calculation -- Previous Cycle");
    previousCycleDataPasses.forEach(BillingUtils::printLog);

    DataUsageDetails previousCycleDataUsage = calculateDataUsageForCycle(previousCycleDataPasses,
        info, BillingCycle.PREVIOUS, previousBillCycleStartDate, previousBillCycleEndDate);

    log.info("*************************	Penultimate Period	****************************\n");
    final LocalDateTime penulBillCycleEndDate = previousBillCycleEndDate.minusMonths(1);
    final LocalDateTime penulBillCycleStartDate = previousBillCycleStartDate.minusMonths(1);

    List<DataPass> penultimateCycleDataPasses = getFilteredDataPassBasedOnBillCycle(
        filteredDataPasses, penulBillCycleStartDate, penulBillCycleEndDate);

    log.info("Final Pass for calculation -- Previous Cycle");
    penultimateCycleDataPasses.forEach(BillingUtils::printLog);

    DataUsageDetails penultimateCycleDataUsage =
        calculateDataUsageForCycle(penultimateCycleDataPasses, info, BillingCycle.PENULTIMATE,
            penulBillCycleStartDate, penulBillCycleEndDate);

    log.info(
        "************************* End of Calculation for Postpaid  ****************************\n");
    SubscriberBillingInfo billingInfo = SubscriberBillingInfo.builder()

        // Current Period
        .billingPeriodStartDate(currentCycleDataUsage.getBillingPeriodStartDate())
        .billingPeriodEndDate(currentCycleDataUsage.getBillingPeriodEndDate())
        .dataAvail(currentCycleDataUsage.getDataAvail())
        .dataUsed(currentCycleDataUsage.getDataUsed())
        .zeroRatedDataUsed(currentCycleDataUsage.getZeroRatedDataUsed())
        .zeroRatedDataUsedPerService(currentCycleDataUsage.getZeroRatedDataUsedPerService())

        // Previous Period
        .lbcStartDate(previousCycleDataUsage.getBillingPeriodStartDate())
        .lbcEndDate(previousCycleDataUsage.getBillingPeriodEndDate())
        .lbcDataAvail(previousCycleDataUsage.getDataAvail())
        .lbcDataUsed(previousCycleDataUsage.getDataUsed())
        .lbcZeroRatedDataUsed(previousCycleDataUsage.getZeroRatedDataUsed())
        .lbcZeroRatedDataUsedPerService(previousCycleDataUsage.getZeroRatedDataUsedPerService())

        // Penultimate Period
        .pbcStartDate(penultimateCycleDataUsage.getBillingPeriodStartDate())
        .pbcEndDate(penultimateCycleDataUsage.getBillingPeriodEndDate())
        .pbcDataAvail(penultimateCycleDataUsage.getDataAvail())
        .pbcDataUsed(penultimateCycleDataUsage.getDataUsed())
        .pbcZeroRatedDataUsed(penultimateCycleDataUsage.getZeroRatedDataUsed())
        .pbcZeroRatedDataUsedPerService(penultimateCycleDataUsage.getZeroRatedDataUsedPerService())
        .build();

    BillingUtils.printDataUsage(billingInfo);
    log.info(
        "\n++++++++++++++++++++++++++++++++ SubscriberBillingInfo Postpaid  ++++++++++++++++++++++++++++++++++++++++");
    log.info(billingInfo.toString());
    log.info(
        "++++++++++++++++++++++++++++++++ SubscriberBillingInfo Postpaid    ++++++++++++++++++++++++++++++++++++++++");

    return billingInfo;
  }

  private DataUsageDetails calculateDataUsageForCycle(List<DataPass> dataPasses,
      final SubscriberInfo info, BillingCycle billingCycle, LocalDateTime billingStartDate,
      LocalDateTime billingEndDate) {

    double dataAvail;
    double dataUsed;
    double dataUsedSharer = 0D;
    double zeroRatedDataUsed = 0D;

    double dataUsedUnlimited = 0D;
    double dataUsedSharedGrp = 0D;
    double dataUsedNonShared = 0D;
    double dataRemaining;
    double dataUsageCPass = 0D;
    Map<String, Double> zeroRatedDataUsedPerService = null;

    log.info("Calculation begin here=============");

    List<String> passToConsider =
        BillingCycle.CURRENT == billingCycle ? BillingConstant.CURRENT_CYCLE_INFO_TYPE
            : BillingConstant.HISTORICAL_CYCLE_INFO_TYPE;

    // Calculate for Unlimited Pass
    if (BillingUtils.isUnlimitedUsage(dataPasses)) {
      dataUsedUnlimited = dataPasses.stream().filter(e -> passToConsider.contains(e.getInfoType()))
          .filter(e -> e.getShareDetails() != null)
          .map(e -> e.getShareDetails().getSharerDataUsage()).flatMap(Collection::stream)
          .filter(sha -> info.getMsisdn().equals(sha.getMsisdn()))
          .mapToDouble(SharerDataUsage::getUsedVolume).sum();

      dataAvail = -1D;
      dataRemaining = -1D;
    } else {

      dataUsedSharer = dataPasses.stream().filter(t -> passToConsider.contains(t.getInfoType()))
          .filter(p -> BillingUtils.isSharedPass(info, p))
          .map(e -> e.getShareDetails().getSharerDataUsage()).flatMap(Collection::stream)
          .filter(sha -> info.getMsisdn().equals(sha.getMsisdn()))
          .mapToDouble(SharerDataUsage::getUsedVolume).sum();


      if (BillingCycle.CURRENT == billingCycle) {
        Optional<DataPass> currentDataPass =
            dataPasses.stream().filter(e -> "C".equals(e.getInfoType())).findFirst();
        if (currentDataPass.isPresent()) {
          dataUsageCPass =
              (double) currentDataPass.get().getFup() - currentDataPass.get().getVolume();
        }
      }

      dataUsedSharedGrp =
          dataUsageCPass + getUsageDetails(dataPasses, e -> BillingUtils.isSharedPass(info, e));

      dataUsedNonShared =
          dataUsageCPass + getUsageDetails(dataPasses, e -> !BillingUtils.isSharedPass(info, e));

      dataAvail = dataPasses.stream().filter(e -> passToConsider.contains(e.getInfoType()))
          .mapToDouble(DataPass::getFup).sum();

    }

    zeroRatedDataUsed =
        getZeroRatedUsage(dataPasses, BillingCycle.CURRENT == billingCycle ? "ZR" : "EZR");
    zeroRatedDataUsedPerService = getZeroRatedDataUsedPerService(dataPasses,
        BillingCycle.CURRENT == billingCycle ? "ZR" : "EZR");
    /*
     * if (BillingCycle.CURRENT == billingCycle) { zeroRatedDataUsed = getZeroRatedUsage(dataPasses,
     * BillingCycle.CURRENT == billingCycle ? "ZR" : "EZR"); zeroRatedDataUsedPerService =
     * getZeroRatedDataUsedPerService(dataPasses, "ZR"); } else if (BillingCycle.PREVIOUS ==
     * billingCycle) { zeroRatedDataUsed = getZeroRatedUsage(dataPasses, "EZR");
     * zeroRatedDataUsedPerService = getZeroRatedDataUsedPerService(dataPasses, "EZR"); }
     */

    dataUsed = (dataUsedUnlimited + dataUsedSharer + dataUsedNonShared);
    dataRemaining = dataAvail - (dataUsedNonShared + dataUsedSharedGrp);
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
        .zeroRatedDataUsedPerService(zeroRatedDataUsedPerService).build();
  }

  private double getUsageDetails(List<DataPass> dataPasses, Predicate<? super DataPass> predicate) {

    return dataPasses.stream()
        .filter(e -> BillingConstant.HISTORICAL_CYCLE_INFO_TYPE.contains(e.getInfoType()))
        .filter(predicate).mapToDouble(DataPass::getVolume).sum();
  }

  private Map<String, Double> getZeroRatedDataUsedPerService(List<DataPass> dataPasses,
      String infoType) {

    return dataPasses.stream().filter(e -> infoType.equals(e.getInfoType())).collect(Collectors
        .groupingBy(DataPass::getPassType, Collectors.summingDouble(DataPass::getVolume)));
  }

  private Double getZeroRatedUsage(List<DataPass> dataPasses, String infoType) {

    return dataPasses.stream().filter(e -> infoType.equals(e.getInfoType()))
        .mapToDouble(DataPass::getVolume).sum();
  }

}
