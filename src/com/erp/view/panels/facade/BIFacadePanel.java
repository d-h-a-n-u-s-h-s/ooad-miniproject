package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class BIFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public BIFacadePanel() { super("Business Intelligence"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("BI Dashboard", "Executive KPIs and strategic metrics.", new String[]{"KPI", "Target", "Actual", "Status"}, "Refresh", "Export"));
        tabs.addTab("Scorecards", new StubTabPanel("Scorecards", "Department and team performance.", new String[]{"Department", "KPI", "Target", "Actual"}, "Create", "Review", "Update"));
        tabs.addTab("Forecasting", new StubTabPanel("Forecasting", "Revenue, demand, and resource forecasts.", new String[]{"Forecast", "Period", "Method", "Confidence"}, "Create", "Model", "Compare"));
        tabs.addTab("Data Warehouse", new StubTabPanel("Data Warehouse", "Centralized data for BI analytics.", new String[]{"Fact Table", "Records", "Last Load", "Quality"}, "Load", "Validate", "Report"));
        tabs.addTab("Reports", new StubTabPanel("BI Reports", "Executive and strategic reports.", new String[]{"Report", "Audience", "Frequency", "Status"}, "Create", "Distribute", "Archive"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
