package com.ee.cne.pcrf.util;

import java.math.BigInteger;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;

public class TestMain {

	public static void main(String[] args) {
		LocalDateTime today = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
		System.out.println("Todat "+ today);
		LocalDateTime currentMonthBillDate = today.withDayOfMonth(1);
		
		
		final LocalDateTime billStartDate = today.isBefore(currentMonthBillDate) ? currentMonthBillDate : currentMonthBillDate.plusMonths(1) ;
		
		final LocalDateTime billEndDate = billStartDate.minusMonths(1);
		
		//today.withDayOfMonth(Integer.valueOf(12+""));
		
		System.out.println("Start Date: "+ billStartDate);
		System.out.println("End Date: "+ billEndDate);
	}

}
