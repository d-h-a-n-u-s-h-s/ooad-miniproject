package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class SalesFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public SalesFacadePanel() { super("Sales Management"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Sales Dashboard", "Sales performance and KPIs.", new String[]{"Metric", "Target", "Actual", "Variance"}, "Refresh", "Export"));
        tabs.addTab("Quotations", new StubTabPanel("Sales Quotations", "Customer quotes and pricing.", new String[]{"Quote #", "Customer", "Amount", "Status", "Valid Until"}, "New Quote", "Convert", "Expire"));
        tabs.addTab("Orders", new StubTabPanel("Sales Orders", "Customer orders for vehicles and parts.", new String[]{"Order #", "Customer", "Model", "Qty", "Status"}, "New Order", "Approve", "Fulfill"));
        tabs.addTab("Dealers", new StubTabPanel("Dealer Management", "Dealership performance and inventory.", new String[]{"Dealer", "Region", "Inventory", "Sales YTD", "Status"}, "Register Dealer", "Allocate", "Review"));
        tabs.addTab("Incentives", new StubTabPanel("Sales Incentives", "Dealer incentives and promotions.", new String[]{"Scheme", "Type", "Budget", "Spend", "ROI"}, "Create Scheme", "Allocate", "Track"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
