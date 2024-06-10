package com.example.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
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
import com.example.demo.model.Country;
import com.example.demo.model.Customer;
import com.example.demo.report.Message;
import com.example.demo.report.Message.Type;
import com.example.demo.report.ReportFactoryImpl;
import com.example.demo.report.ValidationReport;
import com.example.demo.service.BankingInquiryService;
import com.example.demo.service.LegacyBankService;
import com.example.demo.service.MockLegacyBankService;

public class DataTransformationTest {

    static KieContainer kieContainer;
    static KieSession kieSession;
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

        kieSession = kieContainer.newKieSession();
        kieSession.setGlobal("legacyService", new MockLegacyBankService());
        kieSession.setGlobal("reportFactory", reportFactory);
        kieSession.setGlobal("validationReport", reportFactory.createValidationReport());

    }

    @Test
    public void twoEqualAddressesDifferentInstance() throws Exception {


        final Map<String, Object> addressMap1 = new HashMap<>();
        addressMap1.put("_type_", "Address");
        addressMap1.put("street", "Barrack Street");

        final Map<String, Object> addressMap2 = new HashMap<>();
        addressMap2.put("_type_", "Address");
        addressMap2.put("street", "Barrack Street");
        assertEquals(addressMap1, addressMap2);

        final ExecutionResults results = execute(Arrays.asList(addressMap1, addressMap2),
                "twoEqualAddressesDifferentInstance", "Address", "addresses", new MockLegacyBankService());

        final Iterator<?> addressIterator = ((List<?>) results.getValue("addresses")).iterator();
        final Map<String, Object> addressMapwinner = (Map<String, Object>) addressIterator.next();
        assertEquals(addressMap1, addressMapwinner);
        assertFalse(addressIterator.hasNext());
        reportContextContains(results, "twoEqualAddressesDifferentInstance",
                addressMapwinner == addressMap1 ? addressMap2 : addressMap1);

// first got this working with the following code:        
//final ValidationReport validationReport = reportFactory.createValidationReport();
//    transformAddresses(validationReport);
//  assertReportContains(Message.Type.WARNING, "addressRequired", validationReport);
//      List<?> addresses = ((List<?>) results
//          .getValue("addresses"));
//      Map addressMapwinner = (Map) addresses.get(0);
//      assertEquals(addressMap1, addressMapwinner);
//      assertEquals(1, addresses.size());

    }
    
    @Test
    public void addressNormalizationUSA() throws Exception {
        Map<String, String> addressMap = new HashMap<>();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "U.S.A");

        execute(Arrays.asList(addressMap), "addressNormalizationUSA", null, null, new MockLegacyBankService());

        assertEquals(Country.USA, addressMap.get("country"));
    }
    
    /**
     * This test fails if the previous 2 tests are run with it.
     */
//    @Test
//    public void findAddress() throws Exception {
//
//        final Map<String, Object> customerMap = new HashMap<>();
//        customerMap.put("_type_", "Customer");
//        customerMap.put("customer_id", new Long(111));
//
//        final Map<String, Object> addressMap = new HashMap<>();
//        LegacyBankService service = new StaticMockLegacyBankService(addressMap);
//        kieSession.setGlobal("legacyService", service);
//
//        ExecutionResults results = execute(Arrays.asList(customerMap), "findAddress", "Address", "objects", service);
//        assertEquals("Address", addressMap.get("_type_"));
//        Iterator<?> addressIterator = ((List<?>) results.getValue("objects")).iterator();
//        assertEquals(addressMap, addressIterator.next());
//        assertFalse(addressIterator.hasNext());
//        // clean-up
//        kieSession.setGlobal("legacyService", new MockLegacyBankService());
//    }
    
    @Test
    public void unknownCountry() throws Exception {

        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("_type_", "Address");
        addressMap.put("country", "no country");

        ExecutionResults results = execute(Arrays.asList(addressMap), "unknownCountry", null, null,
                new MockLegacyBankService());

        reportContextContains(results, "unknownCountry", addressMap);
    }

    @Test
    public void knownCountry() throws Exception {
        Map<String, Object> addressMap = new HashMap<>();
        addressMap.put("_type_", "Address");
        addressMap.put("country", Country.FRANCE);

        ExecutionResults results = execute(Arrays.asList(addressMap), "unknownCountry", null, null,
                new MockLegacyBankService());

        final ValidationReport validationReport = (ValidationReport) results.getValue("validationReport");
        assertEquals(0, validationReport.getMessages().size());
    }

    @Test
    public void currencyConversionToEUR() throws Exception {
        Map<String, Object> accountMap = new HashMap<>();
        accountMap.put("_type_", "Account");
        accountMap.put("currency", "USD");
        accountMap.put("balance", "1000");

        execute(Arrays.asList(accountMap), "currencyConversionToEUR", null, null, new MockLegacyBankService());

        assertEquals("EUR", accountMap.get("currency"));
        assertEquals(new BigDecimal("670.000"), accountMap.get("balance"));
    }

    /**
     * asserts that the report contains one message with expected context (input parameter)
     */
    void reportContextContains(ExecutionResults results, String messgeKey, Object object) {
        final ValidationReport validationReport = (ValidationReport) results.getValue("validationReport");
        assertEquals(1, validationReport.getMessages().size());
        final Message message = validationReport.getMessages().iterator().next();
        final List<Object> messageContext = message.getContextOrdered();
        assertEquals(1, messageContext.size());
        assertEquals(object, messageContext.iterator().next());
    }

    public void transformAddresses(final ValidationReport validationReport) {
        final KieSession kieSession = kieContainer.newKieSession();
        
        kieSession.setGlobal("validationReport", validationReport);
        kieSession.setGlobal("legacyService", new MockLegacyBankService());
        kieSession.setGlobal("reportFactory", reportFactory);
        Map<String, String> types = new HashMap<>();
        types.put("_type_", "Customer");
        Map<String, Customer> customerMap = new HashMap<>();
        Customer customer = createBasicCustomer();
        customerMap.put(DATA_TRANSFORMATION_DRL, customer);
        kieSession.insert(customer);
        kieSession.fireAllRules();
        kieSession.dispose();
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
             final LegacyBankService legacyService) {
         ValidationReport validationReport = reportFactory.createValidationReport();
         List<Command<?>> commands = new ArrayList<Command<?>>();
         commands.add(CommandFactory.newSetGlobal("validationReport", validationReport, true));
         commands.add(CommandFactory.newSetGlobal("legacyService", legacyService, true));
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
             getObjectsCommand.setOutIdentifier(filterOut);
             commands.add(getObjectsCommand);
         }
         ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(commands));
         return results;
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

    private Customer createBasicCustomer() {
        final Customer customer = new Customer();
         customer.setPhoneNumber("123456789");
         customer.setAddress(new Address());
        return customer;
    }
     
    public static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }

}
