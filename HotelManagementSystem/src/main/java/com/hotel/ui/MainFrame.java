package com.hotel.ui;

import com.hotel.model.Employee;
import com.hotel.ui.panels.*;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Main application window. Sidebar on the left; content card on the right.
 */
public class MainFrame extends JFrame {

    private final Employee loggedInUser;
    private final JPanel   contentArea = new JPanel(new CardLayout());

    // Sidebar buttons
    private final String[] NAV_ITEMS = {
        "Dashboard", "Clients", "Employees", "Rooms", "Reservations", "Checkout", "Billing"
    };

    public MainFrame(Employee user) {
        super("Hotel Management System");
        this.loggedInUser = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1200, 720);
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        buildUI();
    }

    private void buildUI() {
        setLayout(new BorderLayout());

        // ── Sidebar ───────────────────────────────────────────────────
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.PRIMARY_DARK);
        sidebar.setPreferredSize(new Dimension(200, 0));

        // Brand
        JLabel brand = new JLabel("🏨 HMS");
        brand.setFont(new Font("Segoe UI", Font.BOLD, 20));
        brand.setForeground(Color.WHITE);
        brand.setBorder(new EmptyBorder(24, 20, 8, 20));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(brand);

        JLabel sub = new JLabel("Hotel Manager");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(new Color(0x99, 0xBB, 0xCC));
        sub.setBorder(new EmptyBorder(0, 20, 20, 20));
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(sub);

        sidebar.add(new JSeparator());

        ButtonGroup bg = new ButtonGroup();
        JToggleButton[] navBtns = new JToggleButton[NAV_ITEMS.length];
        for (int i = 0; i < NAV_ITEMS.length; i++) {
            navBtns[i] = sidebarButton(NAV_ITEMS[i]);
            bg.add(navBtns[i]);
            sidebar.add(navBtns[i]);
            final String name = NAV_ITEMS[i];
            navBtns[i].addActionListener(e -> switchPanel(name));
        }

        sidebar.add(Box.createVerticalGlue());

        // Logged-in user badge
        JLabel userLbl = new JLabel("👤 " + loggedInUser.getName());
        userLbl.setFont(UITheme.FONT_SMALL);
        userLbl.setForeground(new Color(0xBB, 0xDD, 0xEE));
        userLbl.setBorder(new EmptyBorder(12, 16, 4, 16));
        userLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(userLbl);

        JLabel roleLbl = new JLabel(loggedInUser.getRole());
        roleLbl.setFont(UITheme.FONT_SMALL);
        roleLbl.setForeground(new Color(0x77, 0x99, 0xAA));
        roleLbl.setBorder(new EmptyBorder(0, 16, 8, 16));
        roleLbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(roleLbl);

        JButton logoutBtn = new JButton("Logout");
        logoutBtn.setFont(UITheme.FONT_SMALL);
        logoutBtn.setForeground(UITheme.DANGER);
        logoutBtn.setBackground(UITheme.PRIMARY_DARK);
        logoutBtn.setBorderPainted(false);
        logoutBtn.setFocusPainted(false);
        logoutBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        logoutBtn.addActionListener(e -> logout());
        sidebar.add(logoutBtn);
        sidebar.add(Box.createVerticalStrut(12));

        add(sidebar, BorderLayout.WEST);

        // ── Content panels ────────────────────────────────────────────
        registerPanels();
        contentArea.setBackground(UITheme.BG);
        add(contentArea, BorderLayout.CENTER);

        // Select Dashboard by default
        navBtns[0].setSelected(true);
        switchPanel("Dashboard");
    }

    private void registerPanels() {
        contentArea.add(new DashboardPanel(loggedInUser), "Dashboard");
        contentArea.add(new ClientPanel(),                "Clients");
        contentArea.add(new EmployeePanel(),              "Employees");
        contentArea.add(new RoomPanel(),                  "Rooms");
        contentArea.add(new ReservationPanel(loggedInUser), "Reservations");
        contentArea.add(new CheckoutPanel(loggedInUser),  "Checkout");
        contentArea.add(new BillingPanel(),               "Billing");
    }

    private void switchPanel(String name) {
        CardLayout cl = (CardLayout) contentArea.getLayout();
        cl.show(contentArea, name);
    }

    private JToggleButton sidebarButton(String text) {
        JToggleButton btn = new JToggleButton(text);
        btn.setFont(UITheme.FONT_BODY);
        btn.setForeground(new Color(0xCC, 0xDD, 0xEE));
        btn.setBackground(UITheme.PRIMARY_DARK);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setMaximumSize(new Dimension(200, 42));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        // Highlight when selected
        btn.addChangeListener(e -> {
            if (btn.isSelected()) {
                btn.setBackground(UITheme.PRIMARY);
                btn.setForeground(Color.WHITE);
            } else {
                btn.setBackground(UITheme.PRIMARY_DARK);
                btn.setForeground(new Color(0xCC, 0xDD, 0xEE));
            }
        });
        return btn;
    }

    private void logout() {
        int ok = JOptionPane.showConfirmDialog(this, "Logout and return to login screen?",
                "Logout", JOptionPane.YES_NO_OPTION);
        if (ok == JOptionPane.YES_OPTION) {
            dispose();
            SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
        }
    }
}
