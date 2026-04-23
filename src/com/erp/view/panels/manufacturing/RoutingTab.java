package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;
import com.erp.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * RoutingTab represents the View for Routings.
 */
public class RoutingTab extends JPanel {

    private JTable routeTable;
    private DefaultTableModel tableModel;
    private JComboBox<BOM> bomFilterCombo;

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
        bomFilterCombo.addItem(new BOM(-1, "All BOMs", ""));
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
            BOM selectedFilter = (BOM) bomFilterCombo.getSelectedItem();
            int filterId = selectedFilter != null ? selectedFilter.getId() : -1;

            java.awt.event.ActionListener[] listeners = bomFilterCombo.getActionListeners();
            for (java.awt.event.ActionListener l : listeners) bomFilterCombo.removeActionListener(l);
            
            bomFilterCombo.removeAllItems();
            bomFilterCombo.addItem(new BOM(-1, "All BOMs", ""));
            
            List<BOM> allBoms = BOMService.getInstance().getAllBOMs();
            BOM newSelected = null;
            if (allBoms != null) {
                for (BOM b : allBoms) {
                    bomFilterCombo.addItem(b);
                    if (b.getId() == filterId) {
                        newSelected = b;
                    }
                }
            }
            if (newSelected != null) {
                bomFilterCombo.setSelectedItem(newSelected);
            } else {
                bomFilterCombo.setSelectedIndex(0);
            }
            
            for (java.awt.event.ActionListener l : listeners) bomFilterCombo.addActionListener(l);

            selectedFilter = (BOM) bomFilterCombo.getSelectedItem();
            filterId = selectedFilter != null ? selectedFilter.getId() : -1;

            List<RoutingStep> steps = BOMService.getInstance().getRoutingSteps();
            List<BOM> boms = BOMService.getInstance().getAllBOMs();
            
            // Create a quick lookup for BOM product names
            java.util.Map<Integer, String> bomNames = new java.util.HashMap<>();
            if (boms != null) {
                for (BOM b : boms) {
                    bomNames.put(b.getId(), b.getProductName());
                }
            }

            if (steps != null) {
                for (RoutingStep step : steps) {
                    int routingId = step.getRoutingId();
                    
                    if (filterId != -1 && routingId != filterId) continue;
                    
                    String productName = bomNames.getOrDefault(routingId, "Unknown");
                    
                    tableModel.addRow(new Object[]{
                            routingId,
                            productName,
                            step.getSequenceNumber(),
                            step.getOperationId(),
                            step.getOperationName(),
                            step.getWorkCenterId(),
                            step.getSetupTime(),
                            step.getRunTime()
                    });
                }
            }
        } catch (Exception e) {
            // Ignore if DB is not ready
        }
    }
}
