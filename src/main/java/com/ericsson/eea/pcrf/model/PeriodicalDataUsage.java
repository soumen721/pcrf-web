package com.ericsson.eea.pcrf.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PeriodicalDataUsage implements Serializable {
	private static final long serialVersionUID = 1L;

	private DataUsageDetails currentCycleDataUsage;

	private DataUsageDetails previousCycleDataUsage;

	private DataUsageDetails penultimateCycleDataUsage;
}
