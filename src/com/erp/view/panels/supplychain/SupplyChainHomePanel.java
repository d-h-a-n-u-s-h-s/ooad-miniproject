package com.erp.view.panels.supplychain;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — tabbed Supply Chain / Purchasing module.
 */
public class SupplyChainHomePanel extends BasePanel {

    private JTabbedPane tabs;

    public SupplyChainHomePanel() { super("Supply Chain"); }

    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard",        new SCMDashboardTab());
        tabs.addTab("Inventory",        new InventoryTab());
        tabs.addTab("Purchase Orders",  new PurchaseOrdersTab());
        tabs.addTab("Suppliers",        new SuppliersTab());
        tabs.addTab("Goods Receipts",   new GoodsReceiptTab());
        tabs.addTab("Shipments",        new ShipmentsTab());
        tabs.addTab("Invoices",         new InvoicesTab());
        tabs.addTab("Requisitions",     new RequisitionsTab());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
