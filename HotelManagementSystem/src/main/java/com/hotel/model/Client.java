package com.hotel.model;

import java.time.LocalDateTime;

public class Client {
    private int id;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String idProof;
    private LocalDateTime createdAt;

    public Client() {}

    public Client(String name, String email, String phone, String address, String idProof) {
        this.name    = name;
        this.email   = email;
        this.phone   = phone;
        this.address = address;
        this.idProof = idProof;
    }

    // ── Getters & Setters ───────────────────────────────────────────
    public int getId()                    { return id; }
    public void setId(int id)             { this.id = id; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getPhone()              { return phone; }
    public void setPhone(String phone)    { this.phone = phone; }

    public String getAddress()            { return address; }
    public void setAddress(String v)      { this.address = v; }

    public String getIdProof()            { return idProof; }
    public void setIdProof(String v)      { this.idProof = v; }

    public LocalDateTime getCreatedAt()   { return createdAt; }
    public void setCreatedAt(LocalDateTime v) { this.createdAt = v; }

    @Override public String toString()    { return name + " (" + phone + ")"; }
}
