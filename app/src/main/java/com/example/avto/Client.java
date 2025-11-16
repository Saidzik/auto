package com.example.avto.models;

public class Client {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String address;
    private String passportSeries;
    private String passportNumber;
    private String issueDate;
    private String issuedBy;
    private String registrationDate;

    public Client(int id, String firstName, String lastName, String phone, String email,
                  String address, String passportSeries, String passportNumber,
                  String issueDate, String issuedBy, String registrationDate) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.passportSeries = passportSeries;
        this.passportNumber = passportNumber;
        this.issueDate = issueDate;
        this.issuedBy = issuedBy;
        this.registrationDate = registrationDate;
    }

    // Getters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getPassportSeries() { return passportSeries; }
    public String getPassportNumber() { return passportNumber; }
    public String getIssueDate() { return issueDate; }
    public String getIssuedBy() { return issuedBy; }
    public String getRegistrationDate() { return registrationDate; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getPassportInfo() {
        return passportSeries + " " + passportNumber;
    }
}
