package com.example.demo.model;

import java.util.Date;

import lombok.Data;

@Data
public class Account {

    private String number;
    private String name;
    private long balance;
    private String currency;
    private Date startDate;
    private Date endDate;
    private String type;
    private double interestRate;
    private String uuid;
    private String status;
    private Customer owner;

}
