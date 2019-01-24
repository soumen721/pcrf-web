package com.ericsson.eea.billing.ws.client;

import java.util.Arrays;
import java.util.UUID;
import org.apache.cxf.frontend.ClientFactoryBean;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.BusinessFault;
import com.ee.cne.ws.dataproduct.generated.DataProduct;
import com.ee.cne.ws.dataproduct.generated.DataProductBasicRequest.KeyIdentifier;
import com.ee.cne.ws.dataproduct.generated.EIMessageContext2;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsRequest;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsRequest.Message;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.ObjectFactory;
import com.ee.cne.ws.dataproduct.generated.TechnicalFault;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.model.SubscriberFilter;
import com.ericsson.eea.billing.model.SubscriberIdType;
import com.ericsson.eea.billing.util.BillingConstant;
import com.ericsson.eea.billing.util.BillingUtils;

/**
 * @author esonchy
 *
 */
public class DataProductsClient {
  private static final Logger log = Logger.getLogger(DataProductsClient.class);

  private DataProductsClient() {

  }

  /**
   * @param filter
   * @return
   * @throws SubscriberBillingInfoNotAvailableException
   * @throws SubscriberBillingRetrievalFailedException
   */
  public static GetCurrentAndAvailableDataProductsResponse getDataProductsWebServiceResponse(
      SubscriberFilter filter)
      throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    GetCurrentAndAvailableDataProductsResponse response = null;
    try {
      String pcrflURL = BillingUtils.getProperties().getProperty(BillingUtils.PCRF_BILLING_WS_URL);
      log.info("BIlling WebService URL :: " + pcrflURL);

      GetCurrentAndAvailableDataProductsRequest request =
          new ObjectFactory().createGetCurrentAndAvailableDataProductsRequest();

      if (filter != null && filter.getId() != null) {
        EIMessageContext2 messageContext = new EIMessageContext2();
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
        message.setRequestOrigin(BillingConstant.EEA_REQUESTOR_ID);
        request.setMessage(message);
      } else {
        log.error("NO Id found for retriveing Billing");
        throw new SubscriberBillingRetrievalFailedException();
      }

      JaxWsProxyFactoryBean proxyFactory = new JaxWsProxyFactoryBean();
      proxyFactory.setHandlers(Arrays.asList(new BillingWSInterceptor()));
      ClientFactoryBean clientBean = proxyFactory.getClientFactoryBean();
      clientBean.setAddress(pcrflURL);
      clientBean.setServiceClass(DataProduct.class);
      DataProduct client = (DataProduct) proxyFactory.create();
      response = client.getCurrentAndAvailableDataProducts(request);

    } catch (BusinessFault e) {

      log.error("Business Exception Occured :: " + e.getFaultInfo().getFaultDescription());
      throw new SubscriberBillingInfoNotAvailableException();
    } catch (TechnicalFault e) {

      log.error("Technical Exception Occured :: " + e.getFaultInfo().getFaultDescription());
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
