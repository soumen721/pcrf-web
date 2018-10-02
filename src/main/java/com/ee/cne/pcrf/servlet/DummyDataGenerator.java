package com.ee.cne.pcrf.servlet;

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

		DataPass dataPass = populateDataPass("C", "10GB Data EE HS EOBC", LocalDateTime.now().minusDays(15),
				LocalDateTime.now().minusDays(4), 125872138L, 8715474912L, "virtual");
		DataPass dataPass1 = populateDataPass("E", "AFUP_CON_HS_UKEU_DCC", LocalDateTime.now().minusDays(20),
				LocalDateTime.now().minusDays(12), 287161212L, 213123123L, "unlimited");
		DataPass dataPass2 = populateDataPass("E", "EE AutoFUP HS_11GB", LocalDateTime.now().minusDays(23),
				LocalDateTime.now().minusDays(15), 287161212L, 213123123L, "virtual");
		DataPass dataPass3 = populateDataPass("E", "EE AutoFUP HS_100GB", LocalDateTime.now().minusDays(27),
				LocalDateTime.now().minusDays(15), 287161212L, 213123123L, "fup_change");
		DataPass dataPass4 = populateDataPass("E", "EE AutoFUP HS_11GB", LocalDateTime.now().minusDays(28),
				LocalDateTime.now().minusDays(15), 287161212L, 213123123L, "virtual");

		dataProduct.getDataProduct().addAll(Arrays.asList(dataPass, dataPass1, dataPass2, dataPass3, dataPass4));

		message.setDataProducts(dataProduct);
		response.setMessage(message);

		return response;
	}

	private static DataPass populateDataPass(String infoType, String passType, LocalDateTime startDate,
			LocalDateTime endDate, Long volume, Long fup, String exp_reason)
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
		usage.setUsedVolume(87216921L);
		SharerDataUsage usage1 = new SharerDataUsage();
		usage1.setMsisdn("126321321333");
		usage1.setUsedVolume(123213213L);

		share.getSharerDataUsage().addAll(Arrays.asList(usage, usage1));
		dataPass.setShareDetails(share);

		return dataPass;
	}

	private static XMLGregorianCalendar toXMLCalender(LocalDateTime date) throws DatatypeConfigurationException {

		GregorianCalendar gcal = GregorianCalendar.from(date.atZone(ZoneId.systemDefault()));
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}

}
