package com.ee.cne.pcrf.servlet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;

public class FilterService {

	final List<String> INVALID_PASS_TYPE = Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");
	final List<String> VALID_INFO_TYPE = Arrays.asList("C", "S", "E", "ZR", "EZR");
	final List<String> EXPIRY_REASON = Arrays.asList("fup_change");
	final String VALID_ZONE = "1";

	public void getFilterData() throws DatatypeConfigurationException {

		GetCurrentAndAvailableDataProductsResponse response = DummyDataGenerator.pupulateResponseData();

		SubscriberInfo info = response.getMessage().getSubscriberInfo();
		System.out.println("MSISDN ID ==> " + info.getMsisdn());
		List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

		List<DataPass> billCycleDataPasses = filterDataPassOnFUPChange(getFilteredDataPassBasedonBillCycle(
				getFilteredDataPass(dataPasses), Integer.valueOf(info.getBillCycle().toString())));

		System.out.println("Final Pass after FUP remove");
		billCycleDataPasses.forEach(e -> {
			System.out.println(e.getInfoType() + " \t| "
					+ e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + " \t| "
					+ e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
					+ "  \t|Expiry_Reason \t| " + e.getExpiryReason());
		});
		
		printDataUsage(billCycleDataPasses, info);
	}

	// Get Filtered invalid PassType, InotType, Zone along and then sort based on
	// Start
	private List<DataPass> getFilteredDataPass(List<DataPass> dataPasses) {

		System.out.println("Before First Iteration ");
		dataPasses.forEach(e -> {
			System.out.println(e.getInfoType() + " \t| " + e.getPassType() + " \t| "
					+ e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + " \t| "
					+ e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
					+ "  \t|Expiry_Reason \t| " + e.getExpiryReason());
		});

		Comparator<? super DataPass> dateComparator = (e1, e2) -> e1.getPassStartTime().compare(e2.getPassStartTime());

		return dataPasses.stream().filter(pass -> !INVALID_PASS_TYPE.contains(pass.getPassType()))
				.filter(info -> VALID_INFO_TYPE.contains(info.getInfoType()))
				.filter(zone -> VALID_ZONE.equals(zone.getValidZone())).sorted(dateComparator.reversed())
				.collect(Collectors.toList());

	}

	private List<DataPass> getFilteredDataPassBasedonBillCycle(List<DataPass> dataPasses, final int billDate) {

		final LocalDateTime billEndDate = LocalDateTime.now();
		final LocalDateTime billStartDate = billEndDate.minusMonths(1).plusDays(1);

		System.out.println("Biling Start Date\t| " + billStartDate);
		System.out.println("Billing End Date\t| " + billEndDate);

		List<DataPass> list = dataPasses.stream()
				.filter(pass -> pass.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime()
						.isAfter(billStartDate)
						&& pass.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime()
								.isBefore(billEndDate))
				.collect(Collectors.toList());

		System.out.println("After Sorting ON Date\t|\t|");
		list.forEach(e -> {
			System.out.println(e.getInfoType() + " \t| "
					+ e.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + " \t| "
					+ e.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
					+ "  \t|Expiry_Reason \t| " + e.getExpiryReason());
		});

		return list;
	}

	private List<DataPass> filterDataPassOnFUPChange(List<DataPass> dataPasses) {

		List<DataPass> list = new ArrayList<>();
		for (DataPass dataPass : dataPasses) {
			if (EXPIRY_REASON.contains(dataPass.getExpiryReason())) {
				break;
			}
			list.add(dataPass);
		}

		return list;
	}

	private void printDataUsage(List<DataPass> dataPasses, final SubscriberInfo info) {
		System.out.println("Calculation begin here");
		dataPasses.forEach(e->{
			if("unlimited".equalsIgnoreCase(e.getExpiryReason())) {
				System.out.println("Unlimited Pass :\t" + e.getInfoType()+"\t|"+e.getExpiryReason());
			}
			if(info.getTypeOfAccess().contains("L") && e.getShareDetails() != null && e.getShareDetails().getSharerDataUsage().size() >0) {
				System.out.println("Its a Shaed Pass: \t" + e.getInfoType()+"\t|"+e.getExpiryReason());
			}
		});
	}
	
	public static void main(String arg[]) throws DatatypeConfigurationException {

		FilterService filterService = new FilterService();
		filterService.getFilterData();
	}

}
