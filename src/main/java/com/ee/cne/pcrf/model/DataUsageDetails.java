package com.ee.cne.pcrf.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class DataUsageDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private long dataUsed;

	private long dataAvail;

	private long dataRemaining;
}
