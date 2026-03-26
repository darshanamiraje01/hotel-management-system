package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Billing;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BillingDAO {

    public boolean createBill(Billing b) {
        String sql = """
            INSERT INTO billing (reservation_id, nights, room_charge, extra_charges,
                                 discount, total_amount, payment_method, paid_at)
            VALUES (?,?,?,?,?,?,?,NOW())
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, b.getReservationId());
            ps.setInt(2, b.getNights());
            ps.setBigDecimal(3, b.getRoomCharge());
            ps.setBigDecimal(4, b.getExtraCharges());
            ps.setBigDecimal(5, b.getDiscount());
            ps.setBigDecimal(6, b.getTotalAmount());
            ps.setString(7, b.getPaymentMethod().name());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) b.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Optional<Billing> getBillByReservation(int reservationId) {
        String sql = """
            SELECT b.*, c.name AS client_name, r.room_number
            FROM billing b
            JOIN reservations res ON b.reservation_id = res.id
            JOIN clients c ON res.client_id = c.id
            JOIN rooms r   ON res.room_id   = r.id
            WHERE b.reservation_id = ?
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Billing> getAllBills() {
        List<Billing> list = new ArrayList<>();
        String sql = """
            SELECT b.*, c.name AS client_name, r.room_number
            FROM billing b
            JOIN reservations res ON b.reservation_id = res.id
            JOIN clients c ON res.client_id = c.id
            JOIN rooms r   ON res.room_id   = r.id
            ORDER BY b.paid_at DESC
            """;
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Monthly revenue summary. */
    public double getMonthlyRevenue(int year, int month) {
        String sql = "SELECT COALESCE(SUM(total_amount),0) FROM billing " +
                     "WHERE YEAR(paid_at)=? AND MONTH(paid_at)=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, year);
            ps.setInt(2, month);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    private Billing map(ResultSet rs) throws SQLException {
        Billing b = new Billing();
        b.setId(rs.getInt("id"));
        b.setReservationId(rs.getInt("reservation_id"));
        b.setNights(rs.getInt("nights"));
        b.setRoomCharge(rs.getBigDecimal("room_charge"));
        b.setExtraCharges(rs.getBigDecimal("extra_charges"));
        b.setDiscount(rs.getBigDecimal("discount"));
        b.setTotalAmount(rs.getBigDecimal("total_amount"));
        b.setPaymentMethod(rs.getString("payment_method"));
        Timestamp ts = rs.getTimestamp("paid_at");
        if (ts != null) b.setPaidAt(ts.toLocalDateTime());
        b.setClientName(rs.getString("client_name"));
        b.setRoomNumber(rs.getString("room_number"));
        return b;
    }
}
