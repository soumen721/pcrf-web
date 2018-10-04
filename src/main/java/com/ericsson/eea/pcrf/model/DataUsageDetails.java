package com.ericsson.eea.pcrf.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataUsageDetails implements Serializable {

	private static final long serialVersionUID = 1L;

	private long dataUsed;

	private long dataAvail;

	private long dataRemaining;
}
