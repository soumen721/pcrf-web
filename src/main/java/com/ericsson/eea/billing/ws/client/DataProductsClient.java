package com.ericsson.eea.billing.ws.client;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.DataProduct;
import com.ee.cne.ws.dataproduct.generated.DataProductBasicRequest.KeyIdentifier;
import com.ee.cne.ws.dataproduct.generated.DataProductService;
import com.ee.cne.ws.dataproduct.generated.EIMessageContext2;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsRequest;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsRequest.Message;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.ObjectFactory;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.model.SubscriberFilter;
import com.ericsson.eea.billing.model.SubscriberIdType;
import com.ericsson.eea.billing.util.BillingConstant;
import com.ericsson.eea.billing.util.BillingUtils;

public class DataProductsClient {
  private static final Logger log = Logger.getLogger(DataProductsClient.class);

  public static GetCurrentAndAvailableDataProductsResponse getDataProductsWebServiceResponse(
      SubscriberFilter filter)
      throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    GetCurrentAndAvailableDataProductsResponse response = null;
    try {
      // TODO remove once got actual Response
      response = GenerateResponse.getDummyResponse();
      if (response == null) {
        URL wsdlURL =
            new URL(BillingUtils.getProperties().getProperty(BillingUtils.PCRF_BILLING_WS_URL));
        log.info("BIlling WebService URL :: " + wsdlURL.toURI().toString());

        GetCurrentAndAvailableDataProductsRequest request =
            new ObjectFactory().createGetCurrentAndAvailableDataProductsRequest();

        if (filter != null && filter.getId() != null) {
          EIMessageContext2 messageContext = new EIMessageContext2();
          messageContext.setTarget("pdf");
          messageContext.setTimeLeft(200L);
          messageContext.setSender(BillingConstant.EEA_SENDER_ID);
          messageContext.setCorrelationId(UUID.randomUUID().toString());
          request.setEiMessageContext2(messageContext);

          String msisdn = null;
          if (SubscriberIdType.msisdn == filter.getId().getIdType()) {
            msisdn = filter.getId().getId();
          } else {
            log.error("No MSISDN subscriberType found");
            throw new SubscriberBillingRetrievalFailedException();
          }

          Message message = new Message();
          KeyIdentifier identifier = new KeyIdentifier();
          identifier.setMsisdn(msisdn);
          message.setKeyIdentifier(identifier);
          message.setRequestOrigin(BillingConstant.EEA_SENDER_ID);
          request.setMessage(message);
        } else {
          log.error("NO Id found for retriveing Billing");
          throw new SubscriberBillingRetrievalFailedException();
        }

        DataProductService dataService = new DataProductServiceImpl(wsdlURL);
        DataProduct port = dataService.getDataProduct10();
        response = port.getCurrentAndAvailableDataProducts(request);
      }

    } catch (MalformedURLException e) {

      log.error("Exception Occured :: " + e.getMessage());
      throw new SubscriberBillingInfoNotAvailableException();
    } catch (URISyntaxException e) {

      log.error("Exception Occured :: " + e.getMessage());
      throw new SubscriberBillingRetrievalFailedException();
    } catch (Exception e) {

      log.error("Exception Occured :: " + e.getMessage());
      throw new SubscriberBillingRetrievalFailedException();
    }

    if (response == null || response.getMessage() == null
        || response.getMessage().getSubscriberInfo() == null) {
      throw new SubscriberBillingInfoNotAvailableException();
    }

    return response;
  }
}
