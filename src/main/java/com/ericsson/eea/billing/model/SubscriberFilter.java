package com.ericsson.eea.billing.model;

import java.io.Serializable;

public class SubscriberFilter implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * The Unique Subscriber ID
     */
    private SubscriberId id;

    /**
     * The Start Time In Unix Epoch for the Search
     */
    private Long startTimeEpoch;

    /**
     * The End Time In Unix Epoch for the Search
     */
    private Long endTimeEpoch;

    public SubscriberId getId() {
        return id;
    }

    public void setId(SubscriberId id) {
        this.id = id;
    }

    public Long getStartTimeEpoch() {
        return startTimeEpoch;
    }

    public void setStartTimeEpoch(Long startTimeEpoch) {
        this.startTimeEpoch = startTimeEpoch;
    }

    public Long getEndTimeEpoch() {
        return endTimeEpoch;
    }

    public void setEndTimeEpoch(Long endTimeEpoch) {
        this.endTimeEpoch = endTimeEpoch;
    }

}
