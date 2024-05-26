package com.example.demo.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.model.Customer;
import com.example.demo.service.CustomerService;

@RestController
public class CustomerController {

    private final CustomerService customerService;

    @Autowired
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @RequestMapping(value = "/validateCustomerHasPhoneNumber", method = RequestMethod.GET, produces = "application/json")
    public Customer validateCustomerHasPhoneNumber(@RequestParam(required = true) String phone) {

        Customer customer = new Customer();
        if (phone != null && phone.length() > 0) {
            customer.setPhoneNumber(phone);
        }

        customerService.validateCustomer(customer);
        return customer;
    }

}
