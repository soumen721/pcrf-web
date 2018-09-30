package com.ee.cne.pcrf.servlet;

import java.util.Arrays;
import java.util.List;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;

public class FilterService {

	final List<String> PASS_TYPE_IGNORE = Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");
	final List<String> INFO_TYPE = Arrays.asList("C", "S", "E", "ZR", "EZR");
	final List<String> EXPIRY_REASON = Arrays.asList("fup_change");
	
	public void getFilterData(){
		//GetCurrentAndAvailableDataProductsRequest request = new GetCurrentAndAvailableDataProductsRequest();
		
		GetCurrentAndAvailableDataProductsResponse response = new GetCurrentAndAvailableDataProductsResponse();
		
		SubscriberInfo info = response.getMessage().getSubscriberInfo();
		List<DataPass> dataPasses = response.getMessage().getDataProducts().getDataProduct();
		
		dataPasses.stream();
	
	}
}
