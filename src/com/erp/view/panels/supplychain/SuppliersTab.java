package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class SuppliersTab extends StubTabPanel {
    public SuppliersTab() {
        super(
                "Supplier Master",
                "Supplier database, performance ratings, and contract terms.",
                new String[]{"Code", "Name", "Category", "Location", "Rating", "Status"},
                "Add Supplier", "Edit", "Rate", "Deactivate"
        );
    }
}
