package com.example.demo;

import java.util.Set;

import org.drools.kiesession.rulebase.InternalKnowledgeBase;
import org.drools.kiesession.rulebase.KnowledgeBaseFactory;
//import org.drools.serialization.protobuf.ProtobufMessages.KnowledgeBase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.DecisionTableConfiguration;
import org.kie.internal.builder.DecisionTableInputType;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;

import com.example.demo.model.Customer;
import com.example.demo.report.Message;
import com.example.demo.report.ReportFactoryImpl;
import com.example.demo.report.ValidationReport;

public class DecisionTableTest {

    static KieContainer kieContainer;
    static KieSession kieSession;
    static ReportFactoryImpl reportFactory = new ReportFactoryImpl();
    static ValidationReport validationReport;
    private static final String CUSTOMER_XLSX = "rules/customer.xlsx";

    @BeforeAll
    public static void setUpClass() throws Exception {

//        KieServices kieServices = KieServices.Factory.get();
//        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
//        kieFileSystem.write(ResourceFactory.newClassPathResource(CUSTOMER_XLSX));
//        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
//        kieBuilder.buildAll();
//        KieModule kieModule = kieBuilder.getKieModule();
//        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        InternalKnowledgeBase nb = createKnowledgeBaseFromSpreadsheet();
        kieSession = nb.newKieSession();
        kieSession.setGlobal("reportFactory", reportFactory);
        validationReport = reportFactory.createValidationReport();
        kieSession.setGlobal("validationReport", validationReport);
//        kieSession = kieContainer.newKieSession();
    }

    @Test
    public void executeDecisionTabelRule() {
        final Customer customer = new Customer();
        customer.setFirstName("David");
        customer.setPhoneNumber("1236174");
        kieSession.insert(customer);
        kieSession.fireAllRules();
        Set<Message> messages = validationReport.getMessages();
        for (Message message: messages) {
            System.out.println("message key:" + message.getMessageKey());
            System.out.println("message type:" + message.getType());
        }
    }
    
    private static InternalKnowledgeBase createKnowledgeBaseFromSpreadsheet()
            throws Exception {
          DecisionTableConfiguration dtconf =KnowledgeBuilderFactory
            .newDecisionTableConfiguration();
          dtconf.setInputType( DecisionTableInputType.XLS );
          //dtconf.setInputType( DecisionTableInputType.CSV );
          KnowledgeBuilder knowledgeBuilder =
            KnowledgeBuilderFactory.newKnowledgeBuilder();
          knowledgeBuilder.add(ResourceFactory.newClassPathResource(
                  CUSTOMER_XLSX), ResourceType.DTABLE,
            dtconf);
          //knowledgeBuilder.add(ResourceFactory
          // .newClassPathResource("interest calculation.csv"),
          // ResourceType.DTABLE, dtconf);

          if (knowledgeBuilder.hasErrors()) {
            throw new RuntimeException(knowledgeBuilder.getErrors()
                 .toString());
          }
          InternalKnowledgeBase knowledgeBase = KnowledgeBaseFactory
            .newKnowledgeBase();
          knowledgeBase.addPackages(
              knowledgeBuilder.getKnowledgePackages());
          return knowledgeBase;
    }

//    @Test
//    public void dontGreetTom() {
//        final Customer customer = new Customer();
//        customer.setFirstName("Tom");
//        kieSession.insert(customer);
//        kieSession.fireAllRules();
//    }

}
