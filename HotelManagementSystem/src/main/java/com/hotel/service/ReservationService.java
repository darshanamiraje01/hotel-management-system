package com.hotel.service;

import com.hotel.dao.BillingDAO;
import com.hotel.dao.ReservationDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.Billing;
import com.hotel.model.Billing.PaymentMethod;
import com.hotel.model.Reservation;
import com.hotel.model.Room;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Coordinates reservation and billing operations.
 * Keeps business rules out of the UI and DAO layers.
 */
public class ReservationService {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final RoomDAO        roomDAO        = new RoomDAO();
    private final BillingDAO     billingDAO     = new BillingDAO();

    /** Creates a reservation and marks the room OCCUPIED. */
    public boolean bookRoom(Reservation reservation) {
        if (!isRoomAvailable(reservation.getRoomId(), reservation.getCheckInDate(), reservation.getCheckOutDate())) {
            return false;
        }
        boolean saved = reservationDAO.addReservation(reservation);
        if (saved) {
            roomDAO.updateRoomStatus(reservation.getRoomId(), Room.Status.OCCUPIED);
        }
        return saved;
    }

    /** Processes checkout: creates bill, marks reservation CHECKED_OUT, frees room. */
    public Optional<Billing> checkOut(int reservationId,
                                      BigDecimal extraCharges,
                                      BigDecimal discount,
                                      PaymentMethod paymentMethod) {
        Optional<Reservation> opt = reservationDAO.getReservationById(reservationId);
        if (opt.isEmpty()) return Optional.empty();

        Reservation res = opt.get();
        if (res.getStatus() == Reservation.Status.CHECKED_OUT ||
            res.getStatus() == Reservation.Status.CANCELLED) {
            return Optional.empty();
        }

        long nights     = res.getNights();
        BigDecimal rate = BigDecimal.valueOf(res.getPricePerNight());
        BigDecimal roomCharge = rate.multiply(BigDecimal.valueOf(nights));

        Billing bill = new Billing(reservationId, (int) nights, roomCharge,
                                   extraCharges, discount, paymentMethod);

        boolean billSaved = billingDAO.createBill(bill);
        if (billSaved) {
            reservationDAO.updateStatus(reservationId, Reservation.Status.CHECKED_OUT);
            roomDAO.updateRoomStatus(res.getRoomId(), Room.Status.AVAILABLE);
            return Optional.of(bill);
        }
        return Optional.empty();
    }

    public boolean cancelReservation(int reservationId) {
        Optional<Reservation> opt = reservationDAO.getReservationById(reservationId);
        if (opt.isEmpty()) return false;
        Reservation res = opt.get();
        boolean cancelled = reservationDAO.cancelReservation(reservationId);
        if (cancelled) {
            roomDAO.updateRoomStatus(res.getRoomId(), Room.Status.AVAILABLE);
        }
        return cancelled;
    }

    public List<Room> findAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        return roomDAO.getAvailableRooms(checkIn, checkOut);
    }

    public List<Room> findAvailableRoomsByType(LocalDate checkIn, LocalDate checkOut, int typeId) {
        return roomDAO.getAvailableRoomsByType(checkIn, checkOut, typeId);
    }

    public List<Reservation> getAllReservations()  { return reservationDAO.getAllReservations(); }
    public List<Reservation> getActiveReservations() { return reservationDAO.getActiveReservations(); }

    private boolean isRoomAvailable(int roomId, LocalDate checkIn, LocalDate checkOut) {
        return roomDAO.getAvailableRooms(checkIn, checkOut)
                      .stream()
                      .anyMatch(r -> r.getId() == roomId);
    }
}
