package com.hotel.ui.panels;

import com.hotel.dao.RoomDAO;
import com.hotel.model.Room;
import com.hotel.model.RoomType;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RoomPanel extends JPanel {

    private final RoomDAO dao = new RoomDAO();

    private final String[] COLS = {"ID", "Room No.", "Floor", "Type", "Price/Night", "Status"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    // Filter controls
    private final JComboBox<String> statusFilter = new JComboBox<>(new String[]{"All", "AVAILABLE", "OCCUPIED", "MAINTENANCE"});
    private List<RoomType> roomTypes;

    public RoomPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        roomTypes = dao.getAllRoomTypes();
        buildUI();
        loadData();
    }

    private void buildUI() {
        // ── Top bar ───────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);
        top.add(UITheme.headingLabel("Room Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel("Filter by Status:"));
        actions.add(statusFilter);
        JButton addBtn      = UITheme.primaryButton("+ Add Room");
        JButton editBtn     = UITheme.accentButton("Edit");
        JButton deleteBtn   = UITheme.dangerButton("Delete");
        JButton maintBtn    = UITheme.accentButton("Set Maintenance");
        JButton refreshBtn  = UITheme.primaryButton("Refresh");
        actions.add(addBtn); actions.add(editBtn); actions.add(maintBtn);
        actions.add(deleteBtn); actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);

        // Colour-coded status column
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setForeground(UITheme.statusColor(val.toString()));
                    setFont(UITheme.FONT_BODY.deriveFont(Font.BOLD));
                }
                return this;
            }
        });

        add(new JScrollPane(table), BorderLayout.CENTER);

        // ── Legend ────────────────────────────────────────────────────
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 0));
        legend.setOpaque(false);
        addLegend(legend, "Available", UITheme.SUCCESS);
        addLegend(legend, "Occupied",  UITheme.DANGER);
        addLegend(legend, "Maintenance", UITheme.WARNING);
        add(legend, BorderLayout.SOUTH);

        // ── Listeners ─────────────────────────────────────────────────
        statusFilter.addActionListener(e -> loadData());
        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow(); if (row < 0) { msg("Select a room first."); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            dao.getRoomById(id).ifPresent(r -> showDialog(r));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
        maintBtn.addActionListener(e -> setMaintenance());
        refreshBtn.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String filter = (String) statusFilter.getSelectedItem();
        List<Room> rooms = dao.getAllRooms();
        for (Room r : rooms) {
            if (!filter.equals("All") && !r.getStatus().name().equals(filter)) continue;
            tableModel.addRow(new Object[]{
                r.getId(), r.getRoomNumber(), r.getFloor(),
                r.getRoomTypeName(), "₹" + r.getPricePerNight(), r.getStatus().name()
            });
        }
    }

    private void showDialog(Room existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                  isEdit ? "Edit Room" : "Add Room", true);
        dlg.setSize(400, 300);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(UITheme.CARD);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JTextField roomNumF = field(isEdit ? existing.getRoomNumber() : "");
        JTextField floorF   = field(isEdit ? String.valueOf(existing.getFloor()) : "1");

        JComboBox<RoomType> typeBox = new JComboBox<>(roomTypes.toArray(new RoomType[0]));
        if (isEdit) {
            for (int i = 0; i < typeBox.getItemCount(); i++) {
                if (typeBox.getItemAt(i).getId() == existing.getRoomTypeId()) {
                    typeBox.setSelectedIndex(i); break;
                }
            }
        }

        String[] statusOpts = {"AVAILABLE", "OCCUPIED", "MAINTENANCE"};
        JComboBox<String> statusBox = new JComboBox<>(statusOpts);
        if (isEdit) statusBox.setSelectedItem(existing.getStatus().name());

        Object[][] rows = {{"Room Number *", roomNumF}, {"Floor", floorF}, {"Room Type", typeBox}, {"Status", statusBox}};
        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.4; form.add(new JLabel(rows[i][0] + ":"), gc);
            gc.gridx = 1; gc.weightx = 0.6; form.add((Component) rows[i][1], gc);
        }

        JButton save = UITheme.primaryButton(isEdit ? "Update" : "Save");
        gc.gridx = 0; gc.gridy = rows.length; gc.gridwidth = 2;
        form.add(save, gc);

        save.addActionListener(e -> {
            if (roomNumF.getText().isBlank()) { msg("Room number is required."); return; }
            Room r = isEdit ? existing : new Room();
            r.setRoomNumber(roomNumF.getText().trim());
            try { r.setFloor(Integer.parseInt(floorF.getText().trim())); } catch (Exception ignored) {}
            r.setRoomTypeId(((RoomType) typeBox.getSelectedItem()).getId());
            r.setStatus((String) statusBox.getSelectedItem());
            boolean ok = isEdit ? dao.updateRoom(r) : dao.addRoom(r);
            if (ok) { dlg.dispose(); loadData(); }
            else msg("Failed to save. Room number may already exist.");
        });

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) { msg("Select a room first."); return; }
        String status = (String) tableModel.getValueAt(row, 5);
        if (status.equals("OCCUPIED")) { msg("Cannot delete an occupied room."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete room " + tableModel.getValueAt(row, 1) + "?",
                "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && dao.deleteRoom(id)) loadData();
    }

    private void setMaintenance() {
        int row = table.getSelectedRow(); if (row < 0) { msg("Select a room first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String current = (String) tableModel.getValueAt(row, 5);
        Room.Status next = current.equals("MAINTENANCE") ? Room.Status.AVAILABLE : Room.Status.MAINTENANCE;
        if (dao.updateRoomStatus(id, next)) loadData();
    }

    private void addLegend(JPanel p, String text, Color c) {
        JPanel dot = new JPanel(); dot.setBackground(c);
        dot.setPreferredSize(new Dimension(12, 12));
        p.add(dot);
        JLabel lbl = new JLabel(text); lbl.setFont(UITheme.FONT_SMALL);
        p.add(lbl);
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
    private JTextField field(String v) { JTextField f = new JTextField(v, 18); f.setFont(UITheme.FONT_BODY); return f; }
}
