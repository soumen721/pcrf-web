package com.ericsson.eea.billing.service.impl;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingCycle;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.billing.util.ChainCycle;
import com.ericsson.eea.billing.util.CustomrType;
import org.jboss.logging.Logger;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import static com.ericsson.eea.billing.util.BillingConstant.VALID_INFO_TYPE_PREPAID;
import static com.ericsson.eea.billing.util.BillingUtils.toLocalDateTime;

public class PrePaidDataUsageCalculationService implements DataUsageCalculationService {
  private static final Logger log = Logger.getLogger(PrePaidDataUsageCalculationService.class);

  @Override
  public SubscriberBillingInfo calculateDataUsage(
      GetCurrentAndAvailableDataProductsResponse response)
      throws SubscriberBillingInfoNotAvailableException {

    GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo subscriberInfo =
        response.getMessage().getSubscriberInfo();
    log.info("Billing details for MSISDN ==> " + subscriberInfo.getMsisdn());

    if (CustomrType.P14 == BillingUtils.getCustomerTypeForPrepaid(subscriberInfo)) {

      return SubscriberBillingInfo.builder()
          .billingPeriodStartDate(LocalDate.now(ZoneOffset.UTC).atStartOfDay().withDayOfMonth(1)
              .toEpochSecond(ZoneOffset.UTC))
          .billingPeriodEndDate(
              LocalDate.now(ZoneOffset.UTC).atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC))
          .dataUsed(0D).dataAvail(0D).dataUsedShared(0D)

          .lbcDataUsed(0D).lbcDataAvail(0D).lbcDataUsedShared(0D)

          .pbcDataUsed(0D).pbcDataAvail(0D).pbcDataUsedShared(0D).build();
    } else {

      List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

      List<DataPass> filteredDataPasses = BillingUtils.getFilteredDataPass(dataPasses,
          type -> VALID_INFO_TYPE_PREPAID.contains(type.getInfoType()));
      LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
      LocalDateTime billingStardDate;
      LocalDateTime billingEndDate;
      List<DataPass> calPasses = filteredDataPasses.stream()
          .filter(pass -> (toLocalDateTime(pass.getPassStartTime()).isAfter(now.minusDays(90))
              || toLocalDateTime(pass.getPassStartTime()).isEqual(now.minusDays(90))))
          // .limit(3)
          .collect(Collectors.toList());

      ChainCycle billCycle = ChainCycle.builder().build();

      SubscriberBillingInfo billingInfo = SubscriberBillingInfo.builder().build();

      if (calPasses == null) {
        billingStardDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay().withDayOfMonth(1);
        billingEndDate = now;
        billingInfo.setBillingPeriodStartDate(billingStardDate.toEpochSecond(ZoneOffset.UTC));
        billingInfo.setBillingPeriodEndDate(billingEndDate.toEpochSecond(ZoneOffset.UTC));
      } else {
        for (int i = 0; i < calPasses.size(); i++) {
          final DataPass pass = calPasses.get(i);

          if (billCycle.getCurrentCycle() == null) {
            billCycle.setCurrentCycle(BillingCycle.CURRENT);
          } else if (billCycle.getCurrentCycle() == BillingCycle.CURRENT) {
            if ("S".equals(pass.getInfoType())) {
              billCycle.setCurrentCycle(BillingCycle.CURRENT);
            } else {
              billCycle.setCurrentCycle(BillingCycle.PREVIOUS);
            }
          } else if (billCycle.getCurrentCycle() == BillingCycle.PREVIOUS) {
            billCycle.setCurrentCycle(BillingCycle.PENULTIMATE);
          } else {
            break;
          }

          if (BillingCycle.CURRENT == billCycle.getCurrentCycle()) {

            if ("S".equals(pass.getInfoType())) {
              billingInfo
                  .setDataAvail(billingInfo.getDataAvail() != null ? billingInfo.getDataAvail()
                      : 0 + ((double) pass.getFup() / 1024));
            } else {
              billingInfo.setBillingPeriodStartDate(
                  toLocalDateTime(pass.getPassStartTime()).toEpochSecond(ZoneOffset.UTC));
              billingInfo.setBillingPeriodEndDate(
                  toLocalDateTime(pass.getPassEndTime()).toEpochSecond(ZoneOffset.UTC));

              if ("C".equals(pass.getInfoType())) {
                billingInfo.setDataUsed((double) (pass.getFup() - pass.getVolume()) / 1024);
              } else if ("E".equals(pass.getInfoType())) {
                billingInfo.setDataUsed((double) (pass.getVolume()) / 1024);
              }

              billingInfo.setDataAvail((double) pass.getFup() / 1024);
            }
          } else if (BillingCycle.PREVIOUS == billCycle.getCurrentCycle()) {
            billingInfo.setLbcStartDate(
                toLocalDateTime(pass.getPassStartTime()).toEpochSecond(ZoneOffset.UTC));
            billingInfo.setLbcEndDate(
                toLocalDateTime(pass.getPassEndTime()).toEpochSecond(ZoneOffset.UTC));

            billingInfo.setLbcDataUsed((double) (pass.getVolume()) / 1024);
            billingInfo.setLbcDataAvail((double) pass.getFup() / 1024);
          } else if (BillingCycle.PENULTIMATE == billCycle.getCurrentCycle()) {
            billingInfo.setPbcStartDate(
                toLocalDateTime(pass.getPassStartTime()).toEpochSecond(ZoneOffset.UTC));
            billingInfo.setPbcEndDate(
                toLocalDateTime(pass.getPassEndTime()).toEpochSecond(ZoneOffset.UTC));

            billingInfo.setPbcDataUsed((double) (pass.getVolume()) / 1024);
            billingInfo.setPbcDataAvail((double) pass.getFup() / 1024);
          }
        }
      }

      BillingUtils.printDataUsage(billingInfo);
      log.info(
          "\n++++++++++++++++++++++++++++++++ SubscriberBillingInfo Postpaid  ++++++++++++++++++++++++++++++++++++++++");
      log.info(billingInfo.toString());
      log.info(
          "++++++++++++++++++++++++++++++++ SubscriberBillingInfo Postpaid    ++++++++++++++++++++++++++++++++++++++++");

      return billingInfo;
    }


  }

}
