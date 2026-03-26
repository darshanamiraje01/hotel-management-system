package com.hotel.ui;

import com.hotel.dao.EmployeeDAO;
import com.hotel.model.Employee;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Optional;

public class LoginFrame extends JFrame {

    private final JTextField     usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel         errorLabel    = new JLabel(" ");
    private final EmployeeDAO    employeeDAO   = new EmployeeDAO();

    public LoginFrame() {
        super("Hotel Management System — Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(440, 380);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUI();
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UITheme.BG);

        // ── Header strip ─────────────────────────────────────────────
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY);
        header.setBorder(new EmptyBorder(24, 32, 24, 32));
        JLabel title = new JLabel("🏨 Hotel Management System");
        title.setFont(UITheme.FONT_TITLE);
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.CENTER);
        JLabel sub = new JLabel("Staff Login");
        sub.setFont(UITheme.FONT_BODY);
        sub.setForeground(new Color(0xBB, 0xDD, 0xEE));
        header.add(sub, BorderLayout.SOUTH);
        root.add(header, BorderLayout.NORTH);

        // ── Form card ────────────────────────────────────────────────
        JPanel card = UITheme.cardPanel(new GridBagLayout());
        card.setBorder(new EmptyBorder(32, 40, 24, 40));
        card.setBackground(UITheme.CARD);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 1;
        card.add(new JLabel("Username:"), gc);
        gc.gridx = 1; card.add(usernameField, gc);

        gc.gridx = 0; gc.gridy = 1;
        card.add(new JLabel("Password:"), gc);
        gc.gridx = 1; card.add(passwordField, gc);

        errorLabel.setForeground(UITheme.DANGER);
        errorLabel.setFont(UITheme.FONT_SMALL);
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        card.add(errorLabel, gc);

        JButton loginBtn = UITheme.primaryButton("Sign In");
        gc.gridy = 3;
        card.add(loginBtn, gc);

        loginBtn.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        root.add(card, BorderLayout.CENTER);

        JLabel footer = new JLabel("© 2025 Hotel Management System", SwingConstants.CENTER);
        footer.setFont(UITheme.FONT_SMALL);
        footer.setForeground(UITheme.TEXT_MUTED);
        footer.setBorder(new EmptyBorder(8, 0, 12, 0));
        root.add(footer, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Please enter username and password.");
            return;
        }

        Optional<Employee> result = employeeDAO.authenticate(username, password);
        if (result.isPresent()) {
            dispose();
            SwingUtilities.invokeLater(() -> new MainFrame(result.get()).setVisible(true));
        } else {
            errorLabel.setText("Invalid username or password.");
            passwordField.setText("");
        }
    }
}
