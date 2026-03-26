package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Reservation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationDAO {

    private static final String JOIN_SQL = """
        SELECT res.*,
               c.name   AS client_name,
               r.room_number,
               rt.name  AS room_type_name,
               rt.price_per_night
        FROM reservations res
        JOIN clients    c  ON res.client_id = c.id
        JOIN rooms      r  ON res.room_id   = r.id
        JOIN room_types rt ON r.room_type_id = rt.id
        """;

    public boolean addReservation(Reservation res) {
        String sql = """
            INSERT INTO reservations
              (client_id, room_id, employee_id, check_in_date, check_out_date, status, special_requests)
            VALUES (?,?,?,?,?,?,?)
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, res.getClientId());
            ps.setInt(2, res.getRoomId());
            ps.setInt(3, res.getEmployeeId());
            ps.setDate(4, Date.valueOf(res.getCheckInDate()));
            ps.setDate(5, Date.valueOf(res.getCheckOutDate()));
            ps.setString(6, res.getStatus().name());
            ps.setString(7, res.getSpecialRequests());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) res.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Reservation> getAllReservations() {
        List<Reservation> list = new ArrayList<>();
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(JOIN_SQL + " ORDER BY res.check_in_date DESC")) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<Reservation> getActiveReservations() {
        List<Reservation> list = new ArrayList<>();
        String sql = JOIN_SQL + " WHERE res.status IN ('CONFIRMED','CHECKED_IN') ORDER BY res.check_in_date";
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Reservation> getReservationById(int id) {
        String sql = JOIN_SQL + " WHERE res.id = ?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public List<Reservation> getReservationsByClient(int clientId) {
        List<Reservation> list = new ArrayList<>();
        String sql = JOIN_SQL + " WHERE res.client_id = ? ORDER BY res.check_in_date DESC";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, clientId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean updateStatus(int reservationId, Reservation.Status status) {
        String sql = "UPDATE reservations SET status=? WHERE id=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, reservationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean cancelReservation(int id) {
        return updateStatus(id, Reservation.Status.CANCELLED);
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation res = new Reservation();
        res.setId(rs.getInt("id"));
        res.setClientId(rs.getInt("client_id"));
        res.setRoomId(rs.getInt("room_id"));
        res.setEmployeeId(rs.getInt("employee_id"));
        res.setCheckInDate(rs.getDate("check_in_date").toLocalDate());
        res.setCheckOutDate(rs.getDate("check_out_date").toLocalDate());
        res.setStatus(rs.getString("status"));
        res.setSpecialRequests(rs.getString("special_requests"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) res.setCreatedAt(ts.toLocalDateTime());
        // joined
        res.setClientName(rs.getString("client_name"));
        res.setRoomNumber(rs.getString("room_number"));
        res.setRoomTypeName(rs.getString("room_type_name"));
        res.setPricePerNight(rs.getDouble("price_per_night"));
        return res;
    }
}
