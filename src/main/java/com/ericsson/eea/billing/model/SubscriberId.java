package com.ericsson.eea.billing.model;

import java.io.Serializable;

public class SubscriberId implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * The Type of Subscribe ID (imsi, msisdn, nif, imei, email e.t.c)
     */
    SubscriberIdType idType;
    /**
     * Subscriber ID Value depending on the idType
     */
    String id;

    public SubscriberIdType getIdType() {
        return idType;
    }

    public void setIdType(SubscriberIdType idType) {
        this.idType = idType;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
