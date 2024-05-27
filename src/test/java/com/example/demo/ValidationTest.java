package com.example.demo;

import static org.junit.Assert.assertNull;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import java.util.Set;

import com.example.demo.model.Account;
import com.example.demo.model.Customer;
import com.example.demo.model.Address;
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
    private static final String ACCOUNTS_DRL = "rules/accounts.drl";
    private static final String CHECK_PHONE_NUMBER_DRL = "rules/checkCustomerPhoneNumber.drl";
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

//    @Test
//    public void test() {
//        validate(new Customer(), new Account());
//    }
    
    @Test
    public void addressRequired() throws Exception {
      ValidationReport validationReport = new ValidationReportImpl();
      Customer customer = new Customer();
      validate(customer, new Account(), validationReport);
      assertNull(customer.getAddress());
      assertReportContains(Message.Type.WARNING,
         "addressRequired", customer, validationReport);

      customer.setAddress(new Address());
      validate(customer, new Account(), validationReport);
      assertNotReportContains(Message.Type.WARNING,
         "addressRequired", customer, validationReport);
     }

     private void assertReportContains(Type warning, String string, Customer customer,
             ValidationReport validationReport) {
//        ValidationReport report =
//                reportFactory.createValidationReport();
//           List<Command> commands = new ArrayList<Command>();
//           commands.add(CommandFactory.newSetGlobal(
//               "validationReport", report));
//           commands.add(CommandFactory
//               .newInsertElements(getFacts(customer)));
//           session.execute(CommandFactory
//               .newBatchExecution(commands));
         System.out.println("************assertReportContains***************");
         Set<Message> messages = validationReport.getMessages();
         for (Message m : messages) {
             System.out.println("message type:" + m.getType());
             System.out.println("message key:" + m.getMessageKey());
             System.out.println("message ordered context:" + m.getContextOrdered());
         }

//           assertTrue("Report doesn't contain message [" + messageKey
//               + "]", report.contains(messageKey));
//           Message message = getMessage(report, messageKey);
//           assertEquals(Arrays.asList(context),
//               message.getContextOrdered());

     }

     private void assertNotReportContains(Type warning, String string, Customer customer,
             ValidationReport validationReport) {
         System.out.println("************assertNotReportContains***************");
         Set<Message> messages = validationReport.getMessages();
         for (Message m : messages) {
             System.out.println("message type:" + m.getType());
             System.out.println("message key:" + m.getMessageKey());
             System.out.println("message ordered context:" + m.getContextOrdered());
         }

     }

    public void validate(Customer customer, Account account, ValidationReport validationReport) {
        KieSession kieSession = kieContainer.newKieSession(); //kieContainer.newKieSession("rulesSession");
        kieSession.insert(customer);
        kieSession.insert(account);
        kieSession.setGlobal("validationReport", validationReport);
        kieSession.setGlobal("reportFactory", new ReportFactoryImpl());
        kieSession.setGlobal("inquiryService", new BankingInquiryService(kieContainer));
        kieSession.fireAllRules();
        kieSession.dispose();
      }

}
