package com.example.demo.service;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Account;
import com.example.demo.model.Customer;

@Service
public class BankingInquiryService {

    private final KieContainer kieContainer;

    @Autowired
    public BankingInquiryService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    boolean isAccountNumberUnique (Account account) {
        return false;
    }

    public void validate(Customer customer, Account account) {
      KieSession kieSession = kieContainer.newKieSession(); //kieContainer.newKieSession("rulesSession");
      kieSession.insert(customer);
      kieSession.insert(account);
      kieSession.fireAllRules();
      kieSession.dispose();
    }

}
