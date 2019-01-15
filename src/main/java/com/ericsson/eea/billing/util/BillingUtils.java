package com.ericsson.eea.billing.util;

import static com.ericsson.eea.billing.util.BillingConstant.BYTE_TO_MB;
import static com.ericsson.eea.billing.util.BillingConstant.CUST_TYPE_NEXUS;
import static com.ericsson.eea.billing.util.BillingConstant.UNLIMITED_PASS_TYPE;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.XMLConstants;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;

public class BillingUtils {

  private static final Logger log = Logger.getLogger(BillingUtils.class);
  public static final String PCRF_BILLING_WS_URL = "pcrf.billing.ws.url";
  private static Properties properties;

  private BillingUtils() {

  }

  /**
   * @return
   */
  public static synchronized Properties getProperties() {
    try {
      if (properties != null) {
        log.info("Config serve form previously loaded instance");
        return properties;
      }
      properties = new Properties();
      String dir = System.getenv("config_dir");
      log.info("Config Location: "+ dir);
      InputStream in = new FileInputStream(dir + File.separator+ "pcrf_config.properties");
      properties.load(in);
      log.info("PCRF web-service URL:: " + properties.getProperty(PCRF_BILLING_WS_URL));
      log.info("Config File loaded successfully");
    } catch (IOException ex) {
      log.error("Exception in retriving value from property file ::" + ex);
    }

    return properties;
  }

  /**
   * @param dataPasses
   * @param predicate
   * @return , Get Filtered invalid PassType, InfoType, Zone along and then sort based on Start Date
   */
  public static List<DataPass> getFilteredDataPass(List<DataPass> dataPasses,
      Predicate<? super DataPass> predicate) {

    log.info("All Data pass from PCRF Service ");
    dataPasses.forEach(BillingUtils::printLog);

    Comparator<? super DataPass> dateComparator =
        (e1, e2) -> e1.getPassStartTime().compareTo(e2.getPassStartTime());

    return dataPasses.stream()
        .filter(pass -> !BillingConstant.INVALID_PASS_TYPE.contains(pass.getPassType()))
        .filter(predicate).filter(zone -> BillingConstant.VALID_ZONE.equals(zone.getValidZone()))
        .sorted(dateComparator.reversed()).collect(Collectors.toList());

  }

  /**
   * @param dataPasses
   * @param billCycleStartDate
   * @param billCycleEndDate @return, This method is being used for filter data for a certain time
   */
  public static List<DataPass> getFilteredDataPassBasedOnBillCycle(final List<DataPass> dataPasses,
      final LocalDateTime billCycleStartDate, final LocalDateTime billCycleEndDate) {

    log.info("Bill Cycle Start Date =>\t " + billCycleStartDate);
    log.info("Bill Cycle End Date   =>\t " + billCycleEndDate);

    List<DataPass> list = dataPasses.stream()
        .filter(pass -> (pass.getPassStartTime().isAfter(billCycleStartDate)
            || pass.getPassStartTime().isEqual(billCycleStartDate))
            && (pass.getPassEndTime().isBefore(billCycleEndDate))
            || pass.getPassEndTime().isEqual(billCycleEndDate))
        .collect(Collectors.toList());

    log.info("Passes after filter on BillCycle -- ");
    list.forEach(BillingUtils::printLog);

    return list;
  }

  /**
   * @param dataPasses
   * @return Filter data after and fup_changes
   */
  public static List<DataPass> filterDataPassOnFUPChange(List<DataPass> dataPasses) {

    List<DataPass> list = new ArrayList<>();
    for (DataPass dataPass : dataPasses) {
      if (BillingConstant.EXPIRY_REASON.contains(dataPass.getExpiryReason())) {
        break;
      }
      list.add(dataPass);
    }
    return list;
  }

  /**
   * @param billingInfo
   * @param subscriberInfo
   * @return Method is being used for populate basic details of Data Usage
   */
  public static SubscriberBillingInfo populateResponseBasicDetails(
      SubscriberBillingInfo billingInfo, SubscriberInfo subscriberInfo) {

    billingInfo.setImsi(Long.valueOf(subscriberInfo.getMsisdn()));
    billingInfo
        .setBillingUpdateTime(subscriberInfo.getLastCheckedDate().toEpochSecond(ZoneOffset.UTC));
    billingInfo.setBillingPlanCategory(getBillingCategory(subscriberInfo));

    Integer subscriberType = 0;
    if (TariffType.PREPAID.getType().equals(subscriberInfo.getTariffType())) {
      subscriberType =
          billingInfo.getSubscriberType() != null ? billingInfo.getSubscriberType() : 1;
    }
    billingInfo.setSubscriberType(subscriberType);

    return billingInfo;
  }

  /**
   * @param subscriberInfo
   * @return
   */
  private static int getBillingCategory(SubscriberInfo subscriberInfo) {
    int billingCat = 0;
    if (subscriberInfo.getTariffType().contains("L")) {
      billingCat = 1;
    } else if (BillingConstant.BUSINESS_USER.equals(subscriberInfo.getCustomerType())) {
      billingCat = 2;
    }
    return billingCat;
  }

