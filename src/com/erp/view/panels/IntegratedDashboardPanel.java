package com.erp.view.panels;

import com.erp.util.Constants;
import com.erp.view.components.DashboardCard;
import com.erp.view.components.FakeChartPanel;

import javax.swing.*;
import java.awt.*;

/**
 * Cross-subsystem executive dashboard (UI-only mockup).
 */
public class IntegratedDashboardPanel extends BasePanel {

    private final DashboardCard ordersTotal     = new DashboardCard("Automation Orders", "-", "all pipelines",    Constants.PRIMARY_COLOR);
    private final DashboardCard ordersPending   = new DashboardCard("Workflow Ready",    "-", "approved orders",  Constants.WARNING_COLOR);
    private final DashboardCard ordersDelivered = new DashboardCard("Delivered",         "-", "fulfilled",        Constants.SUCCESS_COLOR);
    private final DashboardCard openPo          = new DashboardCard("Open Purchase Orders", "-", "procurement",   Constants.ACCENT_COLOR);
    private final DashboardCard lowStock        = new DashboardCard("Low Stock",         "-", "reorder needed",   Constants.PRIMARY_DARK);
    private final DashboardCard mfgInProgress   = new DashboardCard("Mfg In Progress",   "-", "shop floor",       Constants.DANGER_COLOR);

    private final FakeChartPanel ordersChart = new FakeChartPanel("Order Pipeline",       FakeChartPanel.Style.BAR);
    private final FakeChartPanel mfgChart    = new FakeChartPanel("Manufacturing Status", FakeChartPanel.Style.BAR);

    private JLabel greetingLabel;

    public IntegratedDashboardPanel() { super("Executive Dashboard"); }

    @Override
    protected void initializeComponents() {
        greetingLabel = new JLabel();
        greetingLabel.setFont(Constants.FONT_SUBTITLE);
        greetingLabel.setForeground(Constants.TEXT_PRIMARY);
    }

    @Override
    protected void layoutComponents() {
        contentPanel.setLayout(new BorderLayout(0, 10));

        JPanel hero = new JPanel(new BorderLayout());
        hero.setBackground(Constants.BG_WHITE);
        hero.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232)),
                BorderFactory.createEmptyBorder(14, 18, 14, 18)));
        hero.add(greetingLabel, BorderLayout.WEST);
        JLabel hint = new JLabel("Live view across every ERP module");
        hint.setFont(Constants.FONT_SMALL);
        hint.setForeground(Constants.TEXT_SECONDARY);
        hero.add(hint, BorderLayout.EAST);

        JPanel stats = new JPanel(new GridLayout(2, 3, 10, 10));
        stats.setOpaque(false);
        stats.add(ordersTotal); stats.add(ordersPending); stats.add(ordersDelivered);
        stats.add(openPo); stats.add(lowStock); stats.add(mfgInProgress);

        JPanel charts = new JPanel(new GridLayout(1, 2, 10, 0));
        charts.setOpaque(false);
        charts.add(ordersChart); charts.add(mfgChart);

        JPanel north = new JPanel(new BorderLayout(0, 10));
        north.setOpaque(false);
        north.add(hero, BorderLayout.NORTH);
        north.add(stats, BorderLayout.CENTER);

        contentPanel.add(north, BorderLayout.NORTH);
        contentPanel.add(charts, BorderLayout.CENTER);
        refreshData();
    }

    @Override
    public void refreshData() {
        greetingLabel.setText("Welcome back");
    }
}
