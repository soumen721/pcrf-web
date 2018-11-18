package com.ericsson.eea.billing.ws.client;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Set;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;

import com.ericsson.eea.billing.util.BillingUtils;

public class BillingWSInterceptor implements SOAPHandler<SOAPMessageContext> {
	private static final Logger log = Logger.getLogger(BillingWSInterceptor.class);

	@Override
	public boolean handleMessage(SOAPMessageContext context) {

		log.info("Client : handleMessage()......");
		Boolean isRequest = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		try {
			SOAPMessage soapMsg = context.getMessage();
			Document xmlDoc = BillingUtils.toXmlDocument(soapMessageToString(soapMsg));

			if (isRequest) {
				log.info("Billing Request :: \n" + BillingUtils.prettyPrintXML(xmlDoc));
			} else {
				log.info("Billing Response :: \n" + BillingUtils.prettyPrintXML(xmlDoc));
			}
			log.debug("\n");
		} catch (SOAPException | IOException | DatatypeConfigurationException e) {
			log.error("Exception adding SOAP Header :: " + e.getMessage());
			// throw new Exception(e);
		} catch (Exception e) {
			log.error("Exception adding SOAP Header :: " + e.getMessage());
			// throw e;
		}

		return true;

	}

	@Override
	public boolean handleFault(SOAPMessageContext context) {
		log.debug("Client : handleFault()......");
		return true;
	}

	@Override
	public void close(MessageContext context) {
		log.debug("\nClient : close()......");
	}

	@Override
	public Set<QName> getHeaders() {
		log.debug("Client : getHeaders()......");
		return null;
	}

	public String soapMessageToString(SOAPMessage message) throws Exception {
		String result = null;

		if (message != null) {
			ByteArrayOutputStream baos = null;
			try {
				baos = new ByteArrayOutputStream();
				message.writeTo(baos);
				result = baos.toString();
			} catch (IOException e) {
				throw e;
			} finally {
				if (baos != null) {
					try {
						baos.close();
					} catch (IOException ioe) {
					}
				}
			}
		}
		return result;
	}
}
