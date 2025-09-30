package com.example.caremitra;

public class Transaction {
    public enum Type {
        DEPOSIT, PAYMENT, REFUND
    }

    private String description;
    private String date;
    private double amount;
    private Type transactionType;
    private String hospitalName;

    public Transaction(String description, String date, double amount, Type transactionType, String hospitalName) {
        this.description = description;
        this.date = date;
        this.amount = amount;
        this.transactionType = transactionType;
        this.hospitalName = hospitalName;
    }

    // Add getters for all fields
    public String getDescription() { return description; }
    public String getDate() { return date; }
    public double getAmount() { return amount; }
    public Type getTransactionType() { return transactionType; }
    public String getHospitalName() { return hospitalName; }
}