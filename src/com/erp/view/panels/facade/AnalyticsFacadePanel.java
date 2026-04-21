package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class AnalyticsFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public AnalyticsFacadePanel() { super("Data Analytics"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Analytics Dashboard", "Key insights and trends.", new String[]{"Metric", "Value", "Target", "Trend"}, "Refresh", "Export"));
        tabs.addTab("Explorer", new StubTabPanel("Data Explorer", "Ad-hoc queries and data drill-down.", new String[]{"Table", "Records", "Last Updated"}, "Query", "Export", "Save"));
        tabs.addTab("Visualizations", new StubTabPanel("Visualizations", "Charts, graphs, and dashboards.", new String[]{"Visualization", "Type", "Data Source", "Owner"}, "Create", "Edit", "Share"));
        tabs.addTab("Trends", new StubTabPanel("Trends Analysis", "Time-series and pattern detection.", new String[]{"Metric", "Period", "Trend", "Forecast"}, "Analyze", "Forecast", "Alert"));
        tabs.addTab("Exports", new StubTabPanel("Data Exports", "Bulk data extracts and feeds.", new String[]{"Export", "Format", "Frequency", "Status"}, "Create", "Schedule", "Monitor"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
