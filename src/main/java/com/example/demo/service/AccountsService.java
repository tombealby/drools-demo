package com.example.demo.service;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Account;
import com.example.demo.model.Product;

@Service
public class AccountsService {

    private final KieContainer kieContainer;

    @Autowired
    public AccountsService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public Account printAccountsLessThan100(Account account) {
        //get the stateful session
        KieSession kieSession = kieContainer.newKieSession(); //kieContainer.newKieSession("rulesSession");
        kieSession.insert(account);
        kieSession.fireAllRules();
        kieSession.dispose();
        return account;
    }

}
