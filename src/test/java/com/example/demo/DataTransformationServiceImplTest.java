package com.example.demo;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.demo.model.Account;
import com.example.demo.model.Customer;
import com.example.demo.service.DataTransformationServiceImpl;
import com.example.demo.service.MockLegacyBankService;

public class DataTransformationServiceImplTest {

    private MockLegacyBankService mockLegacyBankService = new MockLegacyBankService();

    @Test
    public void processCustomer() throws Exception {

        // GIVEN
        Map<String, Object> customerMap = new HashMap<>();
        customerMap = mockLegacyBankService.findAllCustomers().get(0);
        DataTransformationServiceImpl classUnderTest = new DataTransformationServiceImpl();

        // WHEN
        Customer actualCustomer = classUnderTest.processCustomer(customerMap);

        // THEN
        List<Account> actualAccounts = actualCustomer.getAccounts();
        assertEquals(1, actualAccounts.size());
        Account actualAccount = actualAccounts.get(0);
        Integer customerId = (Integer) customerMap.get("customer_id");
        mockLegacyBankService = new MockLegacyBankService();
        Map<String, Object> expectedAccountMap = mockLegacyBankService.findAccountByCustomerId(customerId.longValue());
        System.out.println("First customerId:" + customerId);
        System.out.println("First account:" + actualAccount);
        assertEquals(expectedAccountMap.get("currency"), actualAccount.getCurrency());
        assertEquals(expectedAccountMap.get("balance"), Long.toString(actualAccount.getBalance()));

        // GIVEN
        customerMap = mockLegacyBankService.findAllCustomers().get(1);

        // WHEN
        actualCustomer = classUnderTest.processCustomer(customerMap);

        // THEN
        actualAccounts = actualCustomer.getAccounts();
        assertEquals(1, actualAccounts.size());
        actualAccount = actualAccounts.get(0);
        customerId = (Integer) customerMap.get("customer_id");
        mockLegacyBankService = new MockLegacyBankService();
        expectedAccountMap = mockLegacyBankService.findAccountByCustomerId(customerId.longValue());
        System.out.println("Second customerId:" + customerId);
        System.out.println("Second account:" + actualAccount);
        assertEquals(expectedAccountMap.get("currency"), actualAccount.getCurrency());
        assertEquals(expectedAccountMap.get("balance"), Long.toString(actualAccount.getBalance()));
    }

}
