package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Room;
import com.hotel.model.RoomType;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RoomDAO {

    // ── Room Types ──────────────────────────────────────────────────
    public List<RoomType> getAllRoomTypes() {
        List<RoomType> list = new ArrayList<>();
        String sql = "SELECT * FROM room_types ORDER BY name";
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                RoomType rt = new RoomType();
                rt.setId(rs.getInt("id"));
                rt.setName(rs.getString("name"));
                rt.setPricePerNight(rs.getBigDecimal("price_per_night"));
                rt.setDescription(rs.getString("description"));
                list.add(rt);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── All Rooms (joined with type) ────────────────────────────────
    public List<Room> getAllRooms() {
        List<Room> list = new ArrayList<>();
        String sql = """
            SELECT r.*, rt.name AS type_name, rt.price_per_night
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            ORDER BY r.room_number
            """;
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapJoined(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Room> getRoomById(int id) {
        String sql = """
            SELECT r.*, rt.name AS type_name, rt.price_per_night
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            WHERE r.id = ?
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapJoined(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    /**
     * Returns rooms available for the given date range.
     * A room is unavailable if it has a CONFIRMED or CHECKED_IN reservation
     * that overlaps [checkIn, checkOut).
     */
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut) {
        List<Room> list = new ArrayList<>();
        String sql = """
            SELECT r.*, rt.name AS type_name, rt.price_per_night
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            WHERE r.status = 'AVAILABLE'
              AND r.id NOT IN (
                  SELECT res.room_id FROM reservations res
                  WHERE res.status IN ('CONFIRMED','CHECKED_IN')
                    AND res.check_in_date  < ?
                    AND res.check_out_date > ?
              )
            ORDER BY r.room_number
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(checkOut));
            ps.setDate(2, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapJoined(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Rooms available filtered by type. */
    public List<Room> getAvailableRoomsByType(LocalDate checkIn, LocalDate checkOut, int roomTypeId) {
        List<Room> list = new ArrayList<>();
        String sql = """
            SELECT r.*, rt.name AS type_name, rt.price_per_night
            FROM rooms r
            JOIN room_types rt ON r.room_type_id = rt.id
            WHERE r.status = 'AVAILABLE'
              AND r.room_type_id = ?
              AND r.id NOT IN (
                  SELECT res.room_id FROM reservations res
                  WHERE res.status IN ('CONFIRMED','CHECKED_IN')
                    AND res.check_in_date  < ?
                    AND res.check_out_date > ?
              )
            ORDER BY r.room_number
            """;
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, roomTypeId);
            ps.setDate(2, Date.valueOf(checkOut));
            ps.setDate(3, Date.valueOf(checkIn));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapJoined(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean addRoom(Room room) {
        String sql = "INSERT INTO rooms (room_number, floor, room_type_id, status) VALUES (?,?,?,?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getFloor());
            ps.setInt(3, room.getRoomTypeId());
            ps.setString(4, room.getStatus().name());
            if (ps.executeUpdate() > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) room.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRoomStatus(int roomId, Room.Status status) {
        String sql = "UPDATE rooms SET status=? WHERE id=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setInt(2, roomId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean updateRoom(Room room) {
        String sql = "UPDATE rooms SET room_number=?, floor=?, room_type_id=?, status=? WHERE id=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, room.getRoomNumber());
            ps.setInt(2, room.getFloor());
            ps.setInt(3, room.getRoomTypeId());
            ps.setString(4, room.getStatus().name());
            ps.setInt(5, room.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteRoom(int id) {
        String sql = "DELETE FROM rooms WHERE id=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Room mapJoined(ResultSet rs) throws SQLException {
        Room r = new Room();
        r.setId(rs.getInt("id"));
        r.setRoomNumber(rs.getString("room_number"));
        r.setFloor(rs.getInt("floor"));
        r.setRoomTypeId(rs.getInt("room_type_id"));
        r.setRoomTypeName(rs.getString("type_name"));
        r.setPricePerNight(rs.getDouble("price_per_night"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
