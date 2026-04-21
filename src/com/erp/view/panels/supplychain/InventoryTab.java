package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class InventoryTab extends StubTabPanel {
    public InventoryTab() {
        super(
                "Inventory Management",
                "Stock levels, reorder points, and warehouse allocation.",
                new String[]{"SKU", "Description", "Warehouse", "On Hand", "Reorder Point", "Allocated"},
                "Receive Stock", "Issue Stock", "Transfer", "Adjust Count"
        );
    }
}
