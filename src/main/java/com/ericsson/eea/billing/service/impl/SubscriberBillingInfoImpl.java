package com.ericsson.eea.billing.service.impl;

import com.ee.cne.ws.dataproduct.generated.Calculator;
import com.ee.cne.ws.dataproduct.generated.CalculatorSoap;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.*;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.service.SubscriberBillingRemote;
import com.ericsson.eea.billing.util.BillingUtils;
import com.ericsson.eea.billing.util.DummyDataGenerator;
import com.ericsson.eea.billing.util.TariffType;
import org.jboss.logging.Logger;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeConfigurationException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;

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
    DataUsageCalculationService usageCalculationService = null;

    if (TariffType.Prepaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PrePaidDataUsageCalculationService();
    } else if (TariffType.Postpaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PostPaidDataUsageCalculationService();
    }
    SubscriberBillingInfo billingInfo = usageCalculationService.calculateDataUsage(response);
    MessageEnvelope<SubscriberBillingInfo> envelope = new MessageEnvelope<>();
    envelope.setData(Arrays.asList(billingInfo));

    return envelope;
  }

  public static void main(String arg[]) throws DatatypeConfigurationException,
      SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    GetCurrentAndAvailableDataProductsResponse response = getDataProductsWebServiceResponse();
    GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo subscriberInfo =
        response.getMessage().getSubscriberInfo();
    DataUsageCalculationService usageCalculationService = null;

    if (TariffType.Prepaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PrePaidDataUsageCalculationService();
    } else if (TariffType.Postpaid.name().equals(subscriberInfo.getTariffType())) {

      usageCalculationService = new PostPaidDataUsageCalculationService();
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

      Calculator service = new Calculator(wsdlURL);
      CalculatorSoap ctxport = service.getCalculatorSoap();
      int resp = ctxport.add(10, 20);
      log.info("Addition Result ==> " + resp);

      /*
       * DataProductService dataService = new DataProductService(wsdlURL); DataProduct port =
       * dataService.getDataProduct10(); GetCurrentAndAvailableDataProductsRequest request =
       * ObjectFactory.createGetCurrentAndAvailableDataProductsRequest();
       *
       * // set msisdn and request origin in the request and call service
       * GetCurrentAndAvailableDataProductsResponse response = port.
       * getCurrentAndAvailableDataProducts(request); } catch (BusinessFault bex) { // handle
       * business fault by throwing EJB endpoint specific exception } catch (TechnicalFault tex) {
       * // handle technical fault by throwing EJB endpoint specific exception
       */
      response = DummyDataGenerator.populateResponseData();
    } catch (MalformedURLException e) {
      e.printStackTrace();
      throw new SubscriberBillingInfoNotAvailableException();
    } catch (DatatypeConfigurationException | URISyntaxException e) {
      throw new SubscriberBillingRetrievalFailedException();
    }

    if (response == null) {
      throw new SubscriberBillingInfoNotAvailableException();
    }
    return response;
  }
}
