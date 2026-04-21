package com.erp.view.panels.facade;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;
import com.erp.view.panels.StubTabPanel;

import javax.swing.*;
import java.awt.*;

public class MarketingFacadePanel extends BasePanel {
    private JTabbedPane tabs;
    public MarketingFacadePanel() { super("Marketing"); }
    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }
    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel("Marketing Dashboard", "Campaign performance and ROI.", new String[]{"Campaign", "Budget", "Spend", "ROI"}, "Refresh", "Export"));
        tabs.addTab("Campaigns", new StubTabPanel("Campaigns", "Design and execute marketing campaigns.", new String[]{"Campaign", "Channel", "Budget", "Status"}, "New Campaign", "Launch", "Pause"));
        tabs.addTab("Segments", new StubTabPanel("Segments", "Audience segmentation and targeting.", new String[]{"Segment", "Size", "Profile", "Value"}, "Create", "Edit", "Target"));
        tabs.addTab("Email", new StubTabPanel("Email Marketing", "Email campaigns and nurture sequences.", new String[]{"Email", "Sent", "Opens", "Clicks"}, "Create", "Send", "Track"));
        tabs.addTab("Analytics", new StubTabPanel("Analytics", "Campaign metrics and attribution.", new String[]{"Metric", "Value", "Trend", "Goal"}, "Analyze", "Attribute", "Report"));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
