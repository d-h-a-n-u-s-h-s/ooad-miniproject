package com.erp.view.panels.orders;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — top-level Orders module.
 */
public class OrdersHomePanel extends BasePanel {

    private JTabbedPane tabs;

    public OrdersHomePanel() { super("Order Processing"); }

    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new OrderDashboardPanel());
        tabs.addTab("Orders",    new OrdersSubPanel());
        tabs.addTab("Inventory", new OrdersInventoryPanel());
        tabs.addTab("Reports",   new ReportingAnalyticsTab());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
