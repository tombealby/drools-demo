package com.example.demo;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;

import com.example.demo.model.Customer;
import com.example.demo.report.ReportFactoryImpl;

public class CustomerGreetingTest {

    static KieContainer kieContainer;
    static KieSession kieSession;
    static ReportFactoryImpl reportFactory = new ReportFactoryImpl();
    private static final String SIMPLE_DSL = "rules/simple.dsl";
    private static final String SIMPLE_DSLR = "rules/simple.dslr";

    @BeforeAll
    public static void setUpClass() throws Exception {

        KieServices kieServices = KieServices.Factory.get();
        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource(SIMPLE_DSL));
        kieFileSystem.write(ResourceFactory.newClassPathResource(SIMPLE_DSLR));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();
        kieContainer = kieServices.newKieContainer(kieModule.getReleaseId());
        kieSession = kieContainer.newKieSession();
    }

    @Test
    public void greetDavid() {
        final Customer customer = new Customer();
        customer.setFirstName("David");
        kieSession.insert(customer);
        kieSession.fireAllRules();
    }

    @Test
    public void dontGreetTom() {
        final Customer customer = new Customer();
        customer.setFirstName("Tom");
        kieSession.insert(customer);
        kieSession.fireAllRules();
    }

}
