package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class OrderDeliveryPanel extends StubTabPanel {
    public OrderDeliveryPanel() {
        super(
                "Delivery",
                "Schedule and track shipments against customer orders.",
                new String[]{"Shipment #", "Order #", "Carrier", "Dispatched", "ETA", "Status"},
                "Schedule", "Dispatch", "Track", "Mark Delivered"
        );
    }
}
