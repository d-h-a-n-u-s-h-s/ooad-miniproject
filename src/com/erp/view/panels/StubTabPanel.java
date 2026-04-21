package com.erp.view.panels;

import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Lightweight UI-only stub used inside every module's {@code JTabbedPane}.
 *
 * Renders a section header, a toolbar of stub buttons, and an empty table
 * with the supplied column headers. No data, no controllers — all buttons
 * funnel into {@link UIHelper#showNotAvailable(Component, String)}.
 */
public class StubTabPanel extends JPanel {

    public StubTabPanel(String title, String subtitle, String[] columns, String... actions) {
        setLayout(new BorderLayout(0, 10));
        setBackground(Constants.BG_LIGHT);
        setBorder(new EmptyBorder(Constants.PADDING_LARGE, Constants.PADDING_LARGE,
                Constants.PADDING_LARGE, Constants.PADDING_LARGE));

        add(buildHeader(title, subtitle), BorderLayout.NORTH);
        add(buildBody(columns, actions), BorderLayout.CENTER);
    }

    private JPanel buildHeader(String title, String subtitle) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        titles.add(titleLabel);
        if (subtitle != null && !subtitle.isEmpty()) {
            JLabel subtitleLabel = new JLabel(subtitle);
            subtitleLabel.setFont(Constants.FONT_SMALL);
            subtitleLabel.setForeground(Constants.TEXT_SECONDARY);
            subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
            titles.add(Box.createVerticalStrut(2));
            titles.add(subtitleLabel);
        }

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody(String[] columns, String[] actions) {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Constants.BG_WHITE);
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        if (actions != null && actions.length > 0) {
            JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            toolbar.setOpaque(false);
            for (String action : actions) toolbar.add(stubButton(action));
            body.add(toolbar, BorderLayout.NORTH);
        }

        body.add(new JScrollPane(buildEmptyTable(columns)), BorderLayout.CENTER);
        return body;
    }

    private JTable buildEmptyTable(String[] columns) {
        DefaultTableModel model = new DefaultTableModel(new Object[0][0],
                columns == null ? new String[]{"Column"} : columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        table.setFont(Constants.FONT_REGULAR);
        table.setRowHeight(26);
        table.setGridColor(new Color(230, 232, 236));
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(Constants.FONT_HEADING);
        header.setBackground(Constants.PRIMARY_COLOR);
        header.setForeground(Constants.TEXT_LIGHT);
        header.setReorderingAllowed(false);
        return table;
    }

    private JButton stubButton(String label) {
        JButton button = new JButton(label);
        button.setFont(Constants.FONT_BUTTON);
        button.setForeground(Constants.TEXT_LIGHT);
        button.setBackground(Constants.PRIMARY_COLOR);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> UIHelper.showNotAvailable(this, label));
        return button;
    }
}
