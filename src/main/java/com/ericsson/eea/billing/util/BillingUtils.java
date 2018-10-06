package com.ericsson.eea.billing.util;

import com.ee.cne.ws.dataproduct.generated.DataPass;
import com.ee.cne.ws.dataproduct.generated.GetCurrentAndAvailableDataProductsResponse;
import com.ericsson.eea.billing.model.SubscriberBillingInfo;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Collectors;

import static com.ericsson.eea.billing.util.BillingConstant.TYPE_UNLIMITED;

public class BillingUtils {

    // Get Filtered invalid PassType, InfoType, Zone along and then sort based on
    // Start Date
    public static List<DataPass> getFilteredDataPass(List<DataPass> dataPasses) {

        System.out.println("Before First Iteration ");
        dataPasses.forEach(BillingUtils::printLog);

        Comparator<? super DataPass> dateComparator = (e1, e2) -> e1.getPassStartTime().compare(e2.getPassStartTime());

        return dataPasses.stream().filter(pass -> !BillingConstant.INVALID_PASS_TYPE.contains(pass.getPassType()))
                .filter(info -> BillingConstant.VALID_INFO_TYPE.contains(info.getInfoType()))
                .filter(zone -> BillingConstant.VALID_ZONE.equals(zone.getValidZone()))
                .sorted(dateComparator.reversed()).collect(Collectors.toList());

    }

    public static List<DataPass> getFilteredDataPassBasedOnBillCycle(final List<DataPass> dataPasses,
                                                                     final LocalDateTime billCycleStartDate, final LocalDateTime billCycleEndDate) {

        System.out.println("Bill Cycle Start Date =>\t " + billCycleStartDate.toLocalDate());
        System.out.println("Bill Cycle End Date=>\t " + billCycleEndDate.toLocalDate());

        List<DataPass> list = dataPasses.stream()
                .filter(pass -> (BillingUtils.toLocalDateTime(pass.getPassStartTime()).isAfter(billCycleStartDate)
                        || BillingUtils.toLocalDateTime(pass.getPassStartTime()).isEqual(billCycleStartDate))
                        && (BillingUtils.toLocalDateTime(pass.getPassEndTime()).isBefore(billCycleEndDate))
                        || BillingUtils.toLocalDateTime(pass.getPassEndTime()).isEqual(billCycleEndDate))
                .collect(Collectors.toList());

        System.out.println("After Sorting ON Date\t|\t|");
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

    //Only for Prepaid Customer
    public static CustomrType getCustomerTypeForPrepaid(GetCurrentAndAvailableDataProductsResponse.Message.SubscriberInfo info) {

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

    //Some Util class might be remove in future
    public static XMLGregorianCalendar toXMLCalender(LocalDateTime date) throws DatatypeConfigurationException {

        GregorianCalendar gcal = GregorianCalendar.from(date.atZone(ZoneId.systemDefault()));
        return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
    }

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar xmlDate) {

        return xmlDate.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }

    public static void printLog(DataPass dataPass) {

        System.out.println(dataPass.getInfoType() + " \t| "
                + dataPass.getPassStartTime().toGregorianCalendar().toZonedDateTime().toLocalDate() + " \t| "
                + dataPass.getPassEndTime().toGregorianCalendar().toZonedDateTime().toLocalDate()
                + "  \t|Expiry_Reason \t| " + dataPass.getExpiryReason());
    }

    public static void printDataUsage(SubscriberBillingInfo billingInfo) {

        System.out.println("=======================================================================");
        System.out.println("Current Period Data Usage=>\n" + "DataUsed : "
                + billingInfo.getDataUsed() + "\t| Data Avail : "
                + billingInfo.getDataAvail() + "\t| Data Remaining : "
                + billingInfo.getDataUsedShared());

        System.out.println("Previous Period Data Usage=>\n" + "DataUsed : "
                + billingInfo.getLbcDataUsed() + "\t| Data Avail : "
                + billingInfo.getLbcDataAvail() + "\t| Data Remaining : "
                + billingInfo.getLbcDataUsedShared());

        System.out.println("Penultimate Period Data Usage=>\n" + "DataUsed : "
                + billingInfo.getPbcDataUsed() + "\t| Data Avail : "
                + billingInfo.getPbcDataAvail() + "\t| Data ZeroRated : "
                + billingInfo.getPbcDataUsedShared());
    }
}
