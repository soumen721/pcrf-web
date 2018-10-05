package com.ericsson.eea.billing.service;

import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;

import javax.xml.datatype.DatatypeConfigurationException;

public interface DataUsageCalculationService {

    SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response) throws DatatypeConfigurationException;

}
