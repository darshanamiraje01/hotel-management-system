package com.hotel.ui.panels;

import com.hotel.dao.ClientDAO;
import com.hotel.dao.RoomDAO;
import com.hotel.model.*;
import com.hotel.service.ReservationService;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationPanel extends JPanel {

    private final ReservationService service = new ReservationService();
    private final ClientDAO clientDAO = new ClientDAO();
    private final RoomDAO roomDAO     = new RoomDAO();
    private final Employee loggedInUser;

    private final String[] COLS = {"ID","Client","Room","Type","Check-In","Check-Out","Nights","Status"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All","CONFIRMED","CHECKED_IN","CHECKED_OUT","CANCELLED"});

    public ReservationPanel(Employee user) {
        this.loggedInUser = user;
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.headingLabel("Reservation Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel("Filter:"));
        actions.add(statusFilter);
        JButton bookStdBtn  = UITheme.primaryButton("+ Book Standard Room");
        JButton bookDelBtn  = UITheme.accentButton("+ Book Deluxe Room");
        JButton cancelBtn   = UITheme.dangerButton("Cancel Booking");
        JButton refreshBtn  = UITheme.primaryButton("Refresh");
        actions.add(bookStdBtn); actions.add(bookDelBtn); actions.add(cancelBtn); actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);

        statusFilter.addActionListener(e -> loadData());
        bookStdBtn.addActionListener(e -> showBookingDialog(1)); // typeId 1 = Standard
        bookDelBtn.addActionListener(e -> showBookingDialog(2)); // typeId 2 = Deluxe
        cancelBtn.addActionListener(e -> cancelSelected());
        refreshBtn.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String filter = (String) statusFilter.getSelectedItem();
        List<Reservation> list = service.getAllReservations();
        for (Reservation r : list) {
            if (!filter.equals("All") && !r.getStatus().name().equals(filter)) continue;
            tableModel.addRow(new Object[]{
                r.getId(), r.getClientName(), r.getRoomNumber(),
                r.getRoomTypeName(), r.getCheckInDate(), r.getCheckOutDate(),
                r.getNights(), r.getStatus()
            });
        }
    }

    private void showBookingDialog(int roomTypeId) {
        String typeName = roomTypeId == 1 ? "Standard" : "Deluxe";
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                  "New " + typeName + " Reservation", true);
        dlg.setSize(520, 480);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 28, 20, 28));
        form.setBackground(UITheme.CARD);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(7, 4, 7, 4);

        // Client selector
        List<Client> clients = clientDAO.getAllClients();
        JComboBox<Client> clientBox = new JComboBox<>(clients.toArray(new Client[0]));

        // Date fields
        JTextField checkInF  = field(LocalDate.now().toString());
        JTextField checkOutF = field(LocalDate.now().plusDays(1).toString());

        // Available rooms panel (populated on search)
        JComboBox<Room> roomBox = new JComboBox<>();
        JButton searchRoomsBtn = UITheme.primaryButton("Search Available Rooms");

        JTextField requestsF = field("");

        Object[][] rows = {
            {"Client *", clientBox},
            {"Check-in Date * (yyyy-mm-dd)", checkInF},
            {"Check-out Date * (yyyy-mm-dd)", checkOutF},
            {"", searchRoomsBtn},
            {"Available Room *", roomBox},
            {"Special Requests", requestsF}
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.4;
            form.add(new JLabel(rows[i][0] + (rows[i][0].toString().isEmpty() ? "" : "")), gc);
            gc.gridx = 1; gc.weightx = 0.6;
            form.add((Component) rows[i][1], gc);
        }

        searchRoomsBtn.addActionListener(e -> {
            try {
                LocalDate ci = LocalDate.parse(checkInF.getText().trim());
                LocalDate co = LocalDate.parse(checkOutF.getText().trim());
                if (!co.isAfter(ci)) { msg("Check-out must be after check-in."); return; }
                List<Room> available = service.findAvailableRoomsByType(ci, co, roomTypeId);
                roomBox.removeAllItems();
                for (Room r : available) roomBox.addItem(r);
                if (available.isEmpty()) msg("No " + typeName + " rooms available for those dates.");
            } catch (Exception ex) {
                msg("Invalid date format. Use yyyy-mm-dd.");
            }
        });

        JButton confirmBtn = UITheme.primaryButton("Confirm Booking");
        gc.gridx = 0; gc.gridy = rows.length; gc.gridwidth = 2;
        form.add(confirmBtn, gc);

        confirmBtn.addActionListener(e -> {
            if (clientBox.getSelectedItem() == null || roomBox.getSelectedItem() == null) {
                msg("Please select a client and an available room."); return;
            }
            try {
                LocalDate ci = LocalDate.parse(checkInF.getText().trim());
                LocalDate co = LocalDate.parse(checkOutF.getText().trim());
                Client client = (Client) clientBox.getSelectedItem();
                Room room     = (Room) roomBox.getSelectedItem();

                Reservation res = new Reservation(
                    client.getId(), room.getId(), loggedInUser.getId(),
                    ci, co, requestsF.getText().trim()
                );
                if (service.bookRoom(res)) {
                    dlg.dispose();
                    loadData();
                    JOptionPane.showMessageDialog(this,
                        "Booking confirmed!\nRoom " + room.getRoomNumber() +
                        " (" + typeName + ") for " + client.getName() +
                        "\n" + ci + " → " + co + "  (" + res.getNights() + " nights)");
                } else {
                    msg("Room is no longer available. Please search again.");
                }
            } catch (Exception ex) {
                msg("Invalid data. Check dates (yyyy-mm-dd).");
            }
        });

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void cancelSelected() {
        int row = table.getSelectedRow(); if (row < 0) { msg("Select a reservation."); return; }
        String status = tableModel.getValueAt(row, 7).toString();
        if (status.equals("CHECKED_OUT") || status.equals("CANCELLED")) {
            msg("This reservation cannot be cancelled."); return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cancel reservation #" + id + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (service.cancelReservation(id)) { loadData(); msg("Reservation cancelled."); }
            else msg("Cancellation failed.");
        }
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
    private JTextField field(String v) { JTextField f = new JTextField(v, 20); f.setFont(UITheme.FONT_BODY); return f; }
}
