package com.ericsson.eea.billing.service.impl;

import static com.ericsson.eea.billing.util.BillingConstant.BYTE_TO_MB;
import static com.ericsson.eea.billing.util.BillingConstant.VALID_INFO_TYPE_PREPAID;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.logging.Logger;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingCycle;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.billing.util.ChainCycle;
import com.ericsson.eea.billing.util.CustomrType;

public class PrePaidDataUsageCalculationService implements DataUsageCalculationService {
	private static final Logger log = Logger.getLogger(PrePaidDataUsageCalculationService.class);

	@Override
	public SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response)
			throws SubscriberBillingInfoNotAvailableException {

		final GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo subscriberInfo = response.getMessage()
				.getSubscriberInfo();
		log.info("Prepaid Billing details for MSISDN ==> " + subscriberInfo.getMsisdn());

		if (CustomrType.P14 == BillingUtils.getCustomerTypeForPrepaid(subscriberInfo)) {
			log.info("P14 Prepaid Subscriber " + subscriberInfo.getMsisdn());
			return SubscriberBillingInfo.builder()
					.billingPeriodStartDate(LocalDate.now(ZoneOffset.UTC).atStartOfDay().withDayOfMonth(1)
							.toEpochSecond(ZoneOffset.UTC))
					.billingPeriodEndDate(
							LocalDate.now(ZoneOffset.UTC).atTime(23, 59, 59).toEpochSecond(ZoneOffset.UTC))
					.dataUsed(0D).dataAvail(0D).dataUsedShared(0D)

					.lbcDataUsed(0D).lbcDataAvail(0D).lbcDataUsedShared(0D)

					.pbcDataUsed(0D).pbcDataAvail(0D).pbcDataUsedShared(0D).build();
		} else {

			final List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

			final List<DataPass> filteredDataPasses = BillingUtils.getFilteredDataPass(dataPasses,
					type -> VALID_INFO_TYPE_PREPAID.contains(type.getInfoType()));
			LocalDateTime now = LocalDateTime.now(Clock.systemUTC());
			LocalDateTime billingStardDate;
			LocalDateTime billingEndDate;
			final List<DataPass> calPasses = filteredDataPasses.stream()
					.filter(pass -> (pass.getPassStartTime().isAfter(now.minusDays(90))
							|| pass.getPassStartTime().isEqual(now.minusDays(90))))
					.collect(Collectors.toList());

			log.info("After applied filter---------------");
			calPasses.forEach(BillingUtils::printLog);
			ChainCycle billCycle = ChainCycle.builder().build();

			final SubscriberBillingInfo billingInfo = SubscriberBillingInfo.builder().build();

			if (calPasses == null || calPasses.isEmpty()) {

				billingInfo.setSubscriberType(2);
				billingStardDate = LocalDate.now(ZoneOffset.UTC).atStartOfDay().withDayOfMonth(1);
				billingEndDate = now;
				billingInfo.setBillingPeriodStartDate(billingStardDate.toEpochSecond(ZoneOffset.UTC));
				billingInfo.setBillingPeriodEndDate(billingEndDate.toEpochSecond(ZoneOffset.UTC));
			} else {
				for (int i = 0; i < calPasses.size(); i++) {
					final DataPass dataPass = calPasses.get(i);

					if (billCycle.getCurrentCycle() == null) {
						billCycle.setCurrentCycle(BillingCycle.CURRENT);
					} else if (billCycle.getCurrentCycle() == BillingCycle.CURRENT) {
						if ("S".equals(dataPass.getInfoType())) {
							billCycle.setCurrentCycle(BillingCycle.CURRENT);
						} else {
							billCycle.setCurrentCycle(BillingCycle.PREVIOUS);
						}
					} else if (billCycle.getCurrentCycle() == BillingCycle.PREVIOUS) {
						billCycle.setCurrentCycle(BillingCycle.PENULTIMATE);
					} else {
						break;
					}

					log.info("*************** Data Pass for " + billCycle.getCurrentCycle()
							+ " Bill Cycle ***************");
					BillingUtils.printLog(dataPass);
					log.info("Bill Cycle Start Date\t" + dataPass.getPassStartTime().toLocalDate());
					log.info("Bill Cycle End Date\t" + dataPass.getPassEndTime().toLocalDate());

					if (BillingCycle.CURRENT == billCycle.getCurrentCycle()) {
						if ("S".equals(dataPass.getInfoType())) {
							billingInfo.setDataAvail(billingInfo.getDataAvail() != null ? billingInfo.getDataAvail()
									: 0 + BillingUtils.getDataUsageInMB((double) (dataPass.getFup())));

							log.info("As this is 'S' pass so re-calculating Cycle period");
							log.info("Bill Cycle Start Date\t"
									+ Instant.ofEpochSecond(billingInfo.getBillingPeriodStartDate())
											.atZone(ZoneId.systemDefault()).toLocalDate());
							log.info("Bill Cycle End Date\t"
									+ Instant.ofEpochSecond(billingInfo.getBillingPeriodEndDate())
											.atZone(ZoneId.systemDefault()).toLocalDate());
						} else {
							billingInfo.setBillingPeriodStartDate(
									dataPass.getPassStartTime().toEpochSecond(ZoneOffset.UTC));
							billingInfo
									.setBillingPeriodEndDate(dataPass.getPassEndTime().toEpochSecond(ZoneOffset.UTC));

							if ("C".equals(dataPass.getInfoType())) {
								billingInfo.setDataUsed(BillingUtils
										.getDataUsageInMB((double) (dataPass.getFup() - dataPass.getVolume())));
							} else if ("E".equals(dataPass.getInfoType())) {
								billingInfo.setDataUsed((double) (dataPass.getVolume()) / BYTE_TO_MB);
							}

							billingInfo.setDataAvail(BillingUtils.getDataUsageInMB((double) (dataPass.getFup())));
						}
					} else if (BillingCycle.PREVIOUS == billCycle.getCurrentCycle()) {
						billingInfo.setLbcStartDate(dataPass.getPassStartTime().toEpochSecond(ZoneOffset.UTC));
						billingInfo.setLbcEndDate(dataPass.getPassEndTime().toEpochSecond(ZoneOffset.UTC));

						billingInfo.setLbcDataUsed(BillingUtils.getDataUsageInMB((double) (dataPass.getVolume())));
						billingInfo.setLbcDataAvail(BillingUtils.getDataUsageInMB((double) (dataPass.getFup())));
					} else if (BillingCycle.PENULTIMATE == billCycle.getCurrentCycle()) {
						billingInfo.setPbcStartDate(dataPass.getPassStartTime().toEpochSecond(ZoneOffset.UTC));
						billingInfo.setPbcEndDate(dataPass.getPassEndTime().toEpochSecond(ZoneOffset.UTC));

						billingInfo.setPbcDataUsed(BillingUtils.getDataUsageInMB((double) (dataPass.getVolume())));
						billingInfo.setPbcDataAvail(BillingUtils.getDataUsageInMB((double) (dataPass.getFup())));
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
