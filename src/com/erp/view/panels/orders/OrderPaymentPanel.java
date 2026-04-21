package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class OrderPaymentPanel extends StubTabPanel {
    public OrderPaymentPanel() {
        super(
                "Billing",
                "Generate invoices and record payments against orders.",
                new String[]{"Invoice #", "Order #", "Customer", "Amount", "Due Date", "Paid", "Status"},
                "Create Invoice", "Record Payment", "Send Reminder", "Void"
        );
    }
}
