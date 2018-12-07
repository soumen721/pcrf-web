package com.ericsson.eea.billing.service;

import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;

/**
 * @author esonchy
 *
 */
public interface DataUsageCalculationService {

	/**
	 * @param response
	 * @return
	 * @throws SubscriberBillingRetrievalFailedException
	 * @throws SubscriberBillingInfoNotAvailableException
	 */
	SubscriberBillingInfo calculateDataUsage(GetCurrentAndAvailableDataProductsResponse response)
			throws SubscriberBillingRetrievalFailedException, SubscriberBillingInfoNotAvailableException;

}
