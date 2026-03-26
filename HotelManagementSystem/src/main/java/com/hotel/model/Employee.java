package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Employee {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String role;
    private BigDecimal salary;
    private LocalDate hireDate;
    private String username;
    private String password;
    private boolean active;
    private LocalDateTime createdAt;

    public Employee() {}

    public Employee(String name, String email, String phone, String role,
                    BigDecimal salary, LocalDate hireDate, String username, String password) {
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
        this.salary   = salary;
        this.hireDate = hireDate;
        this.username = username;
        this.password = password;
        this.active   = true;
    }

    // ── Getters & Setters ───────────────────────────────────────────
    public int getId()                      { return id; }
    public void setId(int id)               { this.id = id; }

    public String getName()                 { return name; }
    public void setName(String name)        { this.name = name; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public String getPhone()                { return phone; }
    public void setPhone(String phone)      { this.phone = phone; }

    public String getRole()                 { return role; }
    public void setRole(String role)        { this.role = role; }

    public BigDecimal getSalary()           { return salary; }
    public void setSalary(BigDecimal s)     { this.salary = s; }

    public LocalDate getHireDate()          { return hireDate; }
    public void setHireDate(LocalDate d)    { this.hireDate = d; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }

    public boolean isActive()               { return active; }
    public void setActive(boolean active)   { this.active = active; }

    public LocalDateTime getCreatedAt()     { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    @Override public String toString()      { return name + " [" + role + "]"; }
}
