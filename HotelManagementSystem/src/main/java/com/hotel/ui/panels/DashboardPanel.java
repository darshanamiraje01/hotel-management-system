package com.hotel.ui.panels;

import com.hotel.config.DatabaseConfig;
import com.hotel.model.Employee;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DashboardPanel extends JPanel {

    private final Employee user;
    private JLabel totalRoomsVal, availableVal, occupiedVal, clientsVal, reservationsVal, revenueVal;

    public DashboardPanel(Employee user) {
        this.user = user;
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        refresh();
    }

    private void buildUI() {
        // ── Page header ──────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel welcome = new JLabel("Welcome back, " + user.getName() + " 👋");
        welcome.setFont(UITheme.FONT_TITLE);
        welcome.setForeground(UITheme.TEXT_PRIMARY);
        header.add(welcome, BorderLayout.WEST);

        JLabel date = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy")));
        date.setFont(UITheme.FONT_BODY);
        date.setForeground(UITheme.TEXT_MUTED);
        header.add(date, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // ── Stat cards grid ──────────────────────────────────────────
        JPanel grid = new JPanel(new GridLayout(2, 3, 16, 16));
        grid.setOpaque(false);

        totalRoomsVal   = addStatCard(grid, "Total Rooms",       "0", UITheme.PRIMARY);
        availableVal    = addStatCard(grid, "Available",         "0", UITheme.SUCCESS);
        occupiedVal     = addStatCard(grid, "Occupied",          "0", UITheme.DANGER);
        clientsVal      = addStatCard(grid, "Total Clients",     "0", UITheme.ACCENT);
        reservationsVal = addStatCard(grid, "Active Bookings",   "0", new Color(0x8E44AD));
        revenueVal      = addStatCard(grid, "Revenue This Month","₹0",new Color(0x16A085));

        add(grid, BorderLayout.CENTER);

        // ── Quick tips ───────────────────────────────────────────────
        JPanel tip = new JPanel(new BorderLayout());
        tip.setBackground(new Color(0xE8, 0xF4, 0xFD));
        tip.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0x9A, 0xC8, 0xE8), 1, true),
            new EmptyBorder(12, 16, 12, 16)
        ));
        tip.setBorder(new EmptyBorder(20, 0, 0, 0));
        tip.setOpaque(false);

        JLabel tipLabel = new JLabel("<html><b>Quick tips:</b> Use <i>Reservations</i> to book a room, " +
            "<i>Checkout</i> to generate a bill, and <i>Rooms</i> to manage room inventory.</html>");
        tipLabel.setFont(UITheme.FONT_SMALL);
        tipLabel.setForeground(UITheme.TEXT_MUTED);
        tip.add(tipLabel);
        add(tip, BorderLayout.SOUTH);
    }

    private JLabel addStatCard(JPanel parent, String title, String initial, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xE0, 0xE0, 0xEE), 1, true),
            new EmptyBorder(20, 20, 20, 20)
        ));

        // Accent stripe at top
        JPanel stripe = new JPanel();
        stripe.setBackground(accent);
        stripe.setPreferredSize(new Dimension(0, 4));
        card.add(stripe, BorderLayout.NORTH);

        JLabel valLabel = new JLabel(initial);
        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valLabel.setForeground(accent);
        card.add(valLabel, BorderLayout.CENTER);

        JLabel titleLabel = new JLabel(title.toUpperCase());
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        titleLabel.setForeground(UITheme.TEXT_MUTED);
        card.add(titleLabel, BorderLayout.SOUTH);

        parent.add(card);
        return valLabel;
    }

    public void refresh() {
        SwingWorker<int[], Void> worker = new SwingWorker<>() {
            double revenue;
            @Override protected int[] doInBackground() {
                int[] vals = new int[5];
                try (Connection con = DatabaseConfig.getConnection();
                     Statement st = con.createStatement()) {
                    ResultSet rs;
                    rs = st.executeQuery("SELECT COUNT(*) FROM rooms"); if (rs.next()) vals[0]=rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM rooms WHERE status='AVAILABLE'"); if (rs.next()) vals[1]=rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM rooms WHERE status='OCCUPIED'"); if (rs.next()) vals[2]=rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM clients"); if (rs.next()) vals[3]=rs.getInt(1);
                    rs = st.executeQuery("SELECT COUNT(*) FROM reservations WHERE status IN ('CONFIRMED','CHECKED_IN')"); if (rs.next()) vals[4]=rs.getInt(1);
                    rs = st.executeQuery("SELECT COALESCE(SUM(total_amount),0) FROM billing WHERE MONTH(paid_at)=MONTH(NOW()) AND YEAR(paid_at)=YEAR(NOW())");
                    if (rs.next()) revenue = rs.getDouble(1);
                } catch (Exception e) { e.printStackTrace(); }
                return vals;
            }
            @Override protected void done() {
                try {
                    int[] v = get();
                    totalRoomsVal.setText(String.valueOf(v[0]));
                    availableVal.setText(String.valueOf(v[1]));
                    occupiedVal.setText(String.valueOf(v[2]));
                    clientsVal.setText(String.valueOf(v[3]));
                    reservationsVal.setText(String.valueOf(v[4]));
                    revenueVal.setText(String.format("₹%,.0f", revenue));
                } catch (Exception ignored) {}
            }
        };
        worker.execute();
    }
}
