package com.hotel.ui.panels;

import com.hotel.dao.ClientDAO;
import com.hotel.model.Client;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ClientPanel extends JPanel {

    private final ClientDAO dao = new ClientDAO();

    private final String[] COLS = {"ID", "Name", "Email", "Phone", "Address", "ID Proof"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable  table       = new JTable(tableModel);
    private final JTextField searchField = new JTextField(20);

    public ClientPanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData("");
    }

    private void buildUI() {
        // ── Top bar ───────────────────────────────────────────────────
        JPanel top = new JPanel(new BorderLayout(12, 0));
        top.setOpaque(false);

        JLabel heading = UITheme.headingLabel("Client Management");
        top.add(heading, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        actions.add(new JLabel("Search:"));
        actions.add(searchField);

        JButton searchBtn = UITheme.primaryButton("Search");
        JButton addBtn    = UITheme.primaryButton("+ Add Client");
        JButton editBtn   = UITheme.accentButton("Edit");
        JButton deleteBtn = UITheme.dangerButton("Delete");
        JButton refreshBtn = UITheme.primaryButton("Refresh");

        actions.add(searchBtn);
        actions.add(addBtn);
        actions.add(editBtn);
        actions.add(deleteBtn);
        actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // ── Table ─────────────────────────────────────────────────────
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xEE)));
        add(scroll, BorderLayout.CENTER);

        // ── Listeners ─────────────────────────────────────────────────
        searchBtn.addActionListener(e -> loadData(searchField.getText().trim()));
        searchField.addActionListener(e -> loadData(searchField.getText().trim()));
        refreshBtn.addActionListener(e -> { searchField.setText(""); loadData(""); });
        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { JOptionPane.showMessageDialog(this, "Select a client first."); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            dao.getClientById(id).ifPresent(c -> showDialog(c));
        });
        deleteBtn.addActionListener(e -> deleteSelected());
    }

    private void loadData(String keyword) {
        tableModel.setRowCount(0);
        List<Client> list = keyword.isEmpty() ? dao.getAllClients() : dao.searchClients(keyword);
        for (Client c : list) {
            tableModel.addRow(new Object[]{
                c.getId(), c.getName(), c.getEmail(), c.getPhone(), c.getAddress(), c.getIdProof()
            });
        }
    }

    private void showDialog(Client existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                  isEdit ? "Edit Client" : "Add Client", true);
        dlg.setSize(440, 380);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(UITheme.CARD);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JTextField nameF    = field(isEdit ? existing.getName()    : "");
        JTextField emailF   = field(isEdit ? existing.getEmail()   : "");
        JTextField phoneF   = field(isEdit ? existing.getPhone()   : "");
        JTextField addressF = field(isEdit ? existing.getAddress() : "");
        JTextField proofF   = field(isEdit ? existing.getIdProof() : "");

        String[][] rows = {
            {"Full Name *", null}, {"Email", null}, {"Phone *", null},
            {"Address", null}, {"ID Proof (Passport/Aadhar)", null}
        };
        JTextField[] fields = {nameF, emailF, phoneF, addressF, proofF};

        for (int i = 0; i < fields.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.3;
            form.add(new JLabel(rows[i][0] + ":"), gc);
            gc.gridx = 1; gc.weightx = 0.7;
            form.add(fields[i], gc);
        }

        JButton save = UITheme.primaryButton(isEdit ? "Update" : "Save");
        gc.gridx = 0; gc.gridy = fields.length; gc.gridwidth = 2;
        form.add(save, gc);

        save.addActionListener(e -> {
            if (nameF.getText().isBlank() || phoneF.getText().isBlank()) {
                JOptionPane.showMessageDialog(dlg, "Name and Phone are required.");
                return;
            }
            Client c = isEdit ? existing : new Client();
            c.setName(nameF.getText().trim());
            c.setEmail(emailF.getText().trim());
            c.setPhone(phoneF.getText().trim());
            c.setAddress(addressF.getText().trim());
            c.setIdProof(proofF.getText().trim());
            boolean ok = isEdit ? dao.updateClient(c) : dao.addClient(c);
            if (ok) {
                dlg.dispose();
                loadData("");
                JOptionPane.showMessageDialog(this, "Client " + (isEdit ? "updated" : "added") + " successfully.");
            } else {
                JOptionPane.showMessageDialog(dlg, "Operation failed. Email may already exist.");
            }
        });

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a client first."); return; }
        int id   = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete client '" + name + "'? This cannot be undone.", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.deleteClient(id)) loadData("");
            else JOptionPane.showMessageDialog(this, "Cannot delete: client has existing reservations.");
        }
    }

    private JTextField field(String val) {
        JTextField f = new JTextField(val, 20);
        f.setFont(UITheme.FONT_BODY);
        return f;
    }
}
