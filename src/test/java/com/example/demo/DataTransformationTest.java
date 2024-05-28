package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.commands.runtime.rule.FireAllRulesCommand;
import org.drools.commands.runtime.rule.GetObjectsCommand;
import org.drools.core.ClassObjectSerializationFilter;
import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;

import com.example.demo.model.Account;
import com.example.demo.model.Address;
import com.example.demo.model.Customer;
import com.example.demo.report.Message;
import com.example.demo.report.Message.Type;
import com.example.demo.report.ReportFactoryImpl;
import com.example.demo.report.ValidationReport;
import com.example.demo.report.ValidationReportImpl;
import com.example.demo.service.BankingInquiryService;
import com.example.demo.service.LegacyBankService;

public class DataTransformationTest {

    static KieContainer kieContainer;
    static ReportFactoryImpl reportFactory = new ReportFactoryImpl();
    private static final String DATA_TRANSFORMATION_DRL = "rules/dataTransformation.drl";

    @BeforeAll
    public static void setUpClass() throws Exception {

        KieServices kieServices = KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource(DATA_TRANSFORMATION_DRL));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());

    }

//    @Test
//    public void addressRequired() throws Exception {
//      ValidationReport validationReport = new ValidationReportImpl();
//      final Customer customer = new Customer();
//      customer.setPhoneNumber("123456789");
//      customer.setDateOfBirth(parseDate("1954-02-14"));
//      assertNull(customer.getAddress());
//      executeDataTransform(customer, validationReport);
//      assertReportContains(Message.Type.WARNING, "addressRequired", validationReport);
//
//      validationReport = new ValidationReportImpl();
//      customer.setAddress(new Address());
//      executeDataTransform(customer, validationReport);
//      assertNotReportContains(Message.Type.WARNING, "addressRequired", validationReport);
//     }

    @Test
    public void twoEqualAddressesDifferentInstance()
        throws Exception {
        final KieSession kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("legacyService", new MockLegacyBankService());
        kieSession.setGlobal("reportFactory", reportFactory);
        kieSession.setGlobal("validationReport", reportFactory.createValidationReport());

      Map addressMap1 = new HashMap();
      addressMap1.put("_type_", "Address");
      addressMap1.put("street", "Barrack Street");

      Map addressMap2 = new HashMap();
      addressMap2.put("_type_", "Address");
      addressMap2.put("street", "Barrack Street");
      assertEquals(addressMap1, addressMap2);

      ExecutionResults results = execute(Arrays.asList(
          addressMap1, addressMap2),
          "twoEqualAddressesDifferentInstance", "Address",
          "addresses",kieSession);

      List<?> addresses = ((List<?>) results
          .getValue("addresses"));
      Map addressMapwinner = (Map) addresses.get(0);
      assertEquals(addressMap1, addressMapwinner);
      assertEquals(1, addresses.size());
//      reportContextContains(results,
//          "twoEqualAddressesDifferentInstance",
//          addressMapwinner == addressMap1 ? addressMap2
//              : addressMap1);
      
//      Iterator<?> addressIterator = ((List<?>) results
//              .getValue("addresses")).iterator();
//          Map addressMapwinner = (Map) addressIterator.next();
//          assertEquals(addressMap1, addressMapwinner);
//          assertFalse(addressIterator.hasNext());
//          reportContextContains(results,
//              "twoEqualAddressesDifferentInstance",
//              addressMapwinner == addressMap1 ? addressMap2
//                  : addressMap1);
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

     public void executeDataTransform(final Customer customer, final ValidationReport validationReport) {
         final KieSession kieSession = kieContainer.newKieSession();
         kieSession.setGlobal("legacyService", new MockLegacyBankService());
         kieSession.insert(customer);
         setGlobalsAndFire(validationReport, kieSession);
     }
     
     /**
      * creates multiple commands, calls session.execute and
      * returns results back
      */
     protected ExecutionResults execute(List objects, String ruleName, final String filterType, String filterOut,
             KieSession kieSession) {
         ValidationReport validationReport = reportFactory.createValidationReport();
         List<Command<?>> commands = new ArrayList<Command<?>>();
         commands.add(CommandFactory.newSetGlobal("validationReport", validationReport, true));
         commands.add(CommandFactory.newSetGlobal("legacyService", new MockLegacyBankService(), true));
         commands.add(CommandFactory.newSetGlobal("reportFactory", reportFactory, true));
         commands.add(CommandFactory.newInsertElements(objects));
         commands.add(new FireAllRulesCommand(new RuleNameEqualsAgendaFilter(ruleName)));
         if (filterType != null && filterOut != null) {
             // org.drools.core.ClassObjectSerializationFilter
             GetObjectsCommand getObjectsCommand = new GetObjectsCommand(new ClassObjectSerializationFilter() {
                 @Override
                 public boolean accept(Object object) {
                     return object instanceof Map && ((Map) object).get("_type_").equals(filterType);
                 }
             });
//         GetObjectsCommand getObjectsCommand =
//           new GetObjectsCommand( new ObjectFilter() {
//               public boolean accept(Object object) {
//                 return object instanceof Map
//                     && ((Map) object).get("_type_").equals(
//                          filterType);
//                 }
//               });
             getObjectsCommand.setOutIdentifier(filterOut);
             commands.add(getObjectsCommand);
         }
         ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(commands));
         return results;
     }

