package com.ericsson.eea.billing.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BillingConstant {

  public final static List<String> INVALID_PASS_TYPE =
      Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");
  public final static List<String> VALID_INFO_TYPE_POSTPAID =
      Arrays.asList("C", "S", "E", "ZR", "EZR");
  public final static List<String> VALID_INFO_TYPE_PREPAID = Arrays.asList("C", "S", "E");
  public final static List<String> EXPIRY_REASON = Collections.singletonList("fup_change");
  public final static String VALID_ZONE = "1";
  public final static String TYPE_UNLIMITED = "Unlimited";
}
