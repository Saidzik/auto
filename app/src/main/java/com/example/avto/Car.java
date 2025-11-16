package com.example.avto.models;

public class Car {
    private String id; // Оставляем String для совместимости
    private String brand;
    private String model;
    private int year;
    private String vin;
    private double price;
    private int mileage;
    private String status;
    private String imageUrl;

    public Car(String id, String brand, String model, int year, String vin,
               double price, int mileage, String status, String imageUrl) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.vin = vin;
        this.price = price;
        this.mileage = mileage;
        this.status = status;
        this.imageUrl = imageUrl;
    }

    // Getters
    public String getId() { return id; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public String getVin() { return vin; }
    public double getPrice() { return price; }
    public int getMileage() { return mileage; }
    public String getStatus() { return status; }
    public String getImageUrl() { return imageUrl; }

    public String getFullName() {
        return brand + " " + model;
    }
}