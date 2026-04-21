package com.erp.view.components;

import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * Sidebar navigation component for the main application shell.
 * Pure UI — lists every ERP module, delegates selection events via ActionListener.
 */
public class Sidebar extends JPanel {

    private final List<MenuItem> menuItems = new ArrayList<>();
    private MenuItem selectedItem;
    private ActionListener menuActionListener;

    public Sidebar() {
        setupPanel();
        createMenuItems();
        layoutMenuItems();
    }

    private void setupPanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.BG_DARK);
        setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 0));
    }

    private void createMenuItems() {
        addMenuItem(Constants.MODULE_DASHBOARD, "dashboard");
        addMenuItem(Constants.MODULE_ORDER, "order");
        addMenuItem(Constants.MODULE_CRM, "crm");
        addMenuItem(Constants.MODULE_SALES, "sales");
        addMenuItem(Constants.MODULE_INVENTORY, "inventory");
        addMenuItem(Constants.MODULE_MANUFACTURING, "manufacturing");
        addMenuItem(Constants.MODULE_FINANCE, "finance");
        addMenuItem(Constants.MODULE_ACCOUNTING, "accounting");
        addMenuItem(Constants.MODULE_HR, "hr");
        addMenuItem(Constants.MODULE_PROJECT, "project");
        addMenuItem(Constants.MODULE_REPORTING, "reporting");
        addMenuItem(Constants.MODULE_ANALYTICS, "analytics");
        addMenuItem(Constants.MODULE_BI, "bi");
        addMenuItem(Constants.MODULE_MARKETING, "marketing");
        addMenuItem(Constants.MODULE_AUTOMATION, "automation");
    }

    private void addMenuItem(String title, String actionCommand) {
        menuItems.add(new MenuItem(title, actionCommand));
    }

    private void layoutMenuItems() {
        add(createBrandPanel(), BorderLayout.NORTH);

        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setBackground(Constants.BG_DARK);
        menuPanel.setBorder(new EmptyBorder(Constants.PADDING_MEDIUM, 0, Constants.PADDING_MEDIUM, 0));

        for (MenuItem item : menuItems) {
            menuPanel.add(item);
            menuPanel.add(Box.createVerticalStrut(2));
        }
        menuPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(menuPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(Constants.BG_DARK);
        scrollPane.getViewport().setBackground(Constants.BG_DARK);

        add(scrollPane, BorderLayout.CENTER);

        if (!menuItems.isEmpty()) selectItem(menuItems.get(0));
    }

    private JPanel createBrandPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Constants.SECONDARY_COLOR);
        panel.setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 80));
        panel.setBorder(new EmptyBorder(Constants.PADDING_LARGE, Constants.PADDING_MEDIUM,
                                        Constants.PADDING_LARGE, Constants.PADDING_MEDIUM));

        JLabel brandLabel = new JLabel("TATA MOTORS");
        brandLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 20));
        brandLabel.setForeground(Constants.TATA_GOLD);
        brandLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel versionLabel = new JLabel("ERP v" + Constants.APP_VERSION);
        versionLabel.setFont(Constants.FONT_SMALL);
        versionLabel.setForeground(new Color(200, 210, 230));
        versionLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(brandLabel, BorderLayout.CENTER);
        panel.add(versionLabel, BorderLayout.SOUTH);
        return panel;
    }

    public void setMenuActionListener(ActionListener listener) {
        this.menuActionListener = listener;
    }

    private void selectItem(MenuItem item) {
        if (selectedItem != null) selectedItem.setSelected(false);
        selectedItem = item;
        selectedItem.setSelected(true);
    }

    public void selectByCommand(String actionCommand) {
        for (MenuItem item : menuItems) {
            if (item.getActionCommand().equals(actionCommand)) {
                selectItem(item);
                break;
            }
        }
    }

    private class MenuItem extends JPanel {

        private final String title;
        private final String actionCommand;
        private JLabel titleLabel;
        private boolean isSelected;

        public MenuItem(String title, String actionCommand) {
            this.title = title;
            this.actionCommand = actionCommand;
            setupItem();
        }

        private void setupItem() {
            setLayout(new BorderLayout());
            setBackground(Constants.BG_DARK);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 45));
            setPreferredSize(new Dimension(Constants.SIDEBAR_WIDTH, 45));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setBorder(new EmptyBorder(0, Constants.PADDING_LARGE, 0, Constants.PADDING_MEDIUM));

            titleLabel = new JLabel(title);
            titleLabel.setFont(Constants.FONT_REGULAR);
            titleLabel.setForeground(Constants.TEXT_LIGHT);
            titleLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));

            add(titleLabel, BorderLayout.WEST);

            java.awt.event.MouseAdapter hoverAndClick = new java.awt.event.MouseAdapter() {
                @Override public void mousePressed(java.awt.event.MouseEvent e) { dispatchSelection(); }
                @Override public void mouseClicked(java.awt.event.MouseEvent e) { dispatchSelection(); }
                @Override public void mouseEntered(java.awt.event.MouseEvent e) {
                    if (!isSelected) setBackground(new Color(60, 80, 100));
                }
                @Override public void mouseExited(java.awt.event.MouseEvent e) {
                    if (!isSelected) setBackground(Constants.BG_DARK);
                }
            };
            addMouseListener(hoverAndClick);
            titleLabel.addMouseListener(hoverAndClick);
        }

        private void dispatchSelection() {
            selectItem(MenuItem.this);
            if (menuActionListener != null) {
                java.awt.event.ActionEvent event = new java.awt.event.ActionEvent(
                        MenuItem.this,
                        java.awt.event.ActionEvent.ACTION_PERFORMED,
                        actionCommand);
                menuActionListener.actionPerformed(event);
            }
        }

        public void setSelected(boolean selected) {
            this.isSelected = selected;
            setBackground(selected ? Constants.PRIMARY_COLOR : Constants.BG_DARK);
        }

        public String getActionCommand() { return actionCommand; }
    }
}
