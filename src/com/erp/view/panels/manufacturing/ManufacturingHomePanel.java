package com.erp.view.panels.manufacturing;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

import com.erp.service.InventoryApiServer;
import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — tabbed Manufacturing module.
 */
public class ManufacturingHomePanel extends BasePanel {

    private JTabbedPane tabs;

    public ManufacturingHomePanel() { 
        super("Manufacturing"); 
        
        // Start the inbound REST API server to receive materials from Supply Chain
        InventoryApiServer.getInstance().startServer();
    }

    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
        
        tabs.addChangeListener(e -> {
            Component c = tabs.getSelectedComponent();
            if (c != null) {
                try {
                    c.getClass().getMethod("refreshData").invoke(c);
                } catch (Exception ex) {
                    // Ignore if the tab doesn't have a refreshData method
                }
            }
        });
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard",         new ManufacturingDashboardTab());
        tabs.addTab("Assembly Lines",    new AssemblyLinesTab());
        tabs.addTab("Production Orders", new ProductionOrdersTab());
        tabs.addTab("BOM Explorer",      new BOMExplorerTab());
        tabs.addTab("Routing",           new RoutingTab());
        tabs.addTab("Work Centers",      new WorkCentersTab());
        tabs.addTab("Quality Control",   new QualityControlTab());
        tabs.addTab("Planning",          new ManufacturingPlanningTab());
        tabs.addTab("Shop Floor",        new ShopFloorExecutionTab());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
