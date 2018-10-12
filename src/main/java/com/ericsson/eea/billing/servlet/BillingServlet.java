package com.ericsson.eea.billing.servlet;

import com.ericsson.eea.billing.service.SubscriberBillingRemote;
import java.io.IOException;
import java.io.PrintWriter;
import javax.ejb.EJB;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/billingServlet")
public class BillingServlet extends HttpServlet {
  private static final long serialVersionUID = 1L;

  @EJB(
      mappedName = "java:module/SubscriberBillingInfoImpl!com.ericsson.eea.billing.service.SubscriberBillingRemote")
  SubscriberBillingRemote bean;

  public BillingServlet() {
    super();
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    String resp = null;
    PrintWriter writer = response.getWriter();
    try {
      resp = bean.getBillingCycleInfo(null).toString();
    } catch (Exception e) {
      // TODO: handle exception
    }

    String htmlRespone = "<html>";
    htmlRespone += "<h2> Billing Response <br></h2>";
    htmlRespone += "<h4> " + resp + "</h4>";
    htmlRespone += "</html>";

    writer.println(htmlRespone);
  }

}
