package com.hotel.model;

import java.math.BigDecimal;

public class RoomType {
    private int id;
    private String name;
    private BigDecimal pricePerNight;
    private String description;

    public RoomType() {}

    public RoomType(String name, BigDecimal pricePerNight, String description) {
        this.name          = name;
        this.pricePerNight = pricePerNight;
        this.description   = description;
    }

    public int getId()                       { return id; }
    public void setId(int id)                { this.id = id; }

    public String getName()                  { return name; }
    public void setName(String name)         { this.name = name; }

    public BigDecimal getPricePerNight()     { return pricePerNight; }
    public void setPricePerNight(BigDecimal p) { this.pricePerNight = p; }

    public String getDescription()           { return description; }
    public void setDescription(String d)     { this.description = d; }

    @Override public String toString()       { return name + " (₹" + pricePerNight + "/night)"; }
}
