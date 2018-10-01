package com.ee.cne.pcrf.servlet;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;

public class FilterService {

	final List<String> INVALID_PASS_TYPE = Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");
	final List<String> VALID_INFO_TYPE = Arrays.asList("C", "S", "E", "ZR", "EZR");
	final List<String> EXPIRY_REASON = Arrays.asList("fup_change");
	final String VALID_ZONE = "1";

	public void getFilterData() {

		GetCurrentAndAvailableDataProductsResponse response = new GetCurrentAndAvailableDataProductsResponse();

		SubscriberInfo info = response.getMessage().getSubscriberInfo();
		System.out.println("Info Details :: " + info.getMsisdn());
		List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

		// List<DataPass> filteredDataPasses = getFilteredDataPass(dataPasses);
		List<DataPass> billCycleDataPasses = getFilteredDataPassBasedonBillCycle(getFilteredDataPass(dataPasses),
				Integer.valueOf(info.getBillCycle().toString()));

	}

	// Get Filtered invalid PassType, InotType, Zone along and then sort based on
	// Start
	private List<DataPass> getFilteredDataPass(List<DataPass> dataPasses) {

		return dataPasses.stream().filter(pass -> !INVALID_PASS_TYPE.contains(pass.getPassType()))
				.filter(info -> VALID_INFO_TYPE.contains(info.getInfoType()))
				.filter(zone -> VALID_ZONE.equals(zone.getValidZone()))
				// .sorted(Comparator.comparing(DataPass::getCreatedOn))
				.sorted((e1, e2) -> e1.getPassStartTime().compare(e2.getPassStartTime())).collect(Collectors.toList());

	}

	private List<DataPass> getFilteredDataPassBasedonBillCycle(List<DataPass> dataPasses, final int billDate) {

		final LocalDateTime billEndDate = LocalDateTime.now();
		final LocalDateTime billStartDate = billEndDate.minusMonths(1).plusDays(1);

		System.out.println("Start Date: " + billStartDate);
		System.out.println("End Date: " + billEndDate);

		List<DataPass> dataPasses2 = dataPasses.stream()
				.filter(pass -> pass.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime()
						.isAfter(billStartDate)
						&& pass.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDateTime()
								.isBefore(billEndDate))
				.collect(Collectors.toList());

		return dataPasses2;
	}

}
