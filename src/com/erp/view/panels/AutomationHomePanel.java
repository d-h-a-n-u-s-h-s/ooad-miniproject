package com.erp.view.panels;

import com.erp.util.Constants;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — tabbed Automation module.
 */
public class AutomationHomePanel extends BasePanel {

    private JTabbedPane tabs;

    public AutomationHomePanel() { super("Automation"); }

    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Dashboard", new StubTabPanel(
                "Automation Dashboard",
                "Cross-module automation runs, triggers, and workflow status.",
                new String[]{"Rule", "Trigger", "Module", "Last Run", "Status"},
                "New Rule", "Enable", "Disable"
        ));
        tabs.addTab("Workflows", new StubTabPanel(
                "Workflow Builder",
                "Visual workflow designer for multi-step automation.",
                new String[]{"Workflow", "Trigger", "Actions", "Status"},
                "New Workflow", "Edit", "Test"
        ));
        tabs.addTab("Rules Engine", new StubTabPanel(
                "Rules Engine",
                "Business rules that automatically trigger actions.",
                new String[]{"Rule", "Condition", "Action", "Priority"},
                "Create Rule", "Edit", "Activate"
        ));
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
