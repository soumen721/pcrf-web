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

    SubscriberInfo subscriberInfo = response.getMessage().getSubscriberInfo();
    log.info("PostPaid Billing Details for MSISDN ==> " + subscriberInfo.getMsisdn());
    List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

    // Filter for valid pass & Remove passes after FUP_CHANGE pass including FUP change pass
    List<DataPass> filteredDataPasses = filterDataPassOnFUPChange(getFilteredDataPass(dataPasses,
        type -> VALID_INFO_TYPE_POSTPAID.contains(type.getInfoType())));
    log.info("\nAfter applying fup_change filter--------------");
    filteredDataPasses.forEach(BillingUtils::printLog);

    log.info(
        "\n************************* Start of Calculation for Postpaid  ****************************");
    log.info("*************************	Current Period	****************************");
    // populate bill-cycle
    int currentDate = LocalDate.now(ZoneOffset.UTC).getDayOfMonth();
    int billingDate = Integer.valueOf(subscriberInfo.getBillCycle() + "");
    LocalDateTime currentBillCycleEndDate =
        LocalDate.now(ZoneOffset.UTC).atTime(23, 59, 59).withDayOfMonth(billingDate);
    if (billingDate < currentDate) {
      currentBillCycleEndDate = currentBillCycleEndDate.plusMonths(1);
    }

    final LocalDateTime currentBillCycleStartDate =
        currentBillCycleEndDate.withHour(0).withMinute(0).withSecond(01).minusMonths(1).plusDays(1);
    final List<DataPass> currentCycleDataPasses = getFilteredDataPassBasedOnBillCycle(
        filteredDataPasses, currentBillCycleStartDate, currentBillCycleEndDate);

    final DataUsageDetails currentCycleDataUsage =
        calculateDataUsageForCycle(currentCycleDataPasses, subscriberInfo, BillingCycle.CURRENT,
            currentBillCycleStartDate, currentBillCycleEndDate);

    log.info("*************************	Previous Period	****************************");
    final LocalDateTime previousBillCycleEndDate = currentBillCycleEndDate.minusMonths(1);
    final LocalDateTime previousBillCycleStartDate = currentBillCycleStartDate.minusMonths(1);
    final List<DataPass> previousCycleDataPasses = getFilteredDataPassBasedOnBillCycle(
        filteredDataPasses, previousBillCycleStartDate, previousBillCycleEndDate);

    log.info("Final Pass for calculation -- Previous Cycle");
    previousCycleDataPasses.forEach(BillingUtils::printLog);

    final DataUsageDetails previousCycleDataUsage =
        calculateDataUsageForCycle(previousCycleDataPasses, subscriberInfo, BillingCycle.PREVIOUS,
            previousBillCycleStartDate, previousBillCycleEndDate);

    log.info("*************************	Penultimate Period	****************************");
    final LocalDateTime penulBillCycleEndDate = previousBillCycleEndDate.minusMonths(1);
    final LocalDateTime penulBillCycleStartDate = previousBillCycleStartDate.minusMonths(1);

    final List<DataPass> penultimateCycleDataPasses = getFilteredDataPassBasedOnBillCycle(
        filteredDataPasses, penulBillCycleStartDate, penulBillCycleEndDate);

    log.info("Final Pass for calculation -- Previous Cycle");
    penultimateCycleDataPasses.forEach(BillingUtils::printLog);

    final DataUsageDetails penultimateCycleDataUsage =
        calculateDataUsageForCycle(penultimateCycleDataPasses, subscriberInfo,
            BillingCycle.PENULTIMATE, penulBillCycleStartDate, penulBillCycleEndDate);

    log.info(
        "************************* End of Calculation for Postpaid  ****************************\n");
    final SubscriberBillingInfo billingInfo = SubscriberBillingInfo.builder()

        // Current Period
        .billingPeriodStartDate(currentCycleDataUsage.getBillingPeriodStartDate())
        .billingPeriodEndDate(currentCycleDataUsage.getBillingPeriodEndDate())
        .dataAvail(currentCycleDataUsage.getDataAvail())
        .dataUsed(currentCycleDataUsage.getDataUsed())
        .dataUsedShared(currentCycleDataUsage.getDataUsedShared())
        .zeroRatedDataUsed(currentCycleDataUsage.getZeroRatedDataUsed())
        .zeroRatedDataUsedPerService(currentCycleDataUsage.getZeroRatedDataUsedPerService())

        // Previous Period
        .lbcStartDate(previousCycleDataUsage.getBillingPeriodStartDate())
        .lbcEndDate(previousCycleDataUsage.getBillingPeriodEndDate())
        .lbcDataAvail(previousCycleDataUsage.getDataAvail())
        .lbcDataUsed(previousCycleDataUsage.getDataUsed())
        .lbcDataUsedShared(currentCycleDataUsage.getDataUsedShared())
        .lbcZeroRatedDataUsed(previousCycleDataUsage.getZeroRatedDataUsed())
        .lbcZeroRatedDataUsedPerService(previousCycleDataUsage.getZeroRatedDataUsedPerService())

        // Penultimate Period
        .pbcStartDate(penultimateCycleDataUsage.getBillingPeriodStartDate())
        .pbcEndDate(penultimateCycleDataUsage.getBillingPeriodEndDate())
        .pbcDataAvail(penultimateCycleDataUsage.getDataAvail())
        .pbcDataUsed(penultimateCycleDataUsage.getDataUsed())
        .pbcDataUsedShared(currentCycleDataUsage.getDataUsedShared())
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

  private DataUsageDetails calculateDataUsageForCycle(final List<DataPass> dataPasses,
      final SubscriberInfo info, final BillingCycle billingCycle,
      final LocalDateTime billingStartDate, final LocalDateTime billingEndDate) {

    double dataAvail;
    double dataUsed;
    double dataUsedSharer = 0D;
    double dataUsedShared = 0D;
    double zeroRatedDataUsed = 0D;

    double dataUsedUnlimited = 0D;
    double dataUsedSharedGrp = 0D;
    double dataUsedNonShared = 0D;
    double dataRemaining;
    double dataUsageCPass = 0D;
    Map<String, Double> zeroRatedDataUsedPerService = null;

    log.info(
        "\n=============  " + billingCycle.name() + " Cycle Calculation begin here  =============");

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
      // Sharer Data Usage
      dataUsedSharer = dataPasses.stream()
          .filter(t -> BillingConstant.HISTORICAL_CYCLE_INFO_TYPE.contains(t.getInfoType()))
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

      // Shared Group Data Usage
      dataUsedSharedGrp =
          dataUsageCPass + getUsageDetails(dataPasses, e -> BillingUtils.isSharedPass(info, e));

      // Non Shared Data Usage
      dataUsedNonShared =
          dataUsageCPass + getUsageDetails(dataPasses, e -> !BillingUtils.isSharedPass(info, e));

      // Available Data for Use
      dataAvail = dataPasses.stream().filter(e -> passToConsider.contains(e.getInfoType()))
          .mapToDouble(DataPass::getFup).sum();

    }

    zeroRatedDataUsed =
        getZeroRatedUsage(dataPasses, BillingCycle.CURRENT == billingCycle ? "ZR" : "EZR");
    zeroRatedDataUsedPerService = getZeroRatedDataUsedPerService(dataPasses,
        BillingCycle.CURRENT == billingCycle ? "ZR" : "EZR");

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
        .dataUsed(BillingUtils.getDataUsageInMB(dataUsed))
        .dataAvail(BillingUtils.getDataUsageInMB(dataAvail))
        .dataUsedShared(BillingUtils.getDataUsageInMB(dataUsedShared))
        .dataRemaining(BillingUtils.getDataUsageInMB(dataRemaining))
        .zeroRatedDataUsed(BillingUtils.getDataUsageInMB(zeroRatedDataUsed))
        .zeroRatedDataUsedPerService(zeroRatedDataUsedPerService).build();
  }

  private double getUsageDetails(final List<DataPass> dataPasses,
      final Predicate<? super DataPass> predicate) {

    return dataPasses.stream()
        .filter(e -> BillingConstant.HISTORICAL_CYCLE_INFO_TYPE.contains(e.getInfoType()))
        .filter(predicate).mapToDouble(DataPass::getVolume).sum();
  }

  private Map<String, Double> getZeroRatedDataUsedPerService(final List<DataPass> dataPasses,
      final String infoType) {

    return dataPasses.stream().filter(e -> infoType.equals(e.getInfoType()))
        .collect(Collectors.groupingBy(DataPass::getPassType,
            Collectors.summingDouble(e -> BillingUtils.getDataUsageInMB((double) e.getVolume()))));
  }

  private Double getZeroRatedUsage(final List<DataPass> dataPasses, final String infoType) {

    return dataPasses.stream().filter(e -> infoType.equals(e.getInfoType()))
        .mapToDouble(DataPass::getVolume).sum();
  }

}
