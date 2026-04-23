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

/**
 * ManufacturingPlanningTab represents the MRP view.
 */
public class ManufacturingPlanningTab extends JPanel {

    private JTable planTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;

    public ManufacturingPlanningTab() {
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

        JLabel titleLabel = new JLabel("Manufacturing Planning");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("MRP-style planning: demand, supply, and capacity balance.");
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
        
        JButton mrpBtn = UIHelper.createSecondaryButton("Enter MRP Details");
        mrpBtn.addActionListener(e -> {
            MRPDetailsDialog dialog = new MRPDetailsDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isPlanReleased()) refreshData();
        });

        JButton adjustBtn = UIHelper.createSecondaryButton("Adjust");
        adjustBtn.addActionListener(e -> {
            int row = planTable.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Select a plan to adjust.");
                return;
            }
            int planId = (int) tableModel.getValueAt(row, 0);
            int bomId = (int) tableModel.getValueAt(row, 2);
            int qty = (int) tableModel.getValueAt(row, 3);
            String startDate = (String) tableModel.getValueAt(row, 4);
            
            AdjustPlanDialog dialog = new AdjustPlanDialog(SwingUtilities.getWindowAncestor(this), planId, bomId, qty, startDate);
            dialog.setVisible(true);
            if (dialog.isAdjusted()) refreshData();
        });

        JButton exportBtn = UIHelper.createSecondaryButton("Export");
        exportBtn.addActionListener(e -> UIHelper.showSuccess(this, "Plan exported successfully."));

        JButton convertBtn = UIHelper.createPrimaryButton("Convert to Production Order");
        convertBtn.addActionListener(e -> convertSelectedPlan());
        
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        toolbar.add(mrpBtn);
        toolbar.add(adjustBtn);
        toolbar.add(exportBtn);
        toolbar.add(convertBtn);
        toolbar.add(refreshBtn);
        
        body.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"Plan ID", "Product Name", "BOM ID", "Qty", "Start Date", "Total Cost", "Total Hours", "Status"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        planTable = new JTable(tableModel);
        planTable.setFont(Constants.FONT_REGULAR);
        planTable.setRowHeight(26);
        planTable.setGridColor(new Color(230, 232, 236));
        planTable.setFillsViewportHeight(true);

        JTableHeader header = planTable.getTableHeader();
        header.setFont(Constants.FONT_HEADING);
        header.setBackground(Constants.PRIMARY_COLOR);
        header.setForeground(Constants.TEXT_LIGHT);
        header.setReorderingAllowed(false);

        body.add(new JScrollPane(planTable), BorderLayout.CENTER);

        // Details pane at the bottom
        detailsArea = new JTextArea(6, 40);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        detailsArea.setBackground(new Color(248, 249, 250));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Plan Details"));
        body.add(detailsScroll, BorderLayout.SOUTH);

        // Add selection listener
        planTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateDetailsPane();
            }
        });

        return body;
    }

    private void updateDetailsPane() {
        int row = planTable.getSelectedRow();
        if (row < 0) {
            detailsArea.setText("Select a plan to view details.");
            return;
        }

        String planId = String.valueOf(tableModel.getValueAt(row, 0));
        String productName = String.valueOf(tableModel.getValueAt(row, 1));
        String qty = String.valueOf(tableModel.getValueAt(row, 3));
        String startDate = String.valueOf(tableModel.getValueAt(row, 4));
        String totalCost = String.valueOf(tableModel.getValueAt(row, 5));
        String totalHours = String.valueOf(tableModel.getValueAt(row, 6));
        String status = String.valueOf(tableModel.getValueAt(row, 7));

        StringBuilder sb = new StringBuilder();
        sb.append("=== Plan Generation Details ===\n");
        sb.append("Plan ID: ").append(planId).append("\n");
        sb.append("Product: ").append(productName).append("\n");
        sb.append("Planned Quantity: ").append(qty).append("\n");
        sb.append("Start Date: ").append(startDate).append("\n");
        sb.append("Status: ").append(status).append("\n\n");
        
        sb.append("=== Cost & Capacity Breakdown ===\n");
        sb.append("Total Estimated Cost: ₹").append(totalCost).append("\n");
        sb.append("Total Routing Hours Required: ").append(totalHours).append(" hrs\n");
        sb.append("(Note: Total Cost includes both multilevel BOM material cost and an operational cost of ₹100 per hour.)\n");
        
        detailsArea.setText(sb.toString());
    }

    private void convertSelectedPlan() {
        int row = planTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a plan to convert.");
            return;
        }
        
        int planId = (int) tableModel.getValueAt(row, 0);
        int bomId = (int) tableModel.getValueAt(row, 2);
        int qty = (int) tableModel.getValueAt(row, 3);
        String startDate = (String) tableModel.getValueAt(row, 4);
        String status = (String) tableModel.getValueAt(row, 7);
        
        if ("Converted".equals(status)) {
            JOptionPane.showMessageDialog(this, "Plan is already converted.");
            return;
        }

        try {
            // Check if existing production order exists for this plan
            List<ProductionOrder> orders = BOMService.getInstance().getAllProductionOrders();
            int existingOrderId = -1;
            if (orders != null) {
                for (ProductionOrder o : orders) {
                    if (o.getPlanId() == planId) {
                        existingOrderId = o.getId();
                        break;
                    }
                }
            }

            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_YEAR, 7);
            String endDate = new java.text.SimpleDateFormat("yyyy-MM-dd").format(cal.getTime());

            ProductionOrder order = new ProductionOrder();
            order.setBomId(bomId);
            order.setOrderQuantity(qty);
            order.setStartDate(startDate);
            order.setDueDate(endDate);
            order.setPlanId(planId);
            order.setOrderStatus("Active");

            if (existingOrderId != -1) {
                order.setId(existingOrderId);
                BOMService.getInstance().updateProductionOrder(order);
            } else {
                BOMService.getInstance().createProductionOrder(order);
            }
            
            BOMService.getInstance().updateProductionPlanStatus(planId, "Converted");
            JOptionPane.showMessageDialog(this, "Converted to Production Order successfully.");
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage());
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<ProductionPlan> plans = BOMService.getInstance().getAllProductionPlans();
            List<BOM> boms = BOMService.getInstance().getAllBOMs();
            
            java.util.Map<Integer, String> bomNames = new java.util.HashMap<>();
            if (boms != null) {
                for (BOM b : boms) {
                    bomNames.put(b.getId(), b.getProductName());
                }
            }
            
            if (plans != null) {
                for (ProductionPlan p : plans) {
                    int bomId = p.getBomId();
                    String productName = bomNames.getOrDefault(bomId, "Unknown");
                    tableModel.addRow(new Object[]{
                            p.getId(),
                            productName,
                            bomId,
                            p.getPlannedQuantity(),
                            p.getStartDate() == null ? "" : p.getStartDate(),
                            p.getTotalCost(),
                            p.getTotalHours(),
                            p.getStatus()
                    });
                }
            }
            if (planTable.getRowCount() > 0 && planTable.getSelectedRow() == -1) {
                updateDetailsPane();
            }
        } catch (Exception e) {
            // Ignore if DB is not ready
        }
    }
}
