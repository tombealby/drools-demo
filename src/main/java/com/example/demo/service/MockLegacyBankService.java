package com.example.demo.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.demo.model.Address;
import com.example.demo.model.Customer;

public class MockLegacyBankService implements LegacyBankService {

    @Override
    public List<Map<String, Object>> findAllCustomers() {

        Map<String, Object> customerMap1 = new HashMap<>();
        Customer customer1 = createBasicCustomer();
        customerMap1.put("Customer", customer1);
        customerMap1.put("customer_id", 1);

        Map<String, Object> customerMap2 = new HashMap<>();
        Customer customer2 = createBasicCustomer();
        customerMap2.put("Customer", customer2);
        customerMap2.put("customer_id", 2);

        List<Map<String, Object>> allCustomers = new ArrayList<>();
        allCustomers.add(customerMap1);
        allCustomers.add(customerMap2);
        return allCustomers;
    }

    @Override
    public Map<String, Object> findAddressByCustomerId(Long customerId) {

        Map<String, Object> addressMap1 = new HashMap<>();
        addressMap1.put("_type_", "Address");
        addressMap1.put("street", "Barrack Street");

        Map<String, Object> addressMap2 = new HashMap<>();
        addressMap2.put("_type_", "Address");
        addressMap2.put("street", "Barrack Street");

        List<Map<String, Object>> addresesForCustomer = new ArrayList<>();
        addresesForCustomer.add(addressMap1);
        addresesForCustomer.add(addressMap2);

        if (customerId == 1) {
            return addressMap1;
        } else {
            return addressMap2;
        }
    }

    @Override
    public List<Map<String, Object>> findAccountByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return null;
    }

    private Customer createBasicCustomer() {
        final Customer customer = new Customer();
        customer.setPhoneNumber("123456789");
        customer.setAddress(new Address());
        return customer;
    }

}
