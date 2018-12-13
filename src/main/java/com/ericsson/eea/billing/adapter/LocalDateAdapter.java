package com.ericsson.eea.billing.adapter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import org.jboss.logging.Logger;

/**
 * @author esonchy
 *
 */
public class LocalDateAdapter extends XmlAdapter<String, LocalDateTime> {
  private static final Logger log = Logger.getLogger(LocalDateAdapter.class);

  /*
   * (non-Javadoc)
   * 
   * @see javax.xml.bind.annotation.adapters.XmlAdapter#unmarshal(java.lang.Object)
   */
  @Override
  public LocalDateTime unmarshal(String inputDate) throws Exception {
    ZonedDateTime zTime = ZonedDateTime.parse(inputDate);
    LocalDateTime dateTime =
        ZonedDateTime.parse(inputDate, DateTimeFormatter.ISO_DATE_TIME).toLocalDateTime();
    log.info("Date:: " + zTime);
    log.info("GMT:: " + zTime.withZoneSameInstant(ZoneId.of("GMT")));

    if (!ZoneOffset.UTC.equals(zTime.getZone())) {
      dateTime = dateTime.plusHours(1);
    }

    return dateTime;
  }

  /*
   * (non-Javadoc)
   * 
   * @see javax.xml.bind.annotation.adapters.XmlAdapter#marshal(java.lang.Object)
   */
  @Override
  public String marshal(LocalDateTime inputDate) throws Exception {
    return inputDate.toString();
  }

}
