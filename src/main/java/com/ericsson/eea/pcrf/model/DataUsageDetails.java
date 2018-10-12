package com.ericsson.eea.pcrf.model;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class DataUsageDetails implements Serializable {

  private static final long serialVersionUID = 1L;

  private Long billingPeriodStartDate;
  private Long billingPeriodEndDate;

  private Double dataUsed;

  private Double dataAvail;

  private Double dataRemaining;

  private Double dataUsedShared;

  private Double zeroRatedDataUsed;
}
