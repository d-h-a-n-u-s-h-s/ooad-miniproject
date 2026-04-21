package com.erp.view.panels.hr;

import com.erp.util.Constants;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import java.awt.*;

/**
 * PATTERN: Composite (Structural) — tabbed HR Management module.
 */
public class HRHomePanel extends BasePanel {

    private JTabbedPane tabs;

    public HRHomePanel() { super("HR Management"); }

    @Override
    protected void initializeComponents() {
        tabs = new JTabbedPane();
        tabs.setFont(Constants.FONT_HEADING);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout());
        tabs.addTab("Employee Info Management",       new EIMSPanel());
        tabs.addTab("Recruitment & ATS",              new RecruitmentPanel());
        tabs.addTab("Onboarding Management",          new OnboardingPanel());
        tabs.addTab("Payroll Management",             new PayrollPanel());
        tabs.addTab("Attendance & Leave Management",  new AttendanceLeavePanel());
        tabs.addTab("Performance Management",         new PerformancePanel());
        tabs.addTab("Workforce Planning & Budgeting", new WorkforcePlanningPanel());
        tabs.addTab("Benefits Administration",        new BenefitsAdministrationPanel());
        contentPanel.add(tabs, BorderLayout.CENTER);
    }
}
