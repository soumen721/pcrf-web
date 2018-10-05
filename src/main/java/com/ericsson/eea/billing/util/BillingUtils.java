package com.ericsson.eea.billing.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;

public class BillingUtils {

	public static XMLGregorianCalendar toXMLCalender(LocalDateTime date) throws DatatypeConfigurationException {

		GregorianCalendar gcal = GregorianCalendar.from(date.atZone(ZoneId.systemDefault()));
		return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
	}

	public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlDate) {

		return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
	}

	public static void printLog(DataPass dataPass) {

		System.out.println(dataPass.getInfoType() + " \t| "
				+ dataPass.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + " \t| "
				+ dataPass.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
				+ "  \t|Expiry_Reason \t| " + dataPass.getExpiryReason());
	}

	public static void printDataUsage(SubscriberBillingInfo billingInfo) {

		System.out.println("=======================================================================");
		System.out.println("Current Period Data Usage=>\n" + "DataUsed : "
				+ billingInfo.getDataUsed() + "\t| Data Avail : "
				+ billingInfo.getDataAvail() + "\t| Data Remaining : "
				+ billingInfo.getDataUsedShared());

		System.out.println("Previous Period Data Usage=>\n" + "DataUsed : "
				+ billingInfo.getLbcDataUsed() + "\t| Data Avail : "
				+ billingInfo.getLbcDataAvail() + "\t| Data Remaining : "
				+ billingInfo.getLbcDataUsedShared());

		System.out.println("Penultimate Period Data Usage=>\n" + "DataUsed : "
				+ billingInfo.getPbcDataUsed() + "\t| Data Avail : "
				+ billingInfo.getPbcDataAvail() + "\t| Data Remaining : "
				+ billingInfo.getPbcDataUsedShared());
	}
}
