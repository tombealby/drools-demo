package com.example.demo;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.runtime.KieContainer;
import org.kie.internal.io.ResourceFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SpringBootDroolsHelloWorldApp {
    
    private static final String RULES_DRL = "rules/rules.drl";
    private static final String ACCOUNTS_DRL = "rules/accounts.drl";
    private static final String CHECK_PHONE_NUMBER_DRL = "rules/checkCustomerPhoneNumber.drl";
    
    public static void main(String[] args) {
        SpringApplication.run(SpringBootDroolsHelloWorldApp.class, args);

    }

    @Bean
    public KieContainer kieContainer() {
        
        KieServices kieServices = KieServices.Factory.get();

        KieFileSystem kieFileSystem = kieServices.newKieFileSystem();
        kieFileSystem.write(ResourceFactory.newClassPathResource(RULES_DRL));
        kieFileSystem.write(ResourceFactory.newClassPathResource(CHECK_PHONE_NUMBER_DRL));
//        kieFileSystem.write(ResourceFactory.newClassPathResource(accountsDrl));
        KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);
        kieBuilder.buildAll();
        KieModule kieModule = kieBuilder.getKieModule();

        return kieServices.newKieContainer(kieModule.getReleaseId());
    }

}
