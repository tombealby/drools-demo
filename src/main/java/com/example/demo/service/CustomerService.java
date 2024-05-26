package com.example.demo.service;

import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.demo.model.Customer;
import com.example.demo.model.Product;

@Service
public class CustomerService {

    private final KieContainer kieContainer;

    @Autowired
    public CustomerService(KieContainer kieContainer) {
        this.kieContainer = kieContainer;
    }

    public Customer validateCustomer(Customer customer) {
        //get the stateful session
        KieSession kieSession = kieContainer.newKieSession(); //kieContainer.newKieSession("rulesSession");
        kieSession.insert(customer);
        kieSession.fireAllRules();
        kieSession.dispose();
        return customer;
    }

}
