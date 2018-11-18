package com.ericsson.eea.billing.ws.client;

import java.io.File;
import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.DataProducts;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfoNotAvailableException;
import com.ericsson.eea.billing.model.SubscriberBillingRetrievalFailedException;
import com.ericsson.eea.billing.util.BillingUtils;

public class GenerateResponse {

	final static int ADD_DAY = 4;

	public static GetCurrentAndAvailableDataProductsResponse getDummyResponse()
			throws SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException,
			JAXBException {

		JAXBContext jaxbContext = JAXBContext.newInstance(GetCurrentAndAvailableDataProductsResponse.class);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		File file = new File("D:\\Workspace\\Jboss-Poc\\pcrf\\pcrf-web\\src\\main\\resources\\SampleResponse.xml");
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		GetCurrentAndAvailableDataProductsResponse response = (GetCurrentAndAvailableDataProductsResponse) unmarshaller
				.unmarshal(file);

		GetCurrentAndAvailableDataProductsResponse resp = updateDataResponse(response);
		marshaller.marshal(resp, System.out);
		return resp;

	}

	private static GetCurrentAndAvailableDataProductsResponse updateDataResponse(
			GetCurrentAndAvailableDataProductsResponse response) {
		GetCurrentAndAvailableDataProductsResponse upateResponse = new GetCurrentAndAvailableDataProductsResponse();
		upateResponse.setEiMessageContext2(response.getEiMessageContext2());
		Message message = new Message();
		SubscriberInfo subInfo = response.getMessage().getSubscriberInfo();
		LocalDateTime checkDate = BillingUtils.toLocalDateTime(subInfo.getLastCheckedDate());
		try {
			subInfo.setLastCheckedDate(BillingUtils.toXMLCalender(checkDate.plusMonths(ADD_DAY)));
		} catch (DatatypeConfigurationException e2) {
			e2.printStackTrace();
		}

		// subInfo.setBillCycle(checkDate.get);
		message.setSubscriberInfo(subInfo);

		List<DataPass> passes = response.getMessage().getDataProducts().getDataProduct().stream()
				.filter(d -> d.getPassStartTime() != null && d.getPassEndTime() != null).map(e -> {
					DataPass pass = e;

					LocalDateTime startDate = BillingUtils.toLocalDateTime(pass.getPassStartTime());
					LocalDateTime endDate = BillingUtils.toLocalDateTime(pass.getPassEndTime());
					try {
						pass.setPassStartTime(BillingUtils.toXMLCalender(startDate.plusMonths(ADD_DAY)));
						pass.setPassEndTime(BillingUtils.toXMLCalender(endDate.plusMonths(ADD_DAY)));
					} catch (DatatypeConfigurationException e1) {
						e1.printStackTrace();
					}
					return pass;
				}).collect(Collectors.toList());

		DataProducts product = new DataProducts();
		product.getDataProduct().addAll(passes);

		message.setDataProducts(product);
		upateResponse.setMessage(message);

		return upateResponse;
	}

	public static void main(String arg[]) throws JAXBException, FileNotFoundException,
			SubscriberBillingInfoNotAvailableException, SubscriberBillingRetrievalFailedException {

		GenerateResponse.getDummyResponse();
	}

}
