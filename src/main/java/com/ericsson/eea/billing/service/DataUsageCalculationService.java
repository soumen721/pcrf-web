package com.ericsson.eea.billing.service;

import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;

public interface DataUsageCalculationService {

    SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response)
            throws SubscriberBillingRetrievalFailedException, SubscriberBillingInfoNotAvailableException;

}
