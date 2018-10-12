package com.ericsson.eea;

import javax.xml.namespace.QName;
import javax.xml.soap.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

public class ToolkitHeaderInjectHandler implements SOAPHandler<SOAPMessageContext> {
  // private static final Logger log = Logger.getLogger(ToolkitHeaderInjectHandler.class);

  @Override
  public boolean handleMessage(SOAPMessageContext context) {

    System.out.println("Client : handleMessage()......");
    Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

    try {
      SOAPMessage soapMsg = context.getMessage();
      if (isRequest) {
        SOAPEnvelope soapEnv = soapMsg.getSOAPPart().getEnvelope();
        SOAPHeader soapHeader = soapEnv.getHeader();

        // if no header, add one
        if (soapHeader == null) {
          soapHeader = soapEnv.addHeader();
        }

        SOAPElement trackingHeader = soapHeader.addChildElement("trackingHeader");

        SOAPBody soapBody = soapEnv.getBody();
        @SuppressWarnings("unchecked")
        Iterator<SOAPBodyElement> elements = soapBody.getChildElements();

        String requestId = null;
        while (elements.hasNext()) {
          SOAPBodyElement element = elements.next();
          System.out.println("Param Name " + element.getNodeValue());
          @SuppressWarnings("unchecked")
          Iterator<SOAPBodyElement> params = element.getChildElements();

          while (params.hasNext()) {
            SOAPBodyElement param = params.next();
            System.out.println(
                "Param Name Inner " + element.getNodeValue() + " Node Value : " + param.getValue());
            if ("correlationId".equals(param.getNodeName())) {
              requestId = param.getNodeValue();
            }
          }
        }

        SOAPElement requestIdNode = trackingHeader.addChildElement("requestId");
        requestIdNode.addTextNode(requestId);
      }

      // Printing Request/Response
      soapMsg.writeTo(System.out);
    } catch (SOAPException | IOException e) {
      System.out.println(e.getMessage());
    }

    return true;

  }

  @Override
  public boolean handleFault(SOAPMessageContext context) {
    System.out.println("Client : handleFault()......");
    return true;
  }

  @Override
  public void close(MessageContext context) {
    System.out.println("Client : close()......");
  }

  @Override
  public Set<QName> getHeaders() {
    System.out.println("Client : getHeaders()......");
    return null;
  }

}
