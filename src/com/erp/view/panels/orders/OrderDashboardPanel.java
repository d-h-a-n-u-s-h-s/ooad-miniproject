package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class OrderDashboardPanel extends StubTabPanel {
    public OrderDashboardPanel() {
        super(
                "Order Dashboard",
                "Live pipeline status of orders in draft, approved, and delivered.",
                new String[]{"Order #", "Customer", "Date", "Amount", "Stage", "Owner", "Status"},
                "New Order", "Approve", "Dispatch", "Close"
        );
    }
}
