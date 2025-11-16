package com.example.avto.models;

import java.util.Date;

public class Deal {
    private String id; // Оставляем String для совместимости
    private String carName;
    private Date date;
    private double amount;
    private String clientName;

    public Deal(String id, String carName, Date date, double amount, String clientName) {
        this.id = id;
        this.carName = carName;
        this.date = date;
        this.amount = amount;
        this.clientName = clientName;
    }

    // Getters
    public String getId() { return id; }
    public String getCarName() { return carName; }
    public Date getDate() { return date; }
    public double getAmount() { return amount; }
    public String getClientName() { return clientName; }
}