package com.hotel.ui.panels;

import com.hotel.dao.BillingDAO;
import com.hotel.model.Billing;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class BillingPanel extends JPanel {

    private final BillingDAO dao = new BillingDAO();

    private final String[] COLS = {"ID","Reservation","Client","Room","Nights","Room Charge","Extra","Discount","Total","Payment","Date"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private JLabel totalLabel;

    public BillingPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.headingLabel("Billing & Revenue"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton refreshBtn = UITheme.primaryButton("Refresh");
        actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(40);
        table.getColumnModel().getColumn(1).setMaxWidth(90);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Revenue summary bar
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        summary.setBackground(UITheme.PRIMARY_DARK);
        summary.setBorder(new EmptyBorder(8, 16, 8, 16));
        totalLabel = new JLabel("Monthly Revenue: ₹0");
        totalLabel.setFont(UITheme.FONT_HEADING);
        totalLabel.setForeground(Color.WHITE);
        summary.add(totalLabel);
        add(summary, BorderLayout.SOUTH);

        refreshBtn.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Billing> bills = dao.getAllBills();
        double grandTotal = 0;
        for (Billing b : bills) {
            tableModel.addRow(new Object[]{
                b.getId(), b.getReservationId(), b.getClientName(), b.getRoomNumber(),
                b.getNights(),
                "₹" + b.getRoomCharge(),
                "₹" + b.getExtraCharges(),
                "₹" + b.getDiscount(),
                "₹" + b.getTotalAmount(),
                b.getPaymentMethod(),
                b.getPaidAt() != null ? b.getPaidAt().toLocalDate() : ""
            });
            grandTotal += b.getTotalAmount().doubleValue();
        }
        // Monthly revenue
        LocalDate now = LocalDate.now();
        double monthly = dao.getMonthlyRevenue(now.getYear(), now.getMonthValue());
        totalLabel.setText(String.format("This Month: ₹%,.2f  |  All Time: ₹%,.2f", monthly, grandTotal));
    }
}
