package com.ericsson.eea.billing.adapter;

import java.time.LocalDate;
import javax.xml.bind.annotation.adapters.XmlAdapter;

public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

  @Override
  public LocalDate unmarshal(String inputDate) throws Exception {
    return LocalDate.parse(inputDate);
  }

  @Override
  public String marshal(LocalDate inputDate) throws Exception {
    return inputDate.toString();
  }

}
