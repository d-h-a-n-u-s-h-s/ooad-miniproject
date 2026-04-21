package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class FinanceFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public FinanceFacadePanel() { super("Financial Management"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Finance Dashboard", "Key financial metrics and ratios.", new String[]{"Metric", "Current", "Budget", "Variance"}, "Refresh", "Export"));
        tabs.addTab("General Ledger", new StubTabPanel("General Ledger", "Chart of accounts and journal entries.", new String[]{"Account", "Debit", "Credit", "Balance"}, "Post Entry", "Reverse", "Export"));
        tabs.addTab("A/P", new StubTabPanel("Accounts Payable", "Supplier invoices and payment tracking.", new String[]{"Invoice", "Supplier", "Amount", "Due Date", "Status"}, "Record Invoice", "Pay", "Dispute"));
        tabs.addTab("A/R", new StubTabPanel("Accounts Receivable", "Customer invoices and collections.", new String[]{"Invoice", "Customer", "Amount", "Due Date", "Status"}, "Create Invoice", "Collect", "Write-off"));
        tabs.addTab("Cash Flow", new StubTabPanel("Cash Flow", "Daily cash position and forecasts.", new String[]{"Date", "Inflow", "Outflow", "Net", "Balance"}, "Project", "Monitor", "Alert"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
