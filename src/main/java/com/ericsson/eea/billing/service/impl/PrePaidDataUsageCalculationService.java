package com.ericsson.eea.billing.service.impl;

import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.service.DataUsageCalculationService;

import javax.xml.datatype.DatatypeConfigurationException;

public class PrePaidDataUsageCalculationService implements DataUsageCalculationService {

    @Override
    public SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response) throws DatatypeConfigurationException {

        System.out.println("Calculate PrePaid Usage");
        return null;
    }

}
