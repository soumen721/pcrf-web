package com.ericsson.eea.billing.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BillingConstant {

	public final static String VALID_ZONE = "1";
	public final static List<String> INVALID_PASS_TYPE = Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");

	public final static List<String> VALID_INFO_TYPE_POSTPAID = Arrays.asList("C", "E", "S", "ZR", "EZR");

	public final static List<String> VALID_INFO_TYPE_PREPAID = Arrays.asList("C", "E", "S");
	public final static List<String> EXPIRY_REASON = Collections.singletonList("fup_change");

  public final static List<String> UNLIMITED_PASS_TYPE = Arrays.asList("EUTMAFUPunlimitedZ21",
      "ROW3EEAFUPunlimited", "ROW4TMAFUPunlimited", "TMHSAFUPUNLIMCLTB", "MBBAFUPunlimited",
      "ROW5TMAFUPunlimited", "AFUP0PET", "MBBAFUP0", "HSAFUP0", "BTAFUPUNLIMDELSOC", "MbbPr3m",
      "ROW3TMAFUPunlimited", "BTBLVOL", "EUEEAFUPunlimitedZ21", "ROW6EEAFUPunlimited", "HScap",
      "ROW9EEAFUPunlimited", "ROW6TMAFUPunlimited", "EUEEAFUPunlimitedZ22", "EUEEAFUPunlimited",
      "MBBAFUPEU500", "EUEEAFUPunlimitedZ21", "VM52HS3584", "ROW4EEAFUPunlimited", "VMHS900",
      "DUMMYAFUPBUY1GB", "ROW7TMAFUPunlimited", "ROW7TMAFUPunlimited", "ROW7TMAFUPunlimited",
      "Mbb2011_90d", "EUTMAFUPunlimited", "EUTMAFUPunlimitedZ21", "EUTMAFUPunlimitedZ22",
      "VMMBBUNLIMITED", "BTAFUPEUUNLIM", "BTROWVOL");

	public final static List<String> CURRENT_CYCLE_INFO_TYPE = Arrays.asList("C", "E", "S");
	public final static List<String> HISTORICAL_CYCLE_INFO_TYPE = Arrays.asList("E");

	public final static String BUSINESS_USER = "BUS_SHARE";

	public static final String CUST_TYPE_NEXUS = "NEXUS";

	public static final long BYTE_TO_MB = 1024 * 1024;

  public final static String EEA_SENDER_ID = "ee_ebdc_portal";
}
