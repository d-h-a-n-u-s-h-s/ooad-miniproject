package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class OrderCancellationPanel extends StubTabPanel {
    public OrderCancellationPanel() {
        super(
                "Payments & Cancellations",
                "Refund requests and order cancellations with approvals.",
                new String[]{"Request #", "Order #", "Customer", "Reason", "Amount", "Status"},
                "New Request", "Approve", "Reject", "Refund"
        );
    }
}
