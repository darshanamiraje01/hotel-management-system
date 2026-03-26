package com.hotel.util;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Central theme constants and helper factory methods for the hotel UI.
 */
public final class UITheme {

    // ── Palette ─────────────────────────────────────────────────────
    public static final Color PRIMARY      = new Color(0x1A, 0x5F, 0x7A);  // deep teal
    public static final Color PRIMARY_DARK = new Color(0x12, 0x44, 0x59);
    public static final Color ACCENT       = new Color(0xF5, 0xA6, 0x23);  // amber
    public static final Color BG           = new Color(0xF4, 0xF6, 0xF8);
    public static final Color CARD         = Color.WHITE;
    public static final Color TEXT_PRIMARY = new Color(0x22, 0x22, 0x22);
    public static final Color TEXT_MUTED   = new Color(0x77, 0x77, 0x88);
    public static final Color SUCCESS      = new Color(0x27, 0xAE, 0x60);
    public static final Color DANGER       = new Color(0xE7, 0x4C, 0x3C);
    public static final Color WARNING      = new Color(0xF3, 0x9C, 0x12);
    public static final Color TABLE_ALT    = new Color(0xF0, 0xF5, 0xFB);

    // ── Fonts ────────────────────────────────────────────────────────
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON  = new Font("Segoe UI", Font.BOLD, 12);

    private UITheme() {}

    // ── Global L&F ──────────────────────────────────────────────────
    public static void apply() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",      BG);
        UIManager.put("OptionPane.background",  CARD);
        UIManager.put("TextField.font",         FONT_BODY);
        UIManager.put("Label.font",             FONT_BODY);
        UIManager.put("ComboBox.font",          FONT_BODY);
        UIManager.put("Table.font",             FONT_BODY);
        UIManager.put("TableHeader.font",       FONT_HEADING);
    }

    // ── Factory: primary button ─────────────────────────────────────
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BUTTON);
        btn.setBackground(PRIMARY);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    // ── Factory: danger button ──────────────────────────────────────
    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(DANGER);
        return btn;
    }

    // ── Factory: accent button ──────────────────────────────────────
    public static JButton accentButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(ACCENT);
        btn.setForeground(TEXT_PRIMARY);
        return btn;
    }

    // ── Factory: styled JTable ──────────────────────────────────────
    public static void styleTable(JTable table) {
        table.setFont(FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(new Color(0xE0, 0xE0, 0xE0));
        table.setSelectionBackground(PRIMARY);
        table.setSelectionForeground(Color.WHITE);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_HEADING);
        header.setBackground(PRIMARY);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);

        // Alternating row colours
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value, boolean sel,
                                                           boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, value, sel, focus, row, col);
                if (!sel) setBackground(row % 2 == 0 ? CARD : TABLE_ALT);
                setBorder(new EmptyBorder(2, 8, 2, 8));
                return this;
            }
        });
    }

    // ── Factory: card panel ──────────────────────────────────────────
    public static JPanel cardPanel(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDD, 0xDD, 0xEE), 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    // ── Factory: section label ───────────────────────────────────────
    public static JLabel headingLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_HEADING);
        lbl.setForeground(PRIMARY);
        return lbl;
    }

    // ── Factory: status badge colours ────────────────────────────────
    public static Color statusColor(String status) {
        return switch (status.toUpperCase()) {
            case "AVAILABLE"  -> SUCCESS;
            case "OCCUPIED"   -> DANGER;
            case "MAINTENANCE"-> WARNING;
            case "CONFIRMED"  -> PRIMARY;
            case "CHECKED_IN" -> ACCENT;
            case "CHECKED_OUT"-> TEXT_MUTED;
            case "CANCELLED"  -> DANGER;
            default           -> TEXT_PRIMARY;
        };
    }
}
