package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class WorkforcePlanningPanel extends StubTabPanel {
    public WorkforcePlanningPanel() {
        super(
                "Workforce Planning & Budgeting",
                "Headcount forecasts, budgets, and organizational planning.",
                new String[]{"Department", "Current", "Planned", "Budget", "Variance", "Owner"},
                "New Plan", "Revise Budget", "Approve", "Export"
        );
    }
}
