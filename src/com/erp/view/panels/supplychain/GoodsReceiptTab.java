package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class GoodsReceiptTab extends StubTabPanel {
    public GoodsReceiptTab() {
        super(
                "Goods Receipt",
                "Receive and inspect incoming materials against purchase orders.",
                new String[]{"GRN #", "PO #", "Supplier", "Items", "Received Date", "Status"},
                "Create GRN", "Receive", "Inspect", "Close"
        );
    }
}
