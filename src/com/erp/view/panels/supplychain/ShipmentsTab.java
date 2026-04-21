package com.erp.view.panels.supplychain;

import com.erp.view.panels.StubTabPanel;

public class ShipmentsTab extends StubTabPanel {
    public ShipmentsTab() {
        super(
                "Shipments",
                "Outbound shipments to customers and inter-plant transfers.",
                new String[]{"Shipment #", "Destination", "Items", "Weight", "Dispatch Date", "Status"},
                "Create Shipment", "Pack", "Dispatch", "Track"
        );
    }
}
