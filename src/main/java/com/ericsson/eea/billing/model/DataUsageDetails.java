package com.ericsson.eea.billing.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

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

	private Map<String, Double> zeroRatedDataUsedPerService;
}
