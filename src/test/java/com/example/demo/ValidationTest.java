package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.example.demo.model.Account;
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
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

    }
    
    @Test
    public void addressRequired() throws Exception {
      ValidationReport validationReport = new ValidationReportImpl();
      final Customer customer = new Customer();
      customer.setPhoneNumber("123456789");
      assertNull(customer.getAddress());
      validateCustomer(customer, validationReport);
      assertReportContains(Message.Type.WARNING, "addressRequired", validationReport);

      validationReport = new ValidationReportImpl();
      customer.setAddress(new Address());
      validateCustomer(customer, validationReport);
      assertNotReportContains(Message.Type.WARNING, "addressRequired", validationReport);
     }

     private void assertReportContains(Type type, String messageString,
             ValidationReport validationReport) {
         System.out.println("************assertReportContains***************");
         assertReportContent(type, messageString, validationReport, true);
     }

     private void assertNotReportContains(Type type, String messageString,
             ValidationReport validationReport) {
         System.out.println("************assertNotReportContains***************");
         assertReportContent(type, messageString, validationReport, false);
     }

     private void assertReportContent(Type type, String messageString,
             ValidationReport validationReport, boolean isMessageExpected) {
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

     public void validateCustomer(final Customer customer, final ValidationReport validationReport) {
         final KieSession kieSession = kieContainer.newKieSession();
         kieSession.insert(customer);
         setGlobalsAndFire(validationReport, kieSession);
     }

     @Test
     public void accountOwnerRequired() throws Exception {
         ValidationReport validationReport = new ValidationReportImpl();
         final Account account = new Account();
         assertNull(account.getOwner());
         validateAccount(account, validationReport);
         assertReportContains(Message.Type.ERROR, "accountOwnerRequired", validationReport);

         validationReport = new ValidationReportImpl();
         account.setOwner(new Customer());
         validateAccount(account, validationReport);
         assertNotReportContains(Message.Type.ERROR, "accountOwnerRequired", validationReport);
     }

     public void validateAccount(final Account account, final ValidationReport validationReport) {
         final KieSession kieSession = kieContainer.newKieSession();
         kieSession.insert(account);
         setGlobalsAndFire(validationReport, kieSession);
     }

     private void setGlobalsAndFire(final ValidationReport validationReport, final KieSession kieSession) {
         kieSession.setGlobal("validationReport", validationReport);
         kieSession.setGlobal("reportFactory", new ReportFactoryImpl());
         kieSession.setGlobal("inquiryService", new BankingInquiryService(kieContainer));
         kieSession.fireAllRules();
         kieSession.dispose();
     }

     @Test
     public void accountBalanceAtLeast() throws Exception {
         ValidationReport validationReport = new ValidationReportImpl();
         final Customer customer = createBasicCustomer();
         final Account account = new Account();
         account.setOwner(new Customer());
         account.setBalance(0);
         customer.getAccounts().add(account);
         validateCustomer(customer, validationReport);
         assertReportContains(Message.Type.WARNING, "accountBalanceAtLeast", validationReport);

         validationReport = new ValidationReportImpl();
         account.setBalance(54);
         validateCustomer(customer, validationReport);
         assertReportContains(Message.Type.WARNING, "accountBalanceAtLeast", validationReport);

         validationReport = new ValidationReportImpl();
         account.setBalance(122);
         validateCustomer(customer, validationReport);
         assertNotReportContains(Message.Type.WARNING, "accountBalanceAtLeast", validationReport);
     }

    private Customer createBasicCustomer() {
        final Customer customer = new Customer();
         customer.setPhoneNumber("123456789");
         customer.setAddress(new Address());
        return customer;
    }

     @Test
     public void studentAccountCustomerAgeLessThan()
         throws Exception {
         ValidationReport validationReport = new ValidationReportImpl();

         final Customer customer = createBasicCustomer();
         customer.setDateOfBirth(parseDate("1954-02-14"));
         final Account account = new Account();
//         account.setOwner(customer);
         account.setBalance(220);
         account.setType(Account.Type.STUDENT);
         customer.getAccounts().add(account);
         validateCustomer(customer, validationReport);
         assertReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", validationReport);

//         customer.setDateOfBirth(parseDate("2010-02-14"));
//         validationReport = new ValidationReportImpl();
//         validateCustomer(customer, validationReport);
//         assertNotReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", validationReport);

         //         assertEquals(Account.Type.STUDENT,
//            account.getType());
//       assertNotReportContains(Message.Type.ERROR,
//           "studentAccountCustomerAgeLessThan", customer);
//
//       account.setType(Account.Type.STUDENT);
//       assertReportContains(Message.Type.ERROR,
//           "studentAccountCustomerAgeLessThan", customer, account);
//
//       customer.setDateOfBirth(NOW.minusYears(20).toDate());
//       assertNotReportContains(Message.Type.ERROR,
//           "studentAccountCustomerAgeLessThan", customer);
       }
     
    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

}
