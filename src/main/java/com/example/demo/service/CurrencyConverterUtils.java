package com.example.demo.service;

public class CurrencyConverterUtils {

    public static String getConversionToEurFrom(Object currencyFrom) {
        String conversion = null;
        if ("USD".equals(currencyFrom)) {
            conversion = "0.670";
        } else if ("SKK".equals(currencyFrom)) {
            conversion = "0.033";
        }
        return conversion;
    }

}
