package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Customer;
import com.example.demo.model.Account;
import com.example.demo.service.BankingInquiryService;

@RestController
public class BankingInquiryController {

    private final BankingInquiryService bankingInquiryService;

    @Autowired
    public BankingInquiryController(BankingInquiryService bankingInquiryService) {
        this.bankingInquiryService = bankingInquiryService;
    }

    @RequestMapping(value = "/validateBank", method = RequestMethod.GET, produces = "application/json")
    public Customer validateCustomerHasPhoneNumber() {

        Customer customer = new Customer();
        Account account = new Account();

        bankingInquiryService.validate(customer, account);
        return customer;
    }

}
