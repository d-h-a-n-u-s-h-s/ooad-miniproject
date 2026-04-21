package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class RequisitionsTab extends StubTabPanel {
    public RequisitionsTab() {
        super(
                "Purchase Requisitions",
                "Internal requests for material or services to be procured.",
                new String[]{"Req #", "Department", "Item", "Qty", "Date", "Requester", "Status"},
                "Create Requisition", "Approve", "Convert to PO", "Reject"
        );
    }
}
