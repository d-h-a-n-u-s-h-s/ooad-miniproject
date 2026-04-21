package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class ReportingFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public ReportingFacadePanel() { super("Reporting"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Reporting Dashboard", "Report library and schedules.", new String[]{"Report", "Type", "Frequency", "Owner"}, "Refresh", "Export"));
        tabs.addTab("Standard", new StubTabPanel("Standard Reports", "Pre-built operational reports.", new String[]{"Report", "Module", "Last Run", "Frequency"}, "Run", "Schedule", "Export"));
        tabs.addTab("Custom", new StubTabPanel("Custom Reports", "User-defined reports and queries.", new String[]{"Report", "Builder", "Created", "Modified"}, "Create", "Edit", "Delete"));
        tabs.addTab("Schedules", new StubTabPanel("Schedules", "Automated report generation.", new String[]{"Report", "Frequency", "Recipients", "Status"}, "Schedule", "Modify", "Pause"));
        tabs.addTab("Distribution", new StubTabPanel("Distribution", "Report delivery via email or portal.", new String[]{"Report", "Recipient", "Format", "Status"}, "Send", "Archive", "Notify"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
