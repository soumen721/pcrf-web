package com.ericsson.eea.billing.service;

import javax.ejb.Remote;

import com.ericsson.eea.billing.model.MessageEnvelope;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.model.SubscriberFilter;

@Remote
public interface SubscriberBillingRemote {

    /**
     * @param filter
     *            - This Contains the Subscriber Details and Time Range for
     *            which Billing Data is Requested
     * @return SubscriberBillingInfo Containing the Requested Billing
     *         Information for the Subscriber
     * @throws SubscriberBillingInfoNotAvailableException
     *             when Billing info is not available for the Subscriber
     * @throws SubscriberBillingRetrievalFailedException
     *             when anyother error happens which prevents Billing Info
     *             retrieval
     */
    public MessageEnvelope<SubscriberBillingInfo> getBillingCycleInfo(SubscriberFilter filter)
            throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException;

}
