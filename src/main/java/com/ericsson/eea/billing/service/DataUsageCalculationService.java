package com.ericsson.eea.billing.service;

import com.ericsson.eea.billing.model.SubscriberBillingInfo;

import javax.xml.datatype.DatatypeConfigurationException;

public interface DataUsageCalculationService {

	SubscriberBillingInfo calculateDataUsage() throws DatatypeConfigurationException;

}
