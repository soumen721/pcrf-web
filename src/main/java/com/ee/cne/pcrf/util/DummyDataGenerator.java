package com.ee.cne.pcrf.util;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails;
import com.ee.cne.ws.dataproduct.generated.DataPass.ShareDetails.SharerDataUsage;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.DataProducts;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ee.cne.ws.dataproduct.generated.SubscriberInfo.Location;

public class DummyDataGenerator {

	/**
	 * @return
	 * @throws DatatypeConfigurationException
	 */
	private final static String MSISDN = "1237129838123";
	private final static int DAY_IN_MNTH = 30;
	private final static int CUR_MONTH_CYCLE = 0;
	private final static int PREV_MONTH_CYCLE = 1;
	private final static int PENLTY_MONTH_CYCLE = 2;

	public static GetCurrentAndAvailableDataProductsResponse pupulateResponseData()
			throws DatatypeConfigurationException {

		GetCurrentAndAvailableDataProductsResponse response = new GetCurrentAndAvailableDataProductsResponse();

		Message message = new Message();
		SubscriberInfo info = new SubscriberInfo();
		info.setMsisdn(MSISDN);
		info.setTypeOfAccess("DL");
		info.setBillCycle(BigInteger.valueOf(24));
		info.setCustomerType("NEXUS");
		info.setLastCheckedDate(toXMLCalender(LocalDateTime.now()));
		info.setTariffType("Postpaid");
		Location localtion = new Location();
		localtion.setCountry("UK");
		localtion.setZone("1");
		info.setLocation(localtion);

		message.setSubscriberInfo(info);

		DataProducts dataProduct = new DataProducts();

		DataPass dataPass = populateDataPass("C", "10GB Data EE HS EOBC",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 15),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 4), 125872138L, 10737418240L, "virtual",
				new Long[] { 8667008000L, 2942488576L });
		DataPass dataPass1 = populateDataPass("E", "AFUP_CON_HS_UKEU_DCC",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 20),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 12), 287161212L, 11811161088L,
				"unlimited", new Long[] { 8960598016L, 2852495360L });
		DataPass dataPass2 = populateDataPass("E", "EE AutoFUP HS_11GB",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 28),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 14), 287161212L, 10737418240L, "virtual",
				new Long[] { 7904909312L, 7904932423L });
		DataPass dataPass3 = populateDataPass("E", "EE AutoFUP HS_100GB",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 27),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 16), 287161212L, 213123123L, "fup_change",
				new Long[] { 790490931L, 7904242412L });
		DataPass dataPass4 = populateDataPass("E", "EE AutoFUP HS_11GB",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 23),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * CUR_MONTH_CYCLE + 18), 287161212L, 213123123L, "virtual",
				new Long[] { 241242414L, 79543559312L });

		DataPass dataPass10 = populateDataPass("E", "10GB Data EE HS EOBC",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PREV_MONTH_CYCLE + 15),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PREV_MONTH_CYCLE + 4), 125872138L, 10737418240L, "virtual",
				new Long[] { 8667008000L, 2942488576L });
		DataPass dataPass11 = populateDataPass("E", "AFUP_CON_HS_UKEU_DCC",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PREV_MONTH_CYCLE + 20),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PREV_MONTH_CYCLE + 12), 287161212L, 11811161088L, "virtual",
				new Long[] { 8960598016L, 2852495360L });

		DataPass dataPass20 = populateDataPass("E", "10GB Data EE HS EOBC",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PENLTY_MONTH_CYCLE + 15),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PENLTY_MONTH_CYCLE + 4), 125872138L, 10737418240L,
				"virtual", new Long[] { 8667008000L, 2942488576L });
		DataPass dataPass21 = populateDataPass("E", "AFUP_CON_HS_UKEU_DCC",
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PENLTY_MONTH_CYCLE + 31),
				LocalDateTime.now().minusDays(DAY_IN_MNTH * PENLTY_MONTH_CYCLE + 12), 287161212L, 11811161088L,
				"virtual", new Long[] { 8960598016L, 2852495360L });

		dataProduct.getDataProduct().addAll(Arrays.asList(dataPass, dataPass1, dataPass2, dataPass3, dataPass4,
				dataPass10, dataPass11, dataPass20, dataPass21));

		message.setDataProducts(dataProduct);
		response.setMessage(message);

		return response;
	}

	private static DataPass populateDataPass(String infoType, String passType, LocalDateTime startDate,
			LocalDateTime endDate, Long volume, Long fup, String exp_reason, Long[] useVol)
			throws DatatypeConfigurationException {

		DataPass dataPass = new DataPass();
		dataPass.setInfoType(infoType);
		dataPass.setPassType(passType);
		dataPass.setPassStartTime(toXMLCalender(startDate));
		dataPass.setPassEndTime(toXMLCalender(endDate));
		dataPass.setVolume(volume);
		dataPass.setExpiryReason(exp_reason);
		dataPass.setFup(fup);
		dataPass.setSalesChannel("");
		dataPass.setValidZone("1");

		ShareDetails share = new ShareDetails();
		share.setOriginatorMsisdn(MSISDN);

		SharerDataUsage usage = new SharerDataUsage();
		usage.setMsisdn(MSISDN);
		usage.setUsedVolume(useVol[0]);
		SharerDataUsage usage1 = new SharerDataUsage();
		usage1.setMsisdn("126321321333");
		usage1.setUsedVolume(useVol[1]);

		share.getSharerDataUsage().addAll(Arrays.asList(usage, usage1));
		dataPass.setShareDetails(share);

		return dataPass;
	}

	private static XMLGregorianCalendar toXMLCalender(LocalDateTime date) throws DatatypeConfigurationException {

		GregorianCalendar gcal = GregorianCalendar.from(date.atZone(ZoneId.systemDefault()));
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}

	public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlDate) {

		return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}

}
