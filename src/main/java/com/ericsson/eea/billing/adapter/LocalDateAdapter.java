package com.ericsson.eea.billing.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDateTime> {

  @Override
  public LocalDateTime unmarshal(String inputDate) throws Exception {
    ZonedDateTime Ztime = ZonedDateTime.parse(inputDate);
    System.out.println("Date ::"+ Ztime);
    System.out.println(Ztime.withZoneSameInstant(ZoneId.of("GMT")));
    
    return ZonedDateTime.parse(inputDate, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
  }

  @Override
  public String marshal(LocalDateTime inputDate) throws Exception {
    return inputDate.toString();
  }

}
