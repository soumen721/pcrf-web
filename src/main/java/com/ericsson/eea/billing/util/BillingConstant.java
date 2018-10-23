package com.ericsson.eea.billing.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BillingConstant {

  public final static List<String> INVALID_PASS_TYPE =
      Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");
  
  public final static List<String> VALID_INFO_TYPE_POSTPAID =
      Arrays.asList("C", "E", "S", "ZR", "EZR");
  
  public final static List<String> VALID_INFO_TYPE_PREPAID = Arrays.asList("C", "E", "S");
  public final static List<String> EXPIRY_REASON = Collections.singletonList("fup_change");
  public final static String VALID_ZONE = "1";
  public final static String TYPE_UNLIMITED = "Unlimited";

  public final static String EEA_SENDER_ID = "ee_cs_toolkit";

  public final static List<String> CURRENT_CYCLE_INFO_TYPE = Arrays.asList("C", "E", "S");
  public final static List<String> HISTORICAL_CYCLE_INFO_TYPE = Arrays.asList("E", "S");
  
}
