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
    private String color;
    private String equipment;

    // Конструктор по умолчанию
    public Car() {
        // Инициализация по умолчанию
    }

    // Существующий конструктор
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

    // Getters для новых полей
    public String getColor() { return color; }
    public String getEquipment() { return equipment; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setYear(int year) { this.year = year; }
    public void setVin(String vin) { this.vin = vin; }
    public void setPrice(double price) { this.price = price; }
    public void setMileage(int mileage) { this.mileage = mileage; }
    public void setStatus(String status) { this.status = status; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setColor(String color) { this.color = color; }
    public void setEquipment(String equipment) { this.equipment = equipment; }

    public String getFullName() {
        return brand + " " + model;
    }
    public String getFormattedPrice() {
        return String.format("%,.0f ₽", price);
    }
}