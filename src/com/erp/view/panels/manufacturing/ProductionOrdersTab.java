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
 * ProductionOrdersTab view.
 */
public class ProductionOrdersTab extends JPanel {

    private JTable orderTable;
    private DefaultTableModel tableModel;

    public ProductionOrdersTab() {
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

        JLabel titleLabel = new JLabel("Production Orders");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Release, track, and close shop-floor production orders.");
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
        
        JButton createBtn = UIHelper.createSecondaryButton("Create Order");
        createBtn.addActionListener(e -> {
            AddProductionOrderDialog dialog = new AddProductionOrderDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isAdded()) refreshData();
        });

        JButton cancelBtn = UIHelper.createDangerButton("Cancel Order");
        cancelBtn.addActionListener(e -> cancelSelectedOrder());
        
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());

        toolbar.add(createBtn);
        toolbar.add(cancelBtn);
        toolbar.add(refreshBtn);
        
        body.add(toolbar, BorderLayout.NORTH);

        String[] columns = {"Order #", "Product Name", "BOM ID", "Quantity (Produced/Total)", "Start Date", "Due Date", "Status"};
        tableModel = new DefaultTableModel(new Object[0][0], columns) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        orderTable = new JTable(tableModel);
        orderTable.setFont(Constants.FONT_REGULAR);
        orderTable.setRowHeight(26);
        orderTable.setGridColor(new Color(230, 232, 236));
        orderTable.setFillsViewportHeight(true);

        JTableHeader header = orderTable.getTableHeader();
        header.setFont(Constants.FONT_HEADING);
        header.setBackground(Constants.PRIMARY_COLOR);
        header.setForeground(Constants.TEXT_LIGHT);
        header.setReorderingAllowed(false);

        body.add(new JScrollPane(orderTable), BorderLayout.CENTER);
        return body;
    }

    private void cancelSelectedOrder() {
        int row = orderTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select an order to cancel.");
            return;
        }
        
        int orderId = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 5);
        
        if ("Cancelled".equals(status)) {
            JOptionPane.showMessageDialog(this, "Order is already cancelled.");
            return;
        }
        
        if ("In Assembly".equals(status) || "Completed".equals(status)) {
            try {
                throw new com.erp.exceptions.ProductionOrderCancellationBlockedException("Cannot cancel production order #" + orderId + " because one or more work orders are already In-Progress.");
            } catch (com.erp.exceptions.ProductionOrderCancellationBlockedException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Cancellation Blocked", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        try {
            BOMService.getInstance().cancelProductionOrder(orderId);
            JOptionPane.showMessageDialog(this, "Production Order Cancelled.");
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage());
        }
    }

    public void refreshData() {
        tableModel.setRowCount(0);
        try {
            List<Map<String, Object>> orders = BOMService.getInstance().getAllProductionOrders();
            List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
            
            java.util.Map<Integer, String> bomNames = new java.util.HashMap<>();
            if (boms != null) {
                for (Map<String, Object> b : boms) {
                    bomNames.put(((Number) b.get("bom_id")).intValue(), (String) b.get("product_name"));
                }
            }
            
            if (orders != null) {
                for (Map<String, Object> o : orders) {
                    int bomId = ((Number) o.get("bom_id")).intValue();
                    String productName = bomNames.getOrDefault(bomId, "Unknown");
                    
                    int orderQty = ((Number) o.get("order_quantity")).intValue();
                    int prodQty = o.get("produced_quantity") != null ? ((Number) o.get("produced_quantity")).intValue() : 0;
                    String qtyStr = prodQty + "/" + orderQty;

                    tableModel.addRow(new Object[]{
                            o.get("production_order_id"),
                            productName,
                            bomId,
                            qtyStr,
                            o.get("start_date") == null ? "" : o.get("start_date").toString(),
                            o.get("due_date") == null ? "" : o.get("due_date").toString(),
                            o.get("order_status")
                    });
                }
            }
        } catch (Exception e) {
            // Ignore if DB is not ready
        }
    }
}
