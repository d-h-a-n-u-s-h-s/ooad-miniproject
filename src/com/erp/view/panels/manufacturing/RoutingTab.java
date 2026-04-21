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
 * RoutingTab represents the View for Routings.
 * Principles & Patterns Used:
 * 1. Low Coupling (GRASP): Fetches data exclusively through BOMService without touching SQL.
 * 2. MVC Architecture: The panel acts as the View and Controller for user gestures.
 */
public class RoutingTab extends JPanel {

    private JTable routeTable;
    private DefaultTableModel tableModel;
    private JComboBox<BomItem> bomFilterCombo;

    static class BomItem {
        int id; String name; String version;
        BomItem(int i, String n, String v) { id=i; name=n; version=v; }
        public String toString() { return id == -1 ? name : name + " (" + version + ")"; }
    }

    public RoutingTab() {
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

        JLabel titleLabel = new JLabel("Routing");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Operation sequences across work centers for each BOM.");
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
        
        JButton addBtn = UIHelper.createSecondaryButton("Add Routing Step");
        addBtn.addActionListener(e -> {
            AddRoutingStepDialog dialog = new AddRoutingStepDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isAdded()) refreshData();
        });
        
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        toolbar.add(addBtn);
        toolbar.add(refreshBtn);
        
        toolbar.add(Box.createHorizontalStrut(15));
        toolbar.add(UIHelper.createLabel("Filter by BOM: ", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        bomFilterCombo = new JComboBox<>();
        bomFilterCombo.addItem(new BomItem(-1, "All BOMs", ""));
        bomFilterCombo.addActionListener(e -> refreshData());
        toolbar.add(bomFilterCombo);
        
        body.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"BOM ID", "Product Name", "Step #", "Op ID", "Operation Name", "Work Center", "Setup Time", "Run Time"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        routeTable = new JTable(tableModel);
        routeTable.setFont(Constants.FONT_REGULAR);
        routeTable.setRowHeight(26);
        routeTable.setGridColor(new Color(230, 232, 236));
        routeTable.setFillsViewportHeight(true);

        JTableHeader header = routeTable.getTableHeader();
        header.setFont(Constants.FONT_HEADING);
        header.setBackground(Constants.PRIMARY_COLOR);
        header.setForeground(Constants.TEXT_LIGHT);
        header.setReorderingAllowed(false);

        body.add(new JScrollPane(routeTable), BorderLayout.CENTER);
        return body;
    }

    private void refreshData() {
        tableModel.setRowCount(0);
        try {
            // Populate filter combo if it's empty (except for "All BOMs")
            if (bomFilterCombo.getItemCount() <= 1) {
                List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
                if (boms != null) {
                    for (Map<String, Object> b : boms) {
                        bomFilterCombo.addItem(new BomItem(((Number) b.get("bom_id")).intValue(), (String) b.get("product_name"), (String) b.get("bom_version")));
                    }
                }
            }

            BomItem selectedFilter = (BomItem) bomFilterCombo.getSelectedItem();
            int filterId = selectedFilter != null ? selectedFilter.id : -1;

            List<Map<String, Object>> steps = BOMService.getInstance().getRoutingSteps();
            List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
            
            // Create a quick lookup for BOM product names
            java.util.Map<Integer, String> bomNames = new java.util.HashMap<>();
            if (boms != null) {
                for (Map<String, Object> b : boms) {
                    bomNames.put(((Number) b.get("bom_id")).intValue(), (String) b.get("product_name"));
                }
            }

            if (steps != null) {
                for (Map<String, Object> step : steps) {
                    int routingId = ((Number) step.get("routing_id")).intValue();
                    
                    if (filterId != -1 && routingId != filterId) continue;
                    
                    String productName = bomNames.getOrDefault(routingId, "Unknown");
                    
                    tableModel.addRow(new Object[]{
                            routingId,
                            productName,
                            step.get("sequence_number"),
                            step.get("operation_id"),
                            step.get("operation_name"),
                            step.get("work_center_id"),
                            step.get("setup_time"),
                            step.get("run_time")
                    });
                }
            }
        } catch (Exception e) {
            // Ignore if DB is not ready
        }
    }
}
