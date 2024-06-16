package com.example.demo.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.commands.runtime.rule.FireAllRulesCommand;
import org.drools.core.base.RuleNameEqualsAgendaFilter;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.QueryResults;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.internal.command.CommandFactory;
import org.kie.internal.io.ResourceFactory;

import com.example.demo.model.Account;
import com.example.demo.model.Customer;
import com.example.demo.report.Message.Type;
import com.example.demo.report.ReportFactoryImpl;
import com.example.demo.report.ValidationReport;

public class DataTransformationServiceImpl {

    private ReportFactoryImpl reportFactory = new ReportFactoryImpl();
    private KieSession kieSession;
    private KieContainer kieContainer;
    private static final String DATA_TRANSFORMATION_DRL = "rules/dataTransformation.drl";

    public DataTransformationServiceImpl() throws Exception {

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

    /**
     * transforms customerMap, creates and stores new customer
     */
    public Customer processCustomer(Map customerMap) {
        Customer customer = new Customer();
        ValidationReport validationReport = reportFactory.createValidationReport();

        List<Command<?>> commands = new ArrayList<Command<?>>();
        commands.add(CommandFactory.newSetGlobal("validationReport", validationReport));
        commands.add(CommandFactory.newInsert(customerMap));
        commands.add(new FireAllRulesCommand(new RuleNameEqualsAgendaFilter("addAccountMapToDroolsSession")));
        commands.add(CommandFactory.newQuery("accounts", "getAccountByCustomerId", new Object[] { customerMap }));
        ExecutionResults results = kieSession.execute(CommandFactory.newBatchExecution(commands));

        if (!validationReport.getMessagesByType(Type.ERROR).isEmpty()) {
            System.out.println("Error in validation report.");
        } else {
            customer = buildCustomer(customerMap, results);
            System.out.println("Customer created :" + customer);
        }
        System.out.println("processCustomer returning customer:" + customer);
        return customer;
    }

    private Customer buildCustomer(final Map customerMap, final ExecutionResults results) {
        final Customer customer = new Customer();
        final QueryResults accountQueryResults = (QueryResults) results.getValue("accounts");
        for (QueryResultsRow accountQueryResult : accountQueryResults) {
            Map accountMap = (Map) accountQueryResult.get("$accountMap");
            Account account = new Account();
            account.setCurrency((String) accountMap.get("currency"));
            account.setBalance(new BigDecimal((String)accountMap.get("balance")).longValue());
            customer.getAccounts().add(account);
        }
        return customer;
    }

}