//     @Test
//     public void accountOwnerRequired() throws Exception {
//         ValidationReport validationReport = new ValidationReportImpl();
//         final Account account = new Account();
//         assertNull(account.getOwner());
//         validateAccount(account, validationReport);
//         assertReportContains(Message.Type.ERROR, "accountOwnerRequired", validationReport);
//
//         validationReport = new ValidationReportImpl();
//         account.setOwner(new Customer());
//         validateAccount(account, validationReport);
//         assertNotReportContains(Message.Type.ERROR, "accountOwnerRequired", validationReport);
//     }

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

//     @Test
//     public void accountBalanceAtLeast() throws Exception {
//         ValidationReport validationReport = new ValidationReportImpl();
//         final Customer customer = createBasicCustomer();
//         customer.setDateOfBirth(parseDate("1954-02-14"));
//         final Account account = new Account();
//         account.setOwner(new Customer());
//         account.setBalance(0);
//         customer.getAccounts().add(account);
//         executeDataTransform(customer, validationReport);
//         assertReportContains(Message.Type.WARNING, "accountBalanceAtLeast", validationReport);
//
//         validationReport = new ValidationReportImpl();
//         account.setBalance(54);
//         executeDataTransform(customer, validationReport);
//         assertReportContains(Message.Type.WARNING, "accountBalanceAtLeast", validationReport);
//
//         validationReport = new ValidationReportImpl();
//         account.setBalance(122);
//         executeDataTransform(customer, validationReport);
//         assertNotReportContains(Message.Type.WARNING, "accountBalanceAtLeast", validationReport);
//     }

    private Customer createBasicCustomer() {
        final Customer customer = new Customer();
         customer.setPhoneNumber("123456789");
         customer.setAddress(new Address());
        return customer;
    }

//    @Test
//    public void studentAccountCustomerAgeLessThan() throws Exception {
//        ValidationReport validationReport = new ValidationReportImpl();
//
//        final Customer customer = createBasicCustomer();
//        customer.setDateOfBirth(parseDate("1954-02-14"));
//        final Account account = new Account();
////         account.setOwner(customer);
//        account.setBalance(220);
//        account.setType(Account.Type.STUDENT);
//        customer.getAccounts().add(account);
//        executeDataTransform(customer, validationReport);
//        assertReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", validationReport);
//
//        customer.setDateOfBirth(parseDate("2010-02-14"));
//        validationReport = new ValidationReportImpl();
//        executeDataTransform(customer, validationReport);
//        assertNotReportContains(Message.Type.ERROR, "studentAccountCustomerAgeLessThan", validationReport);
//    }
     
    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

    class MockLegacyBankService implements LegacyBankService {

        @Override
        public List<Map<String, Object>> findAllCustomers() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Map<String, Object>> findAddressByCustomerId(Long customerId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public List<Map<String, Object>> findAccountByCustomerId(Long customerId) {
            // TODO Auto-generated method stub
            return null;
        }

    }

}
