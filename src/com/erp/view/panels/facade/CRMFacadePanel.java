package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class CRMFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public CRMFacadePanel() { super("CRM"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("CRM Dashboard", "Customer engagement snapshot.", new String[]{"Metric", "Value", "Trend"}, "Refresh", "Export"));
        tabs.addTab("Contacts", new StubTabPanel("Contact Management", "Customer and prospect database.", new String[]{"Name", "Company", "Email", "Phone", "Status"}, "Add Contact", "Edit", "Export"));
        tabs.addTab("Leads", new StubTabPanel("Lead Management", "Sales pipeline and lead scoring.", new String[]{"Lead", "Source", "Status", "Score", "Owner"}, "New Lead", "Qualify", "Convert"));
        tabs.addTab("Opportunities", new StubTabPanel("Opportunities", "Sales deals and win/loss analysis.", new String[]{"Opportunity", "Value", "Stage", "Owner", "Close Date"}, "New Deal", "Update Stage", "Close"));
        tabs.addTab("Activities", new StubTabPanel("Activities", "Calls, meetings, and follow-ups.", new String[]{"Date", "Type", "Contact", "Notes", "Outcome"}, "Log Activity", "Schedule", "Complete"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
