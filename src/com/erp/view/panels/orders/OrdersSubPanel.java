package com.erp.view.panels.orders;

import com.erp.util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * Orders section container — LEFT-placement JTabbedPane gives a side-navigation feel
 * (New Order / View Orders / Customers / Billing / Delivery / Payments).
 */
public class OrdersSubPanel extends JPanel {

    public OrdersSubPanel() {
        setLayout(new BorderLayout());
        setBackground(Constants.BG_LIGHT);

        JTabbedPane side = new JTabbedPane(JTabbedPane.LEFT);
        side.setFont(Constants.FONT_REGULAR);
        side.addTab("New Order",   new OrderEntryPanel());
        side.addTab("View Orders", new ViewOrdersPanel());
        side.addTab("Customers",   new CustomersPanel());
        side.addTab("Billing",     new OrderPaymentPanel());
        side.addTab("Delivery",    new OrderDeliveryPanel());
        side.addTab("Payments",    new OrderCancellationPanel());
        add(side, BorderLayout.CENTER);
    }
}
