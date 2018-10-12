package com.ericsson.eea.billing.util;

import static com.ericsson.eea.billing.util.BillingConstant.TYPE_UNLIMITED;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import org.jboss.logging.Logger;
import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;

public class BillingUtils {

  private static final Logger log = Logger.getLogger(BillingUtils.class);
  public static final String PCRF_BILLING_WS_URL = "pcrf.billing.ws.url";

  public static Properties getProperties() {
    Properties prop = new Properties();
    InputStream input = null;
    try {

      input = BillingUtils.class.getClassLoader().getResourceAsStream("config.properties");
      prop.load(input);
    } catch (IOException ex) {
      log.error("Exception in retriving value from property file ::" + ex.getMessage());
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return prop;
  }

  // Get Filtered invalid PassType, InfoType, Zone along and then sort based on
  // Start Date
  public static List<DataPass> getFilteredDataPass(List<DataPass> dataPasses,
      Predicate<? super DataPass> predicate) {

    log.info("Before First Iteration ");
    dataPasses.forEach(BillingUtils::printLog);

    Comparator<? super DataPass> dateComparator =
        (e1, e2) -> e1.getPassStartTime().compare(e2.getPassStartTime());

    return dataPasses.stream()
        .filter(pass -> !BillingConstant.INVALID_PASS_TYPE.contains(pass.getPassType()))
        .filter(predicate).filter(zone -> BillingConstant.VALID_ZONE.equals(zone.getValidZone()))
        .sorted(dateComparator.reversed()).collect(Collectors.toList());

  }

  public static List<DataPass> getFilteredDataPassBasedOnBillCycle(final List<DataPass> dataPasses,
      final LocalDateTime billCycleStartDate, final LocalDateTime billCycleEndDate) {

    log.info("Bill Cycle Start Date =>\t " + billCycleStartDate.toLocalDate());
    log.info("Bill Cycle End Date=>\t " + billCycleEndDate.toLocalDate());

    List<DataPass> list = dataPasses.stream().filter(
        pass -> (BillingUtils.toLocalDateTime(pass.getPassStartTime()).isAfter(billCycleStartDate)
            || BillingUtils.toLocalDateTime(pass.getPassStartTime()).isEqual(billCycleStartDate))
            && (BillingUtils.toLocalDateTime(pass.getPassEndTime()).isBefore(billCycleEndDate))
            || BillingUtils.toLocalDateTime(pass.getPassEndTime()).isEqual(billCycleEndDate))
        .collect(Collectors.toList());

    log.info("After Sorting ON Date\t|\t|");
    list.forEach(BillingUtils::printLog);

    return list;
  }

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

  // Only for Prepaid Customer
  public static CustomrType getCustomerTypeForPrepaid(SubscriberInfo info) {

    CustomrType customerType = CustomrType.P12;
    if (info != null && TariffType.Prepaid.name().equals(info.getCustomerType())) {
      if (info.getTypeOfAccess() != null && info.getTypeOfAccess().contains("R")) {
        if (info.getCustomerType() != null && "NEXUS".contains(info.getCustomerType())) {
          customerType = CustomrType.P14;
        }
      } else {
        if (info.getTypeOfAccess() != null && info.getTypeOfAccess().contains("U")) {
          customerType = CustomrType.P14;
        } else {
          if (info.getCustomerType() != null && "NEXUS".contains(info.getCustomerType())) {
            customerType = CustomrType.P14;
          }
        }
      }
    }
    return customerType;
  }

  public static boolean isUnlimitedUsage(List<DataPass> dataPasses) {

    return dataPasses.stream().anyMatch(e -> TYPE_UNLIMITED.equals(e.getPassType()));
  }

  public static boolean isSharedPass(SubscriberInfo info, DataPass dataPasse) {

    return info.getTypeOfAccess().contains("L") && dataPasse.getShareDetails() != null
        && dataPasse.getShareDetails().getSharerDataUsage().size() > 1;
  }

  // Some Util class might be remove in future
  public static XMLGregorianCalendar toXMLCalender(LocalDateTime date)
      throws DatatypeConfigurationException {

    GregorianCalendar gcal = GregorianCalendar.from(date.atZone(ZoneId.systemDefault()));
    return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
  }

  public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlDate) {

    return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
  }

  public static void printLog(DataPass dataPass) {

    log.info(dataPass.getInfoType() + " \t| "
        + dataPass.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
        + " \t| " + dataPass.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
        + "  \t|Expiry_Reason \t| " + dataPass.getExpiryReason());
  }

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
}
