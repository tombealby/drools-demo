package com.example.demo.service;

import java.util.Map;

public class StaticMockLegacyBankService extends MockLegacyBankService {

    private Map<String, Object> addressMap;

    public StaticMockLegacyBankService(Map<String, Object> addressMap) {
        this.addressMap = addressMap;
    }

    public Map<String, Object> findAddressByCustomerId(Long customerId) {
        return addressMap;
    }

}