  /**
   * @param info
   * @return Method use for Prepaid use Type definition
   */
  public static CustomrType getCustomerTypeForPrepaid(SubscriberInfo info) {

    CustomrType customerType = CustomrType.P12;
    if (info != null && TariffType.PREPAID.getType().equals(info.getCustomerType())) {
      if (info.getTypeOfAccess() != null && info.getTypeOfAccess().contains("R")) {
        if (info.getCustomerType() != null && CUST_TYPE_NEXUS.contains(info.getCustomerType())) {
          customerType = CustomrType.P14;
        }
      } else {
        if (info.getTypeOfAccess() != null && info.getTypeOfAccess().contains("U")) {
          customerType = CustomrType.P14;
        } else {
          if (info.getCustomerType() != null && CUST_TYPE_NEXUS.contains(info.getCustomerType())) {
            customerType = CustomrType.P14;
          }
        }
      }
    }
    return customerType;
  }

  /**
   * @param dataPasses
   * @return TO check whether a Subscriber is Unlimited data user or not
   */
  public static boolean isUnlimitedUsage(List<DataPass> dataPasses) {

    return dataPasses.stream().anyMatch(e -> UNLIMITED_PASS_TYPE.contains(e.getPassType()));
  }

  /**
   * @param info
   * @param dataPasse
   * @return TO check a Data is is Share pass or not
   */
  public static boolean isSharedPass(SubscriberInfo info, DataPass dataPasse) {

    return info.getTypeOfAccess().contains("L") && dataPasse.getShareDetails() != null
        && dataPasse.getShareDetails().getSharerDataUsage().size() > 1;
  }

  // Some Util class might be remove in future
  /**
   * @param date
   * @return
   * @throws DatatypeConfigurationException
   */
  public static XMLGregorianCalendar toXMLCalender(LocalDateTime date)
      throws DatatypeConfigurationException {

    GregorianCalendar gcal = GregorianCalendar.from(date.atZone(ZoneId.systemDefault()));
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
  }

  /**
   * @param xmlDate
   * @return
   */
  public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlDate) {

    return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
  }

  /**
   * @param dataPass Used for printing log details
   */
  public static void printLog(DataPass dataPass) {

    log.info("Data Pass Info: " + dataPass.getInfoType() + "\t| "
        + (dataPass.getPassStartTime() != null ? dataPass.getPassStartTime() : "NA") + "| "
        + (dataPass.getPassEndTime() != null ? dataPass.getPassEndTime() : "NA") + "| "
        + dataPass.getPassType() + "| " + dataPass.getExpiryReason());
  }

  /**
   * @param billingInfo
   */
  public static void printDataUsage(SubscriberBillingInfo billingInfo) {

    log.info("=======================================================================");
    log.info("Current Period Data Usage=>\n" + "DataUsed : " + billingInfo.getDataUsed()
        + "\t| Data Avail : " + billingInfo.getDataAvail() + "\t| Data ZeroRated : "
        + billingInfo.getZeroRatedDataUsed());

    log.info("Previous Period Data Usage=>\n" + "DataUsed : " + billingInfo.getLbcDataUsed()
        + "\t| Data Avail : " + billingInfo.getLbcDataAvail() + "\t| Data ZeroRated : "
        + billingInfo.getLbcZeroRatedDataUsed());

    log.info("Penultimate Period Data Usage=>\n" + "DataUsed : " + billingInfo.getPbcDataUsed()
        + "\t| Data Avail : " + billingInfo.getPbcDataAvail() + "\t| Data ZeroRated : "
        + billingInfo.getPbcZeroRatedDataUsed());
  }

  /**
   * @param xml
   * @return
   * @throws Exception
   */
  public static final String prettyPrintXML(Document xml) throws TransformerException {

    TransformerFactory factory = TransformerFactory.newInstance();
    factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    Transformer tf = factory.newTransformer();
    tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
    tf.setOutputProperty(OutputKeys.INDENT, "yes");
    Writer out = new StringWriter();
    tf.transform(new DOMSource(xml), new StreamResult(out));

    return out.toString();
  }

  /**
   * @param str
   * @return
   * @throws ParserConfigurationException
   * @throws SAXException
   * @throws IOException
   */
  public static Document toXmlDocument(String str)
      throws ParserConfigurationException, SAXException, IOException {

    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();

    return docBuilder.parse(new InputSource(new StringReader(str)));
  }

  /**
   * @param message
   * @return
   * @throws SOAPException
   * @throws IOException
   */
  public static String soapMessageToString(SOAPMessage message) throws SOAPException, IOException {
    String result = null;

    if (message != null) {
      try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
        message.writeTo(baos);
        result = baos.toString();
      } catch (IOException e) {
        log.error("In soapMessageToString Exception: " + e);
        throw e;
      }
    }
    return result;
  }

  /**
   * @param data
   * @return
   */
  public static Double getDataUsageInMB(Double data) {

    final Double dataNew = data != -1D ? data / BYTE_TO_MB : -1D;

    return BigDecimal.valueOf(dataNew).setScale(2, RoundingMode.HALF_UP).doubleValue();
  }
}
