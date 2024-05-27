package com.example.demo.service;


import org.kie.api.runtime.rule.RuleContext;

import java.util.Date;

import org.kie.api.runtime.KieRuntime;

import com.example.demo.report.ValidationReport;
import com.example.demo.report.ReportFactory;
import com.example.demo.report.Message;

public class ValidationHelper {

    /**
     * adds an error message to the global validation report
     * @param kcontext RuleContext that is accessible from
     * rule condition
     * @param context for the message
     */
   public static void error(RuleContext kcontext,
        Object... context) {
       KieRuntime knowledgeRuntime = kcontext
          .getKnowledgeRuntime();
      ValidationReport validationReport = (ValidationReport)
          knowledgeRuntime.getGlobal("validationReport");
      ReportFactory reportFactory = (ReportFactory)
          knowledgeRuntime.getGlobal("reportFactory");
      validationReport.addMessage(reportFactory.createMessage(
          Message.Type.ERROR, kcontext.getRule().getName(),
          context));
   }

   public static void warning(RuleContext kcontext,
           Object... context) {
          KieRuntime knowledgeRuntime = kcontext
             .getKnowledgeRuntime();
         ValidationReport validationReport = (ValidationReport)
             knowledgeRuntime.getGlobal("validationReport");
         ReportFactory reportFactory = (ReportFactory)
             knowledgeRuntime.getGlobal("reportFactory");
         validationReport.addMessage(reportFactory.createMessage(
             Message.Type.WARNING, kcontext.getRule().getName(),
             context));
      }

   /**
    * @return number of years between today and specified date
    */
   public static int yearsPassedSince(Date date) {
       final int year = date.getYear();
       final int todaysYear = new Date().getYear();
       int yearsPassedSince = todaysYear - year;
       return yearsPassedSince;
   }

}
