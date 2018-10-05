package com.ericsson.eea.billing.service.impl;

import com.ee.cne.ws.dataproduct.generated.Calculator;
import com.ee.cne.ws.dataproduct.generated.CalculatorSoap;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.*;
import com.ericsson.eea.billing.service.DataUsageCalculationService;
import com.ericsson.eea.billing.service.SubscriberBillingRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.xml.datatype.DatatypeConfigurationException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

@Stateless
@Remote(SubscriberBillingRemote.class)
public class SubscriberBillingInfoImpl implements SubscriberBillingRemote {

    @Override
    public MessageEnvelope<SubscriberBillingInfo> getBillingCycleInfo(SubscriberFilter filter)
            throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException, DatatypeConfigurationException {

        // call PCRF web service and process and return response
        DataUsageCalculationService usageCalculationService = new PostPaidDataUsageCalculationService();
        MessageEnvelope<SubscriberBillingInfo> envelope = new MessageEnvelope();
        envelope.setData(Arrays.asList(usageCalculationService.calculateDataUsage()));

        return envelope;
    }

    public static void main(String arg[]) throws DatatypeConfigurationException, MalformedURLException {

        GetCurrentAndAvailableDataProductsResponse response = getDataProductsWebServiceResponse();
        DataUsageCalculationService usageCalculationService = new PostPaidDataUsageCalculationService();
        usageCalculationService.calculateDataUsage();
    }

    private static GetCurrentAndAvailableDataProductsResponse getDataProductsWebServiceResponse() {

        try {
            URL wsdlURL = new URL("http://www.dneonline.com/calculator.asmx?wsdl");
            Calculator service = new Calculator(wsdlURL);
            CalculatorSoap ctxport = service.getCalculatorSoap();
            int resp = ctxport.add(10, 20);
            System.out.println("Addition Result ==> " + resp);

			/*DataProductService dataService = new DataProductService(wsdlURL);
			DataProduct port = dataService.getDataProduct10();
			GetCurrentAndAvailableDataProductsRequest request =
				ObjectFactory.createGetCurrentAndAvailableDataProductsRequest();

			// set msisdn and request origin in the request and call service
			GetCurrentAndAvailableDataProductsResponse response = port.
					getCurrentAndAvailableDataProducts(request);
		} catch (BusinessFault bex) {
			// handle business fault by throwing EJB endpoint specific exception
		} catch (TechnicalFault tex) {
			// handle technical fault by throwing EJB endpoint specific exception*/
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
