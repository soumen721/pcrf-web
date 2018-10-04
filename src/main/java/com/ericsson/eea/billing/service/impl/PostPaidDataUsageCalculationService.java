package com.ericsson.eea.billing.service.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.xml.datatype.DatatypeConfigurationException;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ee.cne.ws.getctxwithoperations.generated.Calculator;
import com.ee.cne.ws.getctxwithoperations.generated.CalculatorSoap;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.util.BillingConstant;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.billing.util.DummyDataGenerator;
import com.ericsson.eea.pcrf.model.DataUsageDetails;
import com.ericsson.eea.pcrf.model.PeriodicalDataUsage;

public class PostPaidDataUsageCalculationService implements DataUsageCalculationService {

	@Override
	public void calculateDataUsage() throws DatatypeConfigurationException {

		GetCurrentAndAvailableDataProductsResponse response = DummyDataGenerator.pupulateResponseData();

		SubscriberInfo info = response.getMessage().getSubscriberInfo();
		System.out.println("MSISDN ID ==> " + info.getMsisdn());
		List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();

		List<DataPass> filteredDataPasses = getFilteredDataPass(dataPasses);

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

		
		PeriodicalDataUsage periodicalDataUsage =  PeriodicalDataUsage.builder()
				.currentCycleDataUsage(currentCycleDataUsage)
				.previousCycleDataUsage(previousCycleDataUsage)
				.penultimateCycleDataUsage(penultimateCycleDataUsage)
				.build() ;
		
		BillingUtils.printDataUsage(periodicalDataUsage);
	}

	// Get Filtered invalid PassType, InotType, Zone along and then sort based on
	// Start Date
	private List<DataPass> getFilteredDataPass(List<DataPass> dataPasses) {

		System.out.println("Before First Iteration ");
		dataPasses.forEach(BillingUtils::printLog);

		Comparator<? super DataPass> dateComparator = (e1, e2) -> e1.getPassStartTime().compare(e2.getPassStartTime());

		return dataPasses.stream().filter(pass -> !BillingConstant.INVALID_PASS_TYPE.contains(pass.getPassType()))
				.filter(info -> BillingConstant.VALID_INFO_TYPE.contains(info.getInfoType()))
				.filter(zone -> BillingConstant.VALID_ZONE.equals(zone.getValidZone())).sorted(dateComparator.reversed())
				.collect(Collectors.toList());

	}

	private List<DataPass> getFilteredDataPassBasedOnBillCycle(final List<DataPass> dataPasses,
			final LocalDateTime billCycleStartDate, final LocalDateTime billCycleEndDate) {

		System.out.println("Bill Cycle Start Date =>\t " + billCycleStartDate.toLocalDate());
		System.out.println("Bill Cycle End Date=>\t " + billCycleEndDate.toLocalDate());

		List<DataPass> list = dataPasses.stream()
				.filter(pass -> (BillingUtils.toLocalDateTime(pass.getPassStartTime()).isAfter(billCycleStartDate)
						|| BillingUtils.toLocalDateTime(pass.getPassStartTime()).isEqual(billCycleStartDate))
						&& (BillingUtils.toLocalDateTime(pass.getPassEndTime()).isBefore(billCycleEndDate))
						|| BillingUtils.toLocalDateTime(pass.getPassEndTime()).isEqual(billCycleEndDate))
				.collect(Collectors.toList());

		System.out.println("After Sorting ON Date\t|\t|");
		list.forEach(BillingUtils::printLog);

		return list;
	}

	private List<DataPass> filterDataPassOnFUPChange(List<DataPass> dataPasses) {

		List<DataPass> list = new ArrayList<>();
		for (DataPass dataPass : dataPasses) {
			if (BillingConstant.EXPIRY_REASON.contains(dataPass.getExpiryReason())) {
				break;
			}
			list.add(dataPass);
		}

		return list;
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
		System.out.println("Data Remaining = " + data_rem );

		return DataUsageDetails.builder()
				.dataUsed(dataUsed_sha)
				.dataAvail(data_avl)
				.dataRemaining(data_rem)
				.build(); 
	}

	public static void main(String arg[]) throws DatatypeConfigurationException, MalformedURLException {
		//Calling a Dummy Wen Service
		URL wsdlURL = new URL("http://www.dneonline.com/calculator.asmx?wsdl");
		Calculator service = new Calculator(wsdlURL);
		CalculatorSoap ctxport = service.getCalculatorSoap();
		int resp = ctxport.add(10, 20);
		System.out.println("Addition Result ==> "+ resp);

		DataUsageCalculationService filterService = new PostPaidDataUsageCalculationService();
		filterService.calculateDataUsage();
	}

}
