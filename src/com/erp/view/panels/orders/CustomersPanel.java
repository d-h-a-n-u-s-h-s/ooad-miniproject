package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class CustomersPanel extends StubTabPanel {
    public CustomersPanel() {
        super(
                "Customers",
                "Customer master — dealers, corporates, individual buyers.",
                new String[]{"Code", "Name", "Type", "City", "Contact", "Credit Limit", "Status"},
                "New Customer", "Edit", "Deactivate", "Export"
        );
    }
}
