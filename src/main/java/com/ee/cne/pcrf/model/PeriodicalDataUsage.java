package com.ee.cne.pcrf.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class PeriodicalDataUsage implements Serializable {
	private static final long serialVersionUID = 1L;

	private DataUsageDetails currentCycleDataUsage;

	private DataUsageDetails previousCycleDataUsage;

	private DataUsageDetails penultimateCycleDataUsage;
}
