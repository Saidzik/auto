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

    // Setters
    public void setId(int id) { this.id = id; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setPosition(String position) { this.position = position; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }
    public void setHireDate(String hireDate) { this.hireDate = hireDate; }
    public void setSalary(double salary) { this.salary = salary; }
    public void setDepartment(String department) { this.department = department; }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    // Пустой конструктор
    public Employee() {}
    // В класс Employee.java добавьте этот метод:

    // Метод для установки полного имени (разделяет на имя и фамилию)
    public void setFullName(String fullName) {
        if (fullName != null && !fullName.trim().isEmpty()) {
            String[] parts = fullName.trim().split("\\s+", 2);
            this.firstName = parts[0];
            this.lastName = parts.length > 1 ? parts[1] : "";
        }
    }
}