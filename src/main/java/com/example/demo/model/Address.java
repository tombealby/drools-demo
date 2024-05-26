package com.example.demo.model;

import lombok.Data;

@Data
public class Address {

    private String addressLine1;
    private String addressLine2;
    private String postCode;
    private String city;
    private String country;
    private String uuid;

}
