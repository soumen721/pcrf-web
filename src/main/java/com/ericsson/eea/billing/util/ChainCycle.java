package com.ericsson.eea.billing.util;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChainCycle implements Serializable{

  private static final long serialVersionUID = 1L;

  private BillingCycle currentCycle;
  private BillingCycle nextCycle;
}
