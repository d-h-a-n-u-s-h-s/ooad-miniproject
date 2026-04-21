package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class InvoicesTab extends StubTabPanel {
    public InvoicesTab() {
        super(
                "Invoices",
                "Receive and match supplier invoices against purchase orders and goods receipts.",
                new String[]{"Invoice #", "Supplier", "PO #", "Amount", "Due Date", "Status"},
                "Receive Invoice", "Match", "Approve", "Pay"
        );
    }
}
