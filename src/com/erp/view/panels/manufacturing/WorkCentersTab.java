package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * WorkCentersTab represents the View for Work Centers.
 * Principles & Patterns Used:
 * 1. Low Coupling (GRASP): Communicates exclusively through BOMService.
 * 2. Information Expert (GRASP): Calculates Utilization inline from Routing Steps data fetched via Service.
 */
public class WorkCentersTab extends JPanel {

    private JTable wcTable;
    private DefaultTableModel tableModel;

    public WorkCentersTab() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Constants.BG_LIGHT);
        setBorder(new EmptyBorder(Constants.PADDING_LARGE, Constants.PADDING_LARGE,
                Constants.PADDING_LARGE, Constants.PADDING_LARGE));

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        
        refreshData();
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("Work Centers");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Machines and stations that execute routing operations.");
        subtitleLabel.setFont(Constants.FONT_SMALL);
        subtitleLabel.setForeground(Constants.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitleLabel);

        header.add(titles, BorderLayout.WEST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(0, 10));
        body.setBackground(Constants.BG_WHITE);
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        
        JButton addBtn = UIHelper.createSecondaryButton("Add Work Center");
        addBtn.addActionListener(e -> {
            AddWorkCenterDialog dialog = new AddWorkCenterDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isAdded()) refreshData();
        });
        
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        toolbar.add(addBtn);
        toolbar.add(refreshBtn);
        body.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"ID", "Name", "Type", "Capacity (hrs)", "Utilization %", "Location"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        wcTable = new JTable(tableModel);
        wcTable.setFont(Constants.FONT_REGULAR);
        wcTable.setRowHeight(26);
        wcTable.setGridColor(new Color(230, 232, 236));
        wcTable.setFillsViewportHeight(true);

        JTableHeader header = wcTable.getTableHeader();
        header.setFont(Constants.FONT_HEADING);
        header.setBackground(Constants.PRIMARY_COLOR);
        header.setForeground(Constants.TEXT_LIGHT);
        header.setReorderingAllowed(false);

        body.add(new JScrollPane(wcTable), BorderLayout.CENTER);
        return body;
    }

    private void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> centers = BOMService.getInstance().getAllWorkCenters();
            List<Map<String, Object>> routings = BOMService.getInstance().getRoutingSteps();

            for (Map<String, Object> wc : centers) {
                String id = (String) wc.get("work_center_id");
                double capacity = ((Number) wc.get("capacity_hours")).doubleValue();
                
                // Calculate consumed capacity
                double consumed = 0;
                if (routings != null) {
                    for (Map<String, Object> r : routings) {
                        if (id.equals(r.get("work_center_id"))) {
                            double setup = ((Number) r.get("setup_time")).doubleValue();
                            double run = ((Number) r.get("run_time")).doubleValue();
                            consumed += setup + run;
                        }
                    }
                }
                
                double utilPct = capacity > 0 ? (consumed / capacity) * 100 : 0.0;
                String utilStr = String.format("%.2f%%", utilPct);

                tableModel.addRow(new Object[]{
                        id,
                        wc.get("work_center_name"),
                        wc.get("work_center_type"),
                        capacity,
                        utilStr,
                        wc.get("location")
                });
            }
        } catch (Exception e) {
            // Ignore error or log silently if DB not initialized yet
        }
    }
}
