package com.ericsson.eea.billing.model;

import java.io.Serializable;
import java.util.Map;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class SubscriberBillingInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * IMSI of the Subscriber
     */
    private Long imsi;

    /**
     * Billing Info update time in Unix Epoch
     */
    private Long billingUpdateTime;

    /**
     * Throttle info update time in Unix Epoch
     */
    private Long throttleUpdateTime;

    /**
     * Total Data Available (MB) in Current Billing Cycle
     */
    private Double dataAvail;

    /**
     * Data Used by this Subscriber (MB) in Current Billing Cycle
     */
    private Double dataUsed;

    /**
     * Total Shared Data Used (MB) in Current Billing Cycle
     */
    private Double dataUsedShared;

    /**
     * Total Zero Rated Data Used (MB) in Current Billing Cycle
     */
    private Double zeroRatedDataUsed;

    /**
     * Total Shared Data Used (MB) in Current Billing Cycle per Service Provider
     */
    private Map<String, Double> zeroRatedDataUsedPerService;

    /**
     * Billing plan Category of the subscriber (0 - Individual, 1 - Shared, 2 -
     * Business)
     */
    private Integer billingPlanCategory;

    /**
     * Throttled flag to indicate whether the subscriber is throttled
     */
    private Boolean throttled;

    /**
     * Current Billing System Date in Unix Epoch
     */
    private Long billingSystemDate;

    /**
     * Current Billing period Start Date in Unix Epoch
     */
    private Long billingPeriodStartDate;

    /**
     * Current Billing period End Date in Unix Epoch
     */
    private Long billingPeriodEndDate;

    /**
     * ARPU of the Subscriber
     */
    private Double arpu;

    /**
     * Total Data Available (MB) in Last Billing Cycle
     */
    private Double lbcDataAvail;

    /**
     * Total Data Used (MB) in Last Billing Cycle
     */
    private Double lbcDataUsed;

    /**
     * Total Shared Data Used (MB) in Last Billing Cycle
     */
    private Double lbcDataUsedShared;

    /**
     * Last Billing period Start Date in Unix Epoch
     */
    private Long lbcStartDate;

    /**
     * Last Billing period End Date in Unix Epoch
     */
    private Long lbcEndDate;

    /**
     * Total Zero Rated Data Used (MB) in Last Billing Cycle
     */
    private Double lbcZeroRatedDataUsed;

    /**
     * Total Shared Data Used (MB) in Last Billing Cycle per Service Provider
     */
    private Map<String, Double> lbcZeroRatedDataUsedPerService;

    /**
     * Total Data Available (MB) in Second Last Billing Cycle
     */
    private Double pbcDataAvail;

    /**
     * Total Data Used (MB) in Second Last Billing Cycle
     */
    private Double pbcDataUsed;

    /**
     * Total Shared Data Used (MB) in Second Last Billing Cycle
     */
    private Double pbcDataUsedShared;

    /**
     * Second Last Billing period Start Date in Unix Epoch
     */
    private Long pbcStartDate;

    /**
     * Second Last Billing period End Date in Unix Epoch
     */
    private Long pbcEndDate;


    /**
     * Total Zero Rated Data Used (MB) in Second Last Billing Cycle
     */
    private Double pbcZeroRatedDataUsed;

    /**
     * Total Shared Data Used (MB) in Second Last Billing Cycle per Service Provider
     */
    private Map<String, Double> pbcZeroRatedDataUsedPerService;

    /**
     * Subscriber Type (0 - Postpaid, 1 - Prepaid with start/end dates, 2 -
     * Prepaid without start/end dates)
     */
    private Integer subscriberType;


    private Map<String, GenericField> customFields;

    public Long getImsi() {
        return imsi;
    }

    public void setImsi(Long imsi) {
        this.imsi = imsi;
    }

    public Long getBillingUpdateTime() {
        return billingUpdateTime;
    }

    public void setBillingUpdateTime(Long billingUpdateTimeMs) {
        this.billingUpdateTime = billingUpdateTimeMs;
    }

    public Long getThrottleUpdateTime() {
        return throttleUpdateTime;
    }

    public void setThrottleUpdateTime(Long throttleUpdateTimeMs) {
        this.throttleUpdateTime = throttleUpdateTimeMs;
    }

    public Double getDataAvail() {
        return dataAvail;
    }

    public void setDataAvail(Double dataAvail) {
        this.dataAvail = dataAvail;
    }

    public Double getDataUsed() {
        return dataUsed;
    }

    public void setDataUsed(Double dataUsed) {
        this.dataUsed = dataUsed;
    }

    public Double getDataUsedShared() {
        return dataUsedShared;
    }

    public void setDataUsedShared(Double dataUsedShared) {
        this.dataUsedShared = dataUsedShared;
    }

    public Double getZeroRatedDataUsed() {
        return zeroRatedDataUsed;
    }

    public void setZeroRatedDataUsed(Double zeroRatedDataUsed) {
        this.zeroRatedDataUsed = zeroRatedDataUsed;
    }

    public Map<String, Double> getZeroRatedDataUsedPerService() {
        return zeroRatedDataUsedPerService;
    }

    public void setZeroRatedDataUsedPerService(Map<String, Double> zeroRatedDataUsedPerService) {
        this.zeroRatedDataUsedPerService = zeroRatedDataUsedPerService;
    }

    public Integer getBillingPlanCategory() {
        return billingPlanCategory;
    }

    public void setBillingPlanCategory(Integer billingPlanCategory) {
        this.billingPlanCategory = billingPlanCategory;
    }

    public Boolean getThrottled() {
        return throttled;
    }

    public void setThrottled(Boolean throttled) {
        this.throttled = throttled;
    }

    public Long getBillingSystemDate() {
        return billingSystemDate;
    }

    public void setBillingSystemDate(Long billingSystemDate) {
        this.billingSystemDate = billingSystemDate;
    }

    public Long getBillingPeriodStartDate() {
        return billingPeriodStartDate;
    }

    public void setBillingPeriodStartDate(Long billingPeriodStartDate) {
        this.billingPeriodStartDate = billingPeriodStartDate;
    }

    public Long getBillingPeriodEndDate() {
        return billingPeriodEndDate;
    }

    public void setBillingPeriodEndDate(Long billingPeriodEndDate) {
        this.billingPeriodEndDate = billingPeriodEndDate;
    }


    public Double getArpu() {
        return arpu;
    }

    public void setArpu(Double arpu) {
        this.arpu = arpu;
    }

    public Double getLbcDataAvail() {
        return lbcDataAvail;
    }

    public void setLbcDataAvail(Double lbcDataAvail) {
        this.lbcDataAvail = lbcDataAvail;
    }

    public Double getLbcDataUsed() {
        return lbcDataUsed;
    }

    public void setLbcDataUsed(Double lbcDataUsed) {
        this.lbcDataUsed = lbcDataUsed;
    }

    public Double getLbcDataUsedShared() {
        return lbcDataUsedShared;
    }

    public void setLbcDataUsedShared(Double lbcDataUsedShared) {
        this.lbcDataUsedShared = lbcDataUsedShared;
    }

    public Long getLbcStartDate() {
        return lbcStartDate;
    }

    public void setLbcStartDate(Long lbcStartDate) {
        this.lbcStartDate = lbcStartDate;
    }

    public Long getLbcEndDate() {
        return lbcEndDate;
    }

    public void setLbcEndDate(Long lbcEndDate) {
        this.lbcEndDate = lbcEndDate;
    }

    public Double getPbcDataAvail() {
        return pbcDataAvail;
    }

    public void setPbcDataAvail(Double pbcDataAvail) {
        this.pbcDataAvail = pbcDataAvail;
    }

    public Double getPbcDataUsed() {
        return pbcDataUsed;
    }

    public void setPbcDataUsed(Double pbcDataUsed) {
        this.pbcDataUsed = pbcDataUsed;
    }

    public Double getPbcDataUsedShared() {
        return pbcDataUsedShared;
    }

    public void setPbcDataUsedShared(Double pbcDataUsedShared) {
        this.pbcDataUsedShared = pbcDataUsedShared;
    }

    public Long getPbcStartDate() {
        return pbcStartDate;
    }

    public void setPbcStartDate(Long pbcStartDate) {
        this.pbcStartDate = pbcStartDate;
    }

    public Long getPbcEndDate() {
        return pbcEndDate;
    }

    public void setPbcEndDate(Long pbcEndDate) {
        this.pbcEndDate = pbcEndDate;
    }

    public Integer getSubscriberType() {
        return subscriberType;
    }

    public void setSubscriberType(Integer subscriberType) {
        this.subscriberType = subscriberType;
    }

    public Map<String, GenericField> getCustomFields() {
        return customFields;
    }

    public void setCustomFields(Map<String, GenericField> customFields) {
        this.customFields = customFields;
    }

    public Double getLbcZeroRatedDataUsed() {
        return lbcZeroRatedDataUsed;
    }

    public void setLbcZeroRatedDataUsed(Double lbcZeroRatedDataUsed) {
        this.lbcZeroRatedDataUsed = lbcZeroRatedDataUsed;
    }

    public Map<String, Double> getLbcZeroRatedDataUsedPerService() {
        return lbcZeroRatedDataUsedPerService;
    }

    public void setLbcZeroRatedDataUsedPerService(Map<String, Double> lbcZeroRatedDataUsedPerService) {
        this.lbcZeroRatedDataUsedPerService = lbcZeroRatedDataUsedPerService;
    }

    public Double getPbcZeroRatedDataUsed() {
        return pbcZeroRatedDataUsed;
    }

    public void setPbcZeroRatedDataUsed(Double pbcZeroRatedDataUsed) {
        this.pbcZeroRatedDataUsed = pbcZeroRatedDataUsed;
    }

    public Map<String, Double> getPbcZeroRatedDataUsedPerService() {
        return pbcZeroRatedDataUsedPerService;
    }

    public void setPbcZeroRatedDataUsedPerService(Map<String, Double> pbcZeroRatedDataUsedPerService) {
        this.pbcZeroRatedDataUsedPerService = pbcZeroRatedDataUsedPerService;
    }

}
