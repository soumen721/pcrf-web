package com.ericsson.eea.billing.service.impl;

import javax.xml.datatype.DatatypeConfigurationException;

import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.service.DataUsageCalculationService;

public class PrePaidDataUsageCalculationService implements DataUsageCalculationService {

	@Override
	public SubscriberBillingInfo calculateDataUsage() throws DatatypeConfigurationException {

		System.out.println("Calculate PrePaid Usage");
		return null;
	}

}
