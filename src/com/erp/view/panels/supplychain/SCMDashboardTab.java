package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class SCMDashboardTab extends StubTabPanel {
    public SCMDashboardTab() {
        super(
                "Supply Chain Dashboard",
                "Real-time view of procurement, inventory, and supplier performance.",
                new String[]{"Metric", "Current", "Target", "Trend", "Status"},
                "Refresh", "Drill Down", "Export"
        );
    }
}
