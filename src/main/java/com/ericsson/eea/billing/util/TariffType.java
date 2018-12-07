package com.ericsson.eea.billing.util;

public enum TariffType {
  POSTPAID("Postpaid"), PREPAID("Prepaid");

  private String type;

  TariffType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }
}
