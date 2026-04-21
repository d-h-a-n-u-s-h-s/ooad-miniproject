package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class AccountingFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public AccountingFacadePanel() { super("Accounting"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Accounting Dashboard", "Compliance and audit status.", new String[]{"Item", "Status", "Due Date", "Owner"}, "Refresh", "Export"));
        tabs.addTab("Tax", new StubTabPanel("Tax Compliance", "GST, income tax, and statutory filings.", new String[]{"Tax Type", "Period", "Amount", "Status"}, "File Return", "Track", "Notify"));
        tabs.addTab("Statements", new StubTabPanel("Financial Statements", "P&L, Balance Sheet, Cash Flow.", new String[]{"Statement", "Period", "Status", "Auditor"}, "Generate", "Export", "Archive"));
        tabs.addTab("Audit Trail", new StubTabPanel("Audit Trail", "Transaction logs and approval workflows.", new String[]{"Transaction", "User", "Timestamp", "Action"}, "View", "Export", "Report"));
        tabs.addTab("Compliance", new StubTabPanel("Compliance", "Regulatory requirements and sign-offs.", new String[]{"Requirement", "Frequency", "Status", "Owner"}, "Review", "Approve", "Notify"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
