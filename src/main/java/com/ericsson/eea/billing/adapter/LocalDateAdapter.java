package com.ericsson.eea.billing.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author esonchy
 *
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDateTime> {

  /* (non-Javadoc)
   * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
   */
  @Override
  public LocalDateTime unmarshal(String inputDate) throws Exception {
    ZonedDateTime zTime = ZonedDateTime.parse(inputDate);
    System.out.println("Date ::"+ zTime);
    System.out.println(zTime.withZoneSameInstant(ZoneId.of("GMT")));
    
    return ZonedDateTime.parse(inputDate, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
  }

  /* (non-Javadoc)
   * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
   */
  @Override
  public String marshal(LocalDateTime inputDate) throws Exception {
    return inputDate.toString();
  }

}
