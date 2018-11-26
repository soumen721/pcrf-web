package com.ericsson.eea.billing.util;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDateTime> {

  @Override
  public LocalDateTime unmarshal(String inputDate) throws Exception {
    return ZonedDateTime.parse(inputDate, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
  }

  @Override
  public String marshal(LocalDateTime inputDate) throws Exception {
    return inputDate.toString();
  }

}
