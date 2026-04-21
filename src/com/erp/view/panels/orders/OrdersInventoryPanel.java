package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class OrdersInventoryPanel extends StubTabPanel {
    public OrdersInventoryPanel() {
        super(
                "Inventory",
                "Finished-goods stock available for order fulfilment.",
                new String[]{"SKU", "Description", "Warehouse", "On Hand", "Reserved", "Available"},
                "Refresh", "Reserve", "Release", "Transfer"
        );
    }
}
