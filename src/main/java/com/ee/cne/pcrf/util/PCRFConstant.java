package com.ee.cne.pcrf.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PCRFConstant {
	
	public final static List<String> INVALID_PASS_TYPE = Arrays.asList("HSAFUP0", "MBBAFUP0", "AFUP0PET");
	public final static List<String> VALID_INFO_TYPE = Arrays.asList("C", "S", "E", "ZR", "EZR");
	public final static List<String> EXPIRY_REASON = Collections.singletonList("fup_change");
	public final static String VALID_ZONE = "1";

}
