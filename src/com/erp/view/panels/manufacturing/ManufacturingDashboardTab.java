package com.erp.view.panels.manufacturing;

import com.erp.view.panels.StubTabPanel;

public class ManufacturingDashboardTab extends StubTabPanel {
    public ManufacturingDashboardTab() {
        super(
                "Manufacturing Dashboard",
                "Plant-wide snapshot of production, work orders, and OEE.",
                new String[]{"Line", "Status", "Current Order", "Output", "OEE", "Downtime"},
                "Refresh", "Drill Down", "Export"
        );
    }
}
