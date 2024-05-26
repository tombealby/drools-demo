package com.example.demo.model;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class Customer {

    private String firstName;
    private String lastName;
    private Date dateOfBirth;
    private String uuid;
    private String email;
    private Address address;
    private List<Account> accounts;
    private String phoneNumber;

}
