package com.ericsson.eea.billing.service.impl;

import javax.ejb.Remote;
import javax.ejb.Stateless;

import com.ericsson.eea.billing.model.MessageEnvelope;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.model.SubscriberFilter;
import com.ericsson.eea.billing.service.SubscriberBillingRemote;

@Stateless
@Remote(SubscriberBillingRemote.class)
public class SubscriberBillingInfoImpl implements SubscriberBillingRemote {

	@Override
	public MessageEnvelope<SubscriberBillingInfo> getBillingCycleInfo(SubscriberFilter filter)
			throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {
		
		// call PCRF web service and process and return response
		return null;
	}

}
