package com.hotel.ui.panels;

import com.hotel.dao.EmployeeDAO;
import com.hotel.model.Employee;
import com.hotel.util.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EmployeePanel extends JPanel {

    private final EmployeeDAO dao = new EmployeeDAO();

    private final String[] COLS = {"ID", "Name", "Role", "Email", "Phone", "Salary", "Hire Date", "Active"};
    private final DefaultTableModel tableModel = new DefaultTableModel(COLS, 0) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
    };
    private final JTable table = new JTable(tableModel);

    public EmployeePanel() {
        setLayout(new BorderLayout(0, 16));
        setBackground(UITheme.BG);
        setBorder(new EmptyBorder(24, 28, 24, 28));
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(UITheme.headingLabel("Employee Management"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton addBtn    = UITheme.primaryButton("+ Add Employee");
        JButton editBtn   = UITheme.accentButton("Edit");
        JButton deactBtn  = UITheme.dangerButton("Deactivate");
        JButton refreshBtn = UITheme.primaryButton("Refresh");
        actions.add(addBtn); actions.add(editBtn); actions.add(deactBtn); actions.add(refreshBtn);
        top.add(actions, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addBtn.addActionListener(e -> showDialog(null));
        editBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { msg("Select an employee first."); return; }
            int id = (int) tableModel.getValueAt(row, 0);
            dao.getEmployeeById(id).ifPresent(emp -> showDialog(emp));
        });
        deactBtn.addActionListener(e -> deactivateSelected());
        refreshBtn.addActionListener(e -> loadData());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        for (Employee e : dao.getAllEmployees()) {
            tableModel.addRow(new Object[]{
                e.getId(), e.getName(), e.getRole(), e.getEmail(),
                e.getPhone(), "₹" + e.getSalary(),
                e.getHireDate(), e.isActive() ? "Yes" : "No"
            });
        }
    }

    private void showDialog(Employee existing) {
        boolean isEdit = existing != null;
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
                                  isEdit ? "Edit Employee" : "Add Employee", true);
        dlg.setSize(480, 460);
        dlg.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(new EmptyBorder(20, 24, 20, 24));
        form.setBackground(UITheme.CARD);

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(6, 4, 6, 4);

        JTextField nameF     = field(isEdit ? existing.getName()   : "");
        JTextField emailF    = field(isEdit ? existing.getEmail()  : "");
        JTextField phoneF    = field(isEdit ? existing.getPhone()  : "");
        JTextField salaryF   = field(isEdit && existing.getSalary() != null ? existing.getSalary().toPlainString() : "");
        JTextField hireDateF = field(isEdit && existing.getHireDate() != null ? existing.getHireDate().toString() : LocalDate.now().toString());
        JTextField userF     = field(isEdit ? existing.getUsername() : "");
        JPasswordField passF = new JPasswordField(20);

        String[] roleOptions = {"Receptionist", "Manager", "Housekeeping", "Security", "Chef", "Accountant"};
        JComboBox<String> roleBox = new JComboBox<>(roleOptions);
        if (isEdit) roleBox.setSelectedItem(existing.getRole());

        Object[][] rows = {
            {"Full Name *", nameF}, {"Email", emailF}, {"Phone", phoneF},
            {"Role", roleBox}, {"Salary", salaryF}, {"Hire Date (yyyy-mm-dd)", hireDateF},
            {"Username *", userF}, {"Password " + (isEdit ? "(leave blank to keep)" : "*"), passF}
        };

        for (int i = 0; i < rows.length; i++) {
            gc.gridx = 0; gc.gridy = i; gc.weightx = 0.35;
            form.add(new JLabel(rows[i][0] + ":"), gc);
            gc.gridx = 1; gc.weightx = 0.65;
            form.add((Component) rows[i][1], gc);
        }

        JButton save = UITheme.primaryButton(isEdit ? "Update" : "Save");
        gc.gridx = 0; gc.gridy = rows.length; gc.gridwidth = 2;
        form.add(save, gc);

        save.addActionListener(e -> {
            if (nameF.getText().isBlank() || userF.getText().isBlank()) {
                msg("Name and Username are required."); return;
            }
            Employee emp = isEdit ? existing : new Employee();
            emp.setName(nameF.getText().trim());
            emp.setEmail(emailF.getText().trim());
            emp.setPhone(phoneF.getText().trim());
            emp.setRole((String) roleBox.getSelectedItem());
            try { emp.setSalary(new BigDecimal(salaryF.getText().trim())); } catch (Exception ignored) {}
            try { emp.setHireDate(LocalDate.parse(hireDateF.getText().trim())); } catch (Exception ignored) {}
            emp.setUsername(userF.getText().trim());
            String pass = new String(passF.getPassword());
            if (!pass.isBlank()) emp.setPassword(pass);
            emp.setActive(true);

            boolean ok = isEdit ? dao.updateEmployee(emp) : dao.addEmployee(emp);
            if (ok) { dlg.dispose(); loadData(); msg("Employee saved successfully."); }
            else msg("Operation failed. Username or email may already exist.");
        });

        dlg.setContentPane(form);
        dlg.setVisible(true);
    }

    private void deactivateSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { msg("Select an employee first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Deactivate '" + name + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION && dao.deleteEmployee(id)) loadData();
    }

    private void msg(String m) { JOptionPane.showMessageDialog(this, m); }
    private JTextField field(String val) { JTextField f = new JTextField(val, 20); f.setFont(UITheme.FONT_BODY); return f; }
}
