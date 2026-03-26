package com.hotel.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Billing {

    public enum PaymentMethod { CASH, CARD, UPI, ONLINE }

    private int           id;
    private int           reservationId;
    private int           nights;
    private BigDecimal    roomCharge;
    private BigDecimal    extraCharges;
    private BigDecimal    discount;
    private BigDecimal    totalAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paidAt;

    // Display fields
    private String clientName;
    private String roomNumber;

    public Billing() {}

    public Billing(int reservationId, int nights, BigDecimal roomCharge,
                   BigDecimal extraCharges, BigDecimal discount,
                   PaymentMethod paymentMethod) {
        this.reservationId = reservationId;
        this.nights        = nights;
        this.roomCharge    = roomCharge;
        this.extraCharges  = extraCharges;
        this.discount      = discount;
        this.paymentMethod = paymentMethod;
        this.totalAmount   = roomCharge.add(extraCharges).subtract(discount);
    }

    public int getId()                          { return id; }
    public void setId(int id)                   { this.id = id; }

    public int getReservationId()               { return reservationId; }
    public void setReservationId(int v)         { this.reservationId = v; }

    public int getNights()                      { return nights; }
    public void setNights(int v)                { this.nights = v; }

    public BigDecimal getRoomCharge()           { return roomCharge; }
    public void setRoomCharge(BigDecimal v)     { this.roomCharge = v; }

    public BigDecimal getExtraCharges()         { return extraCharges; }
    public void setExtraCharges(BigDecimal v)   { this.extraCharges = v; }

    public BigDecimal getDiscount()             { return discount; }
    public void setDiscount(BigDecimal v)       { this.discount = v; }

    public BigDecimal getTotalAmount()          { return totalAmount; }
    public void setTotalAmount(BigDecimal v)    { this.totalAmount = v; }

    public PaymentMethod getPaymentMethod()     { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod v) { this.paymentMethod = v; }
    public void setPaymentMethod(String v)      { this.paymentMethod = PaymentMethod.valueOf(v); }

    public LocalDateTime getPaidAt()            { return paidAt; }
    public void setPaidAt(LocalDateTime v)      { this.paidAt = v; }

    public String getClientName()               { return clientName; }
    public void setClientName(String v)         { this.clientName = v; }

    public String getRoomNumber()               { return roomNumber; }
    public void setRoomNumber(String v)         { this.roomNumber = v; }
}
