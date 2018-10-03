package com.ee.cne.pcrf.service;

import javax.xml.datatype.DatatypeConfigurationException;

public class PrePaidDataUsageCalculationService2 implements DataUsageCalculationService {

	@Override
	public void calculateDataUsage() throws DatatypeConfigurationException {

		System.out.println("Calculate PrePaid Usage");
	}

}
