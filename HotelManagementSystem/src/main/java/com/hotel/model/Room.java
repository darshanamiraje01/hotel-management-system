package com.hotel.model;

public class Room {

    public enum Status { AVAILABLE, OCCUPIED, MAINTENANCE }

    private int    id;
    private String roomNumber;
    private int    floor;
    private int    roomTypeId;
    private String roomTypeName;       // joined from room_types for display
    private double pricePerNight;      // joined for display
    private Status status;

    public Room() {}

    public Room(String roomNumber, int floor, int roomTypeId) {
        this.roomNumber = roomNumber;
        this.floor      = floor;
        this.roomTypeId = roomTypeId;
        this.status     = Status.AVAILABLE;
    }

    // ── Getters & Setters ───────────────────────────────────────────
    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getRoomNumber()            { return roomNumber; }
    public void setRoomNumber(String rn)     { this.roomNumber = rn; }

    public int getFloor()                    { return floor; }
    public void setFloor(int floor)          { this.floor = floor; }

    public int getRoomTypeId()               { return roomTypeId; }
    public void setRoomTypeId(int t)         { this.roomTypeId = t; }

    public String getRoomTypeName()          { return roomTypeName; }
    public void setRoomTypeName(String n)    { this.roomTypeName = n; }

    public double getPricePerNight()         { return pricePerNight; }
    public void setPricePerNight(double p)   { this.pricePerNight = p; }

    public Status getStatus()                { return status; }
    public void setStatus(Status status)     { this.status = status; }
    public void setStatus(String s)          { this.status = Status.valueOf(s); }

    @Override public String toString() {
        return "Room " + roomNumber + " - " + roomTypeName + " [" + status + "]";
    }
}
