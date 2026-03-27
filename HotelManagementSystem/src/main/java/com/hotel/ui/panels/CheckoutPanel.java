package com.hotel.ui.panels;

import com.hotel.model.Billing;
import com.hotel.model.Billing.PaymentMethod;
import com.hotel.model.Employee;
import com.hotel.model.Reservation;
import com.hotel.service.ReservationService;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class CheckoutPanel extends JPanel {

    private final ReservationService service = new ReservationService();
    private final Employee loggedInUser;

    private final String[] COLS = {"ID","Client","Room","Type","Check-In","Check-Out","Nights","Status"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public CheckoutPanel(Employee user) {
        this.loggedInUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        loadActive();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        JLabel heading = UITheme.headingLabel("Checkout & Billing");
        JLabel sub = new JLabel("  — Select a reservation below to process checkout");
        sub.setFont(UITheme.FONT_SMALL);
        sub.setForeground(UITheme.TEXT_MUTED);
        JPanel hdr = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        hdr.setOpaque(false);
        hdr.add(heading); hdr.add(sub);
        top.add(hdr, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton checkoutBtn = UITheme.accentButton("Process Checkout");
        JButton refreshBtn  = UITheme.primaryButton("Refresh");
        actions.add(checkoutBtn); actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);

        checkoutBtn.addActionListener(e -> processCheckout());
        refreshBtn.addActionListener(e -> loadActive());
    }

    private void loadActive() {
        tableModel.setRowCount(0);
        List<Reservation> list = service.getActiveReservations();
        for (Reservation r : list) {
            tableModel.addRow(new Object[]{
                r.getId(), r.getClientName(), r.getRoomNumber(),
                r.getRoomTypeName(), r.getCheckInDate(), r.getCheckOutDate(),
                r.getNights(), r.getStatus()
            });
        }
    }

    private void processCheckout() {
        int row = table.getSelectedRow();
        if (row < 0) { msg("Select a reservation to checkout."); return; }

        int reservationId = (int) tableModel.getValueAt(row, 0);
        String clientName = (String) tableModel.getValueAt(row, 1);
        String roomNum    = (String) tableModel.getValueAt(row, 2);
        long nights       = (long)   tableModel.getValueAt(row, 6);

        // ── Checkout dialog ───────────────────────────────────────────
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                  "Process Checkout — " + clientName, true);
        dlg.setSize(440, 320);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 28, 20, 28));
        form.setBackground(UITheme.CARD);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 4, 8, 4);

        JTextField extraF    = field("0.00");
        JTextField discountF = field("0.00");
        JComboBox<PaymentMethod> payBox = new JComboBox<>(PaymentMethod.values());

        // Summary labels
        JLabel summaryLbl = new JLabel("<html><b>Client:</b> " + clientName +
            " &nbsp;|&nbsp; <b>Room:</b> " + roomNum +
            " &nbsp;|&nbsp; <b>Nights:</b> " + nights + "</html>");
        summaryLbl.setFont(UITheme.FONT_SMALL);

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        form.add(summaryLbl, gc);

        Object[][] rows = {
            {"Extra Charges (₹)", extraF},
            {"Discount (₹)", discountF},
            {"Payment Method", payBox}
        };
        gc.gridwidth = 1;
        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i + 1; gc.weightx = 0.4;
            form.add(new JLabel(rows[i][0] + ":"), gc);
            gc.gridx = 1; gc.weightx = 0.6;
            form.add((Component) rows[i][1], gc);
        }

        JButton confirmBtn = UITheme.accentButton("Confirm & Generate Invoice");
        gc.gridx = 0; gc.gridy = rows.length + 1; gc.gridwidth = 2;
        form.add(confirmBtn, gc);

        confirmBtn.addActionListener(e -> {
            try {
                BigDecimal extra    = new BigDecimal(extraF.getText().trim());
                BigDecimal discount = new BigDecimal(discountF.getText().trim());
                PaymentMethod pm    = (PaymentMethod) payBox.getSelectedItem();

                Optional<Billing> bill = service.checkOut(reservationId, extra, discount, pm);
                if (bill.isPresent()) {
                    dlg.dispose();
                    loadActive();
                    showInvoice(bill.get(), clientName, roomNum, nights);
                } else {
                    msg("Checkout failed. Reservation may already be closed.");
                }
            } catch (NumberFormatException ex) {
                msg("Enter valid amounts (numbers only).");
            }
        });

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void showInvoice(Billing bill, String clientName, String roomNum, long nights) {
        String invoice = String.format(
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
            "         🏨 HOTEL MANAGEMENT SYSTEM%n" +
            "               INVOICE / RECEIPT%n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
            "  Client     : %s%n" +
            "  Room       : %s%n" +
            "  Nights     : %d%n" +
            "───────────────────────────────────────────%n" +
            "  Room Charge: ₹%,.2f%n" +
            "  Extra      : ₹%,.2f%n" +
            "  Discount   : -₹%,.2f%n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
            "  TOTAL PAID : ₹%,.2f%n" +
            "  Payment    : %s%n" +
            "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━%n" +
            "         Thank you for your stay!%n",
            clientName, roomNum, nights,
            bill.getRoomCharge(), bill.getExtraCharges(), bill.getDiscount(),
            bill.getTotalAmount(), bill.getPaymentMethod()
        );

        JTextArea area = new JTextArea(invoice);
        area.setFont(new Font("Monospaced", Font.PLAIN, 13));
        area.setEditable(false);
        area.setBackground(UITheme.CARD);

        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            "Invoice — " + clientName, JOptionPane.INFORMATION_MESSAGE);
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
    private JTextField field(String v) { JTextField f = new JTextField(v, 18); f.setFont(UITheme.FONT_BODY); return f; }
}
