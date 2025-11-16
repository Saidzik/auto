package com.example.avto.models;

public class Employee {
    private int id;
    private String firstName;
    private String lastName;
    private String phone;
    private String email;
    private String position;
    private String birthDate;
    private String hireDate;
    private double salary;
    private String department;

    // Конструктор для данных из БД
    public Employee(int id, String firstName, String lastName, String phone, String email,
                    String position, String birthDate, String hireDate, double salary, String department) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phone = phone;
        this.email = email;
        this.position = position;
        this.birthDate = birthDate;
        this.hireDate = hireDate;
        this.salary = salary;
        this.department = department;
    }

    // Getters
    public int getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPosition() { return position; }
    public String getBirthDate() { return birthDate; }
    public String getHireDate() { return hireDate; }
    public double getSalary() { return salary; }
    public String getDepartment() { return department; }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
