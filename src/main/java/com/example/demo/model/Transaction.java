package com.example.demo.model;

import java.util.Date;

import lombok.Data;

@Data
public class Transaction {

    private Account accountFrom;
    private Account accountTo;
    private String status;
    private long amount;
    private String description;
    private String currency;
    private Date date;
    private String uuid;

}
