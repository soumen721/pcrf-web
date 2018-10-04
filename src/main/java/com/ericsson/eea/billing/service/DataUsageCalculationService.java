package com.ericsson.eea.billing.service;

import javax.xml.datatype.DatatypeConfigurationException;

public interface DataUsageCalculationService {

	void calculateDataUsage() throws DatatypeConfigurationException;

}
