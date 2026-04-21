package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class OrderEntryPanel extends StubTabPanel {
    public OrderEntryPanel() {
        super(
                "New Order",
                "Create a new customer order — lines, pricing, delivery.",
                new String[]{"Line #", "Part", "Description", "Qty", "UoM", "Unit Price", "Total"},
                "Add Line", "Remove Line", "Save Draft", "Submit"
        );
    }
}
