package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class PurchaseOrdersTab extends StubTabPanel {
    public PurchaseOrdersTab() {
        super(
                "Purchase Orders",
                "Create, track, and receive purchase orders from suppliers.",
                new String[]{"PO #", "Supplier", "Item", "Qty", "Unit Price", "Due Date", "Status"},
                "Create PO", "Send to Supplier", "Receive", "Cancel"
        );
    }
}
