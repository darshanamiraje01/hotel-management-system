package com.hotel.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Reservation {

    public enum Status { CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED }

    private int         id;
    private int         clientId;
    private int         roomId;
    private int         employeeId;
    private LocalDate   checkInDate;
    private LocalDate   checkOutDate;
    private Status      status;
    private String      specialRequests;
    private LocalDateTime createdAt;

    // Display-join fields
    private String clientName;
    private String roomNumber;
    private String roomTypeName;
    private double pricePerNight;

    public Reservation() {}

    public Reservation(int clientId, int roomId, int employeeId,
                       LocalDate checkIn, LocalDate checkOut, String specialRequests) {
        this.clientId        = clientId;
        this.roomId          = roomId;
        this.employeeId      = employeeId;
        this.checkInDate     = checkIn;
        this.checkOutDate    = checkOut;
        this.specialRequests = specialRequests;
        this.status          = Status.CONFIRMED;
    }

    /** Number of nights between check-in and check-out. */
    public long getNights() {
        if (checkInDate == null || checkOutDate == null) return 0;
        return ChronoUnit.DAYS.between(checkInDate, checkOutDate);
    }

    // ── Getters & Setters ───────────────────────────────────────────
    public int getId()                            { return id; }
    public void setId(int id)                     { this.id = id; }

    public int getClientId()                      { return clientId; }
    public void setClientId(int v)                { this.clientId = v; }

    public int getRoomId()                        { return roomId; }
    public void setRoomId(int v)                  { this.roomId = v; }

    public int getEmployeeId()                    { return employeeId; }
    public void setEmployeeId(int v)              { this.employeeId = v; }

    public LocalDate getCheckInDate()             { return checkInDate; }
    public void setCheckInDate(LocalDate v)       { this.checkInDate = v; }

    public LocalDate getCheckOutDate()            { return checkOutDate; }
    public void setCheckOutDate(LocalDate v)      { this.checkOutDate = v; }

    public Status getStatus()                     { return status; }
    public void setStatus(Status status)          { this.status = status; }
    public void setStatus(String s)               { this.status = Status.valueOf(s); }

    public String getSpecialRequests()            { return specialRequests; }
    public void setSpecialRequests(String v)      { this.specialRequests = v; }

    public LocalDateTime getCreatedAt()           { return createdAt; }
    public void setCreatedAt(LocalDateTime v)     { this.createdAt = v; }

    public String getClientName()                 { return clientName; }
    public void setClientName(String v)           { this.clientName = v; }

    public String getRoomNumber()                 { return roomNumber; }
    public void setRoomNumber(String v)           { this.roomNumber = v; }

    public String getRoomTypeName()               { return roomTypeName; }
    public void setRoomTypeName(String v)         { this.roomTypeName = v; }

    public double getPricePerNight()              { return pricePerNight; }
    public void setPricePerNight(double v)        { this.pricePerNight = v; }
}
