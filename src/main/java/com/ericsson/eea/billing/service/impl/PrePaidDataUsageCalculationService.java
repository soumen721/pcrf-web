package com.ericsson.eea.billing.service.impl;

import javax.xml.datatype.DatatypeConfigurationException;

import com.ericsson.eea.billing.service.DataUsageCalculationService;

public class PrePaidDataUsageCalculationService implements DataUsageCalculationService {

	@Override
	public void calculateDataUsage() throws DatatypeConfigurationException {

		System.out.println("Calculate PrePaid Usage");
	}

}
