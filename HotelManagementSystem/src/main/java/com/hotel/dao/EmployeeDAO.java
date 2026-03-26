package com.hotel.dao;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Employee;

//import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmployeeDAO {

    public boolean addEmployee(Employee e) {
        String sql = "INSERT INTO employees (name,email,phone,role,salary,hire_date,username,password,is_active) " +
                     "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getRole());
            ps.setBigDecimal(5, e.getSalary());
            ps.setDate(6, e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
            ps.setString(7, e.getUsername());
            ps.setString(8, e.getPassword());
            ps.setBoolean(9, e.isActive());
            int rows = ps.executeUpdate();
            if (rows > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) e.setId(keys.getInt(1));
                }
                return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM employees ORDER BY name";
        try (Connection con = DatabaseConfig.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Optional<Employee> getEmployeeById(int id) {
        String sql = "SELECT * FROM employees WHERE id = ?";
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

    /** Used for login — matches username + password. */
    public Optional<Employee> authenticate(String username, String password) {
        String sql = "SELECT * FROM employees WHERE username=? AND password=? AND is_active=TRUE";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    public boolean updateEmployee(Employee e) {
        String sql = "UPDATE employees SET name=?,email=?,phone=?,role=?,salary=?,hire_date=?,is_active=? WHERE id=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getName());
            ps.setString(2, e.getEmail());
            ps.setString(3, e.getPhone());
            ps.setString(4, e.getRole());
            ps.setBigDecimal(5, e.getSalary());
            ps.setDate(6, e.getHireDate() != null ? Date.valueOf(e.getHireDate()) : null);
            ps.setBoolean(7, e.isActive());
            ps.setInt(8, e.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return false;
    }

    public boolean deleteEmployee(int id) {
        String sql = "UPDATE employees SET is_active=FALSE WHERE id=?";
        try (Connection con = DatabaseConfig.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Employee map(ResultSet rs) throws SQLException {
        Employee e = new Employee();
        e.setId(rs.getInt("id"));
        e.setName(rs.getString("name"));
        e.setEmail(rs.getString("email"));
        e.setPhone(rs.getString("phone"));
        e.setRole(rs.getString("role"));
        e.setSalary(rs.getBigDecimal("salary"));
        Date d = rs.getDate("hire_date");
        if (d != null) e.setHireDate(d.toLocalDate());
        e.setUsername(rs.getString("username"));
        e.setPassword(rs.getString("password"));
        e.setActive(rs.getBoolean("is_active"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) e.setCreatedAt(ts.toLocalDateTime());
        return e;
    }
}
