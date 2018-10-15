package com.ericsson.eea.billing.service.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.DataProduct;
import com.ee.cne.ws.dataproduct.generated.DataProductService;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsRequest;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.ObjectFactory;
import com.ericsson.eea.billing.model.MessageEnvelope;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.model.SubscriberFilter;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.service.SubscriberBillingRemote;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.billing.util.TariffType;

@Stateless
@Remote(SubscriberBillingRemote.class)
public class SubscriberBillingInfoImpl implements SubscriberBillingRemote {
  private static final Logger log = Logger.getLogger(SubscriberBillingInfoImpl.class);

  @Override
  public MessageEnvelope<SubscriberBillingInfo> getBillingCycleInfo(SubscriberFilter filter)
      throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    // call PCRF web service and process and return response
    GetCurrentAndAvailableDataProductsResponse response = getDataProductsWebServiceResponse();
    GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo subscriberInfo =
        response.getMessage().getSubscriberInfo();
    DataUsageCalculationService usageCalculationService;

    if (TariffType.Prepaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PrePaidDataUsageCalculationService();
    } else if (TariffType.Postpaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PostPaidDataUsageCalculationService();
    } else {
      throw new SubscriberBillingInfoNotAvailableException();
    }

    SubscriberBillingInfo billingInfo = usageCalculationService.calculateDataUsage(response);
    MessageEnvelope<SubscriberBillingInfo> envelope = new MessageEnvelope<>();
    envelope.setData(Collections.singletonList(billingInfo));

    return envelope;
  }

  public static void main(String arg[])
      throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    GetCurrentAndAvailableDataProductsResponse response = getDataProductsWebServiceResponse();
    GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo subscriberInfo =
        response.getMessage().getSubscriberInfo();
    DataUsageCalculationService usageCalculationService;

    if (TariffType.Prepaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PrePaidDataUsageCalculationService();
    } else if (TariffType.Postpaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PostPaidDataUsageCalculationService();
    } else {
      throw new SubscriberBillingInfoNotAvailableException();
    }
    usageCalculationService.calculateDataUsage(response);
  }

  private static GetCurrentAndAvailableDataProductsResponse getDataProductsWebServiceResponse()
      throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    GetCurrentAndAvailableDataProductsResponse response = null;
    try {
      URL wsdlURL =
          new URL(BillingUtils.getProperties().getProperty(BillingUtils.PCRF_BILLING_WS_URL));
      log.info("BIlling WebService URL :: " + wsdlURL.toURI().toString());

      // TODO remove once got actual Response
      //response = DummyDataGenerator.populateResponseData();
      if (response == null) {
        DataProductService dataService = new DataProductService(wsdlURL);
        DataProduct port = dataService.getDataProduct10();
        GetCurrentAndAvailableDataProductsRequest request =
            new ObjectFactory().createGetCurrentAndAvailableDataProductsRequest();

        // set msisdn and request origin in the request and call service
        response = port.getCurrentAndAvailableDataProducts(request);
      }

    } catch (MalformedURLException e) {

      log.error("Exception Occured :: " + e.getMessage());
      throw new SubscriberBillingInfoNotAvailableException();
    } catch ( URISyntaxException e) {

      log.error("Exception Occured :: " + e.getMessage());
      throw new SubscriberBillingRetrievalFailedException();
    } catch (Exception e) {

      log.error("Exception Occured :: " + e.getMessage());
      // throw new SubscriberBillingRetrievalFailedException();
    }

    if (response == null || response.getMessage() == null
        || response.getMessage().getSubscriberInfo() == null) {
      throw new SubscriberBillingInfoNotAvailableException();
    }

    return response;
  }
}
