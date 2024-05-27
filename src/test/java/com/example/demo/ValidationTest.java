package com.example.demo;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import com.example.demo.model.Address;
import com.example.demo.model.Customer;
import com.example.demo.report.Message;
import com.example.demo.report.Message.Type;
import com.example.demo.report.ReportFactory;
import com.example.demo.report.ReportFactoryImpl;
import com.example.demo.report.ValidationReport;
import com.example.demo.report.ValidationReportImpl;
import com.example.demo.service.BankingInquiryService;

public class ValidationTest {

    static KieContainer kieContainer;
    static ReportFactory reportFactory;
    private static final String RULES_DRL = "rules/rules.drl";
    private static final String VALIDATION_DRL = "rules/validation.drl";

    @BeforeAll
    public static void setUpClass() throws Exception {

        KieServices kieServices = KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_DRL));
        kieFileSystem.write(ResourceFactory.newClassPathResource(VALIDATION_DRL));
//        kieFileSystem.write(ResourceFactory.newClassPathResource(accountsDrl));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

    }
    
    @Test
    public void addressRequired() throws Exception {
      ValidationReport validationReport = new ValidationReportImpl();
      Customer customer = new Customer();
      customer.setPhoneNumber("123456789");
      validateCustomer(customer, validationReport);
      assertNull(customer.getAddress());
      assertReportContains(Message.Type.WARNING,
         "addressRequired", customer, validationReport);

      validationReport = new ValidationReportImpl();
      customer.setAddress(new Address());
      validateCustomer(customer, validationReport);
      assertNotReportContains(Message.Type.WARNING,
         "addressRequired", customer, validationReport);
     }

     private void assertReportContains(Type type, String messageString, Customer customer,
             ValidationReport validationReport) {
         System.out.println("************assertReportContains***************");
         assertReportContent(type, messageString, customer, validationReport, true);
     }

     private void assertNotReportContains(Type type, String messageString, Customer customer,
             ValidationReport validationReport) {
         System.out.println("************assertReportContains***************");
         assertReportContent(type, messageString, customer, validationReport, false);
     }

     private void assertReportContent(Type type, String messageString, Customer customer,
             ValidationReport validationReport, boolean isMessageExpected) {
         System.out.println("************assertReportContent***************");
         Set<Message> messages = validationReport.getMessages();
         boolean isMessageInReport = false;

         for (Message m : messages) {
             System.out.println("message type:" + m.getType());
             System.out.println("message key:" + m.getMessageKey());
             System.out.println("message ordered context:" + m.getContextOrdered());
             if (type == m.getType() && messageString.equals(m.getMessageKey())) {
                 isMessageInReport = true;
                 break;
             }
         }

         if (isMessageExpected) {
             assertTrue(isMessageInReport);
         } else {
             assertFalse(isMessageInReport);
         }
     }

     public void validateCustomer(Customer customer, ValidationReport validationReport) {
         KieSession kieSession = kieContainer.newKieSession(); // kieContainer.newKieSession("rulesSession");
         kieSession.insert(customer);
         kieSession.setGlobal("validationReport", validationReport);
         kieSession.setGlobal("reportFactory", new ReportFactoryImpl());
         kieSession.setGlobal("inquiryService", new BankingInquiryService(kieContainer));
         kieSession.fireAllRules();
         kieSession.dispose();
     }

}
