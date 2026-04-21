package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class AutomationDashboardTab extends StubTabPanel {
    public AutomationDashboardTab() {
        super(
                "Automation Dashboard",
                "Cross-module automation: triggers, rules, and workflow runs.",
                new String[]{"Rule", "Trigger", "Module", "Runs", "Last Run", "Status"},
                "New Rule", "Enable", "Disable", "View Logs"
        );
    }
}
