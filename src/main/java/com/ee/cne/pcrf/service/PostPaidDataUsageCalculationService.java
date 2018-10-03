package com.ee.cne.pcrf.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;

import com.ee.cne.pcrf.util.DummyDataGenerator;
import com.ee.cne.pcrf.util.PCRFConstant;
import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;

public class PostPaidDataUsageCalculationService implements DataUsageCalculationService {

	@Override
	public void calculateDataUsage() throws DatatypeConfigurationException {

		GetCurrentAndAvailableDataProductsResponse response = DummyDataGenerator.pupulateResponseData();

		SubscriberInfo info = response.getMessage().getSubscriberInfo();
		System.out.println("MSISDN ID ==> " + info.getMsisdn());
		List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

		List<DataPass> filteredDataPasses = getFilteredDataPass(dataPasses);

		// For Current Period
		System.out.println("*************************	Current Period	****************************");
		LocalDateTime billCycleEndDate = LocalDate.now().atStartOfDay();
		LocalDateTime billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
		List<DataPass> currentCycleDataPasses = filterDataPassOnFUPChange(
				getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

		System.out.println("Final Pass after FUP remove Current Cycle");
		currentCycleDataPasses.forEach(e -> System.out.println(
				e.getInfoType() + " \t| " + e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ " \t| " + e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ "  \t|Expiry_Reason \t| " + e.getExpiryReason()));

		printDataUsage(currentCycleDataPasses, info);

		// For Previous Period
		System.out.println("*************************	Previous Period	****************************");
		billCycleEndDate = billCycleEndDate.minusMonths(1);
		billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
		List<DataPass> previousCycleDataPasses = filterDataPassOnFUPChange(
				getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

		System.out.println("Final Pass after FUP remove -- Previous Cycle");
		previousCycleDataPasses.forEach(e -> System.out.println(
				e.getInfoType() + " \t| " + e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ " \t| " + e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ "  \t|Expiry_Reason \t| " + e.getExpiryReason()));

		printDataUsage(previousCycleDataPasses, info);

		// For Penultimate Period
		System.out.println("*************************	Penultimate Period	****************************");
		billCycleEndDate = billCycleEndDate.minusMonths(1);
		billCycleStartDate = billCycleEndDate.minusMonths(1).plusDays(1);
		List<DataPass> penultimateCycleDataPasses = filterDataPassOnFUPChange(
				getFilteredDataPassBasedOnBillCycle(filteredDataPasses, billCycleStartDate, billCycleEndDate));

		System.out.println("Final Pass after FUP remove -- Previous Cycle");
		penultimateCycleDataPasses.forEach(e -> System.out.println(
				e.getInfoType() + " \t| " + e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ " \t| " + e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ "  \t|Expiry_Reason \t| " + e.getExpiryReason()));

		printDataUsage(penultimateCycleDataPasses, info);

	}

	// Get Filtered invalid PassType, InotType, Zone along and then sort based on
	// Start
	private List<DataPass> getFilteredDataPass(List<DataPass> dataPasses) {

		System.out.println("Before First Iteration ");
		dataPasses.forEach(e -> System.out.println(e.getInfoType() + " \t| " + e.getPassType() + " \t| "
				+ e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + " \t| "
				+ e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + "  \t|Expiry_Reason \t| "
				+ e.getExpiryReason()));

		Comparator<? super DataPass> dateComparator = (e1, e2) -> e1.getPassStartTime().compare(e2.getPassStartTime());

		return dataPasses.stream().filter(pass -> !PCRFConstant.INVALID_PASS_TYPE.contains(pass.getPassType()))
				.filter(info -> PCRFConstant.VALID_INFO_TYPE.contains(info.getInfoType()))
				.filter(zone -> PCRFConstant.VALID_ZONE.equals(zone.getValidZone())).sorted(dateComparator.reversed())
				.collect(Collectors.toList());

	}

	private List<DataPass> getFilteredDataPassBasedOnBillCycle(final List<DataPass> dataPasses,
			final LocalDateTime billCycleStartDate, final LocalDateTime billCycleEndDate) {

		System.out.println("Bill Cycle Start Date =>\t " + billCycleStartDate.toLocalDate());
		System.out.println("Bill Cycle End Date=>\t " + billCycleEndDate.toLocalDate());

		List<DataPass> list = dataPasses.stream()
				.filter(pass -> (DummyDataGenerator.toLocalDateTime(pass.getPassStartTime()).isAfter(billCycleStartDate)
						|| DummyDataGenerator.toLocalDateTime(pass.getPassStartTime()).isEqual(billCycleStartDate))
						&& (DummyDataGenerator.toLocalDateTime(pass.getPassEndTime()).isBefore(billCycleEndDate))
						|| DummyDataGenerator.toLocalDateTime(pass.getPassEndTime()).isEqual(billCycleEndDate))
				.collect(Collectors.toList());

		System.out.println("After Sorting ON Date\t|\t|");
		list.forEach(e -> System.out.println(
				e.getInfoType() + " \t| " + e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ " \t| " + e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
						+ "  \t|Expiry_Reason \t| " + e.getExpiryReason()));

		return list;
	}

	private List<DataPass> filterDataPassOnFUPChange(List<DataPass> dataPasses) {

		List<DataPass> list = new ArrayList<>();
		for (DataPass dataPass : dataPasses) {
			if (PCRFConstant.EXPIRY_REASON.contains(dataPass.getExpiryReason())) {
				break;
			}
			list.add(dataPass);
		}

		return list;
	}

	private void printDataUsage(List<DataPass> dataPasses, final SubscriberInfo info) {

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

		System.out.println("*********************************************************************");
		System.out.println("Total Data Used = " + (dataUsed_ult + dataUsed_sha + dataUsed_nonSha));
		System.out.println("Data Avail = " + data_avl);
		System.out.println("Data Remaining = " + (data_avl - (dataUsed_nonSha + dataUsed_shaGrp)));

	}

	public static void main(String arg[]) throws DatatypeConfigurationException {

		DataUsageCalculationService filterService = new PostPaidDataUsageCalculationService();
		filterService.calculateDataUsage();
	}

}
