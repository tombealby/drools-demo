package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Account;
import com.example.demo.model.Product;
import com.example.demo.service.AccountsService;
import com.example.demo.service.JewelleryShopService;

@RestController
public class AccountsController {

    private final AccountsService accountsService;

    @Autowired
    public AccountsController(AccountsService accountsService) {
        this.accountsService = accountsService;
    }

    @RequestMapping(value = "/getAccount", method = RequestMethod.GET, produces = "application/json")
    public Account getAccount(@RequestParam(required = true) String balance) {

        Account account = new Account();
        account.setBalance(Long.parseLong(balance));
        accountsService.printAccountsLessThan100(account);
        return account;
    }

}
