package com.example.demo.service;

import java.util.List;
import java.util.Map;

public interface LegacyBankService {

    List<Map<String, Object>> findAllCustomers();

    // keys for map are object property names
    // (for example, addressLine1) and the values are the actual properties
//    List<Map<String, Object>> findAddressByCustomerId(
//         Long customerId);

    Map<String, Object> findAddressByCustomerId(
            Long customerId);

    // keys for map are object property names
    // (for example, addressLine1) and the values are the actual properties
    List<Map<String, Object>> findAccountByCustomerId(
         Long customerId);

}
