package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class ViewOrdersPanel extends StubTabPanel {
    public ViewOrdersPanel() {
        super(
                "View Orders",
                "Browse and filter every customer order in the system.",
                new String[]{"Order #", "Customer", "Date", "Amount", "Status", "Owner"},
                "Refresh", "Filter", "Open", "Export"
        );
    }
}
