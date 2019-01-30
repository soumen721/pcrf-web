package com.ericsson.eea.billing.service.impl;

import java.util.Collections;
import javax.ejb.EJB;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.MessageEnvelope;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.model.SubscriberFilter;
import com.ericsson.eea.billing.model.SubscriberId;
import com.ericsson.eea.billing.model.SubscriberIdType;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.service.SubscriberBillingRemote;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.billing.util.TariffType;
import com.ericsson.eea.billing.ws.client.DataProductsClient;

/**
 * @author esonchy
 *
 */
@Stateless
@Remote(SubscriberBillingRemote.class)
@EJB(name = "java:jboss/pcrfService", beanInterface = SubscriberBillingRemote.class)  
public class SubscriberBillingInfoImpl implements SubscriberBillingRemote {
	private static final Logger log = Logger.getLogger(SubscriberBillingInfoImpl.class);

	/* (non-Javadoc)
	 * @see com.ericsson.eea.billing.service.SubscriberBillingRemote#getBillingCycleInfo(com.ericsson.eea.billing.model.SubscriberFilter)
	 */
	@Override
	public MessageEnvelope<SubscriberBillingInfo> getBillingCycleInfo(SubscriberFilter filter)
			throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

		GetCurrentAndAvailableDataProductsResponse response = DataProductsClient
				.getDataProductsWebServiceResponse(filter);

		if (response != null && response.getMessage() != null && response.getMessage().getSubscriberInfo() != null) {

			GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo subscriberInfo = response.getMessage()
					.getSubscriberInfo();
			DataUsageCalculationService usageCalculationService;

			if (TariffType.PREPAID.getType().equals(subscriberInfo.getTariffType())) {

				usageCalculationService = new PrePaidDataUsageCalculationService();
			} else if (TariffType.POSTPAID.getType().equals(subscriberInfo.getTariffType())) {

				usageCalculationService = new PostPaidDataUsageCalculationService();
			} else {
				throw new SubscriberBillingInfoNotAvailableException();
			}

			SubscriberBillingInfo billingInfo = usageCalculationService.calculateDataUsage(response);
			billingInfo = BillingUtils.populateResponseBasicDetails(billingInfo, subscriberInfo);
			MessageEnvelope<SubscriberBillingInfo> envelope = new MessageEnvelope<>();
			envelope.setData(Collections.singletonList(billingInfo));

			return envelope;
		} else {
			log.error("Error in retriving Reposne");
			throw new SubscriberBillingInfoNotAvailableException();
		}

	}

	public static void main(String arg[])
			throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

		SubscriberFilter filter = new SubscriberFilter();
		SubscriberId id = new SubscriberId();
		id.setId("447432993984");
		id.setIdType(SubscriberIdType.msisdn);
		filter.setId(id);
		new SubscriberBillingInfoImpl().getBillingCycleInfo(filter);
	}

}
