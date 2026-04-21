package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class ProjectFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public ProjectFacadePanel() { super("Project Management"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Project Dashboard", "Portfolio health and burn-down.", new String[]{"Project", "Status", "Progress", "Owner"}, "Refresh", "Export"));
        tabs.addTab("Portfolio", new StubTabPanel("Portfolio Management", "Strategic projects and priorities.", new String[]{"Project", "Start", "End", "Budget", "Status"}, "New Project", "Prioritize", "Archive"));
        tabs.addTab("Tasks", new StubTabPanel("Task Management", "Work breakdown and activity tracking.", new String[]{"Task", "Assigned To", "Start", "End", "Status"}, "Create Task", "Assign", "Complete"));
        tabs.addTab("Resources", new StubTabPanel("Resource Planning", "Team allocation and capacity.", new String[]{"Resource", "Skill", "Allocation", "Availability"}, "Allocate", "Reallocate", "Report"));
        tabs.addTab("Costs", new StubTabPanel("Budget & Costs", "Project budgets and actuals.", new String[]{"Cost Code", "Budget", "Actual", "Variance"}, "Set Budget", "Track", "Report"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
