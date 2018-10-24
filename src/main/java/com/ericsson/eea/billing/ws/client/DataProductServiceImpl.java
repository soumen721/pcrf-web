package com.ericsson.eea.billing.ws.client;

import java.net.URL;
import javax.jws.HandlerChain;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.DataProductService;

@HandlerChain(file = "handler-chain.xml")
public class DataProductServiceImpl extends DataProductService {
  private static final Logger log = Logger.getLogger(DataProductServiceImpl.class);
  
  public DataProductServiceImpl(URL wsdlLocation) {
    super(wsdlLocation, SERVICE);
    log.info("In DataProductServiceImpl");
  }

}
