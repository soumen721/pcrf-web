package com.ericsson.eea.billing.ws.client;

import java.io.File;
import java.io.FileNotFoundException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;

public class GenerateResponse {

  public static GetCurrentAndAvailableDataProductsResponse getDummyResponse()
      throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException,
      JAXBException {

    JAXBContext jaxbContext =
        JAXBContext.newInstance(GetCurrentAndAvailableDataProductsResponse.class);
    Marshaller marshaller = jaxbContext.createMarshaller();
    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
    // marshaller.marshal(response, new File("src/main/resources/SampleResponse_1.xml"));

    File file = new File("C:\\Project_Details\\custom_login_module\\pcrf-web\\src\\main\\resources\\SampleResponse.xml");
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    GetCurrentAndAvailableDataProductsResponse response =
        (GetCurrentAndAvailableDataProductsResponse) unmarshaller.unmarshal(file);
    //marshaller.marshal(response, System.out);

    return response;
  }

  public static void main(String arg[]) throws JAXBException, FileNotFoundException,
      SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

    GenerateResponse.getDummyResponse();
  }



}
