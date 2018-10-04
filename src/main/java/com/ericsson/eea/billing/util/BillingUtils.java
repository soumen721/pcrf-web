package com.ericsson.eea.billing.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ericsson.eea.pcrf.model.PeriodicalDataUsage;

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

	public static void printDataUsage(PeriodicalDataUsage periodicalDataUsage) {

		System.out.println("=======================================================================");
		System.out.println("Current Period Data Usage=>\n" + "DataUsed : "
				+ periodicalDataUsage.getCurrentCycleDataUsage().getDataUsed() + "\t| Data Avail : "
				+ periodicalDataUsage.getCurrentCycleDataUsage().getDataAvail() + "\t| Data Remaining : "
				+ periodicalDataUsage.getCurrentCycleDataUsage().getDataRemaining());

		System.out.println("Previous Period Data Usage=>\n" + "DataUsed : "
				+ periodicalDataUsage.getPreviousCycleDataUsage().getDataUsed() + "\t| Data Avail : "
				+ periodicalDataUsage.getPreviousCycleDataUsage().getDataAvail() + "\t| Data Remaining : "
				+ periodicalDataUsage.getPreviousCycleDataUsage().getDataRemaining());

		System.out.println("Penultimate Period Data Usage=>\n" + "DataUsed : "
				+ periodicalDataUsage.getPenultimateCycleDataUsage().getDataUsed() + "\t| Data Avail : "
				+ periodicalDataUsage.getPenultimateCycleDataUsage().getDataAvail() + "\t| Data Remaining : "
				+ periodicalDataUsage.getPenultimateCycleDataUsage().getDataRemaining());
	}
}
