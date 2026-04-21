package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * QualityControlTab manages final quality checks on completed orders.
 */
public class QualityControlTab extends JPanel {

    private JComboBox<OrderItem> orderCombo;
    private JTextField defectsField;
    
    private JTable qcTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;

    static class OrderItem {
        int id; String name; int producedQty; String qcStatus;
        OrderItem(int i, String n, int p, String q) { id=i; name=n; producedQty=p; qcStatus=q; }
        public String toString() { return "Order #" + id + " - " + name + " (Qty: " + producedQty + ")"; }
    }

    public QualityControlTab() {
        setLayout(new BorderLayout(0, 10));
        setBackground(Constants.BG_LIGHT);
        setBorder(new EmptyBorder(Constants.PADDING_LARGE, Constants.PADDING_LARGE, Constants.PADDING_LARGE, Constants.PADDING_LARGE));

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

        JLabel titleLabel = new JLabel("Quality Control");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Log defects on Completed production orders to determine Pass/Fail rate.");
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
        body.setOpaque(false);

        // Top Form Panel
        JPanel formWrapper = new JPanel(new BorderLayout(0, 10));
        formWrapper.setBackground(Constants.BG_WHITE);
        formWrapper.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 232), 1, true),
                new EmptyBorder(14, 14, 14, 14)));

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Select Completed Order (Pending QC):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        orderCombo = new JComboBox<>();
        form.add(orderCombo);

        form.add(UIHelper.createLabel("Defective Pieces Found:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        defectsField = new JTextField();
        form.add(defectsField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        JButton logBtn = UIHelper.createPrimaryButton("Log Quality Check");
        logBtn.addActionListener(e -> logQC());
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        
        buttonPanel.add(logBtn);
        buttonPanel.add(refreshBtn);

        formWrapper.add(form, BorderLayout.NORTH);
        formWrapper.add(buttonPanel, BorderLayout.CENTER);

        body.add(formWrapper, BorderLayout.NORTH);

        // Bottom Table and Details Panel
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
        bottomPanel.setOpaque(false);

        // Table
        String[] cols = {"Order ID", "Product", "Produced", "Defects", "QC Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        qcTable = new JTable(tableModel);
        qcTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateDetailsPane();
        });
        
        JScrollPane tableScroll = new JScrollPane(qcTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("QC History"));
        
        // Details Pane
        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        detailsArea.setBackground(new Color(248, 249, 250));
        detailsArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Detailed QC Report"));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, detailsScroll);
        splitPane.setDividerLocation(450);
        splitPane.setOpaque(false);

        bottomPanel.add(splitPane, BorderLayout.CENTER);
        
        body.add(bottomPanel, BorderLayout.CENTER);

        return body;
    }

    private void updateDetailsPane() {
        int row = qcTable.getSelectedRow();
        if (row < 0) {
            detailsArea.setText("Select a record to view the detailed report.");
            return;
        }

        String orderIdStr = String.valueOf(tableModel.getValueAt(row, 0));
        String productName = String.valueOf(tableModel.getValueAt(row, 1));
        String producedStr = String.valueOf(tableModel.getValueAt(row, 2));
        String defectsStr = String.valueOf(tableModel.getValueAt(row, 3));
        String result = String.valueOf(tableModel.getValueAt(row, 4));

        int producedQty = 0;
        int defects = 0;
        try {
            producedQty = Integer.parseInt(producedStr);
            defects = Integer.parseInt(defectsStr);
        } catch (Exception e) {}
        
        double rate = producedQty > 0 ? ((double) defects / producedQty) * 100 : 0;

        String report = String.format(
            "=== Quality Control Report ===\n\n" +
            "Production Order : #%s\n" +
            "Product Name     : %s\n" +
            "Total Produced   : %d units\n" +
            "Defective Units  : %d units\n" +
            "-----------------------------------\n" +
            "Calculated Rate  : %.2f%%\n" +
            "Pass Threshold   : 5.00%%\n\n" +
            "FINAL DECISION   : %s",
            orderIdStr, productName, producedQty, defects, rate, result
        );
        
        detailsArea.setText(report);
    }

    private void logQC() {
        OrderItem item = (OrderItem) orderCombo.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Select a pending order.");
            return;
        }

        int defects;
        try {
            defects = Integer.parseInt(defectsField.getText().trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Valid integer required.");
            return;
        }

        try {
            BOMService.getInstance().logQualityCheck(item.id, defects, item.producedQty);
            JOptionPane.showMessageDialog(this, "QC Logged Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            defectsField.setText("");
            refreshData();
        } catch (com.erp.exceptions.QcDefectThresholdExceededException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Quality Threshold Exceeded", JOptionPane.WARNING_MESSAGE);
            defectsField.setText("");
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        orderCombo.removeAllItems();
        tableModel.setRowCount(0);
        detailsArea.setText("Select a record to view the detailed report.");
        
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
                    String status = (String) o.get("order_status");
                    if ("Completed".equals(status)) {
                        int id = ((Number) o.get("production_order_id")).intValue();
                        int bomId = ((Number) o.get("bom_id")).intValue();
                        int prodQty = o.get("produced_quantity") != null ? ((Number) o.get("produced_quantity")).intValue() : 0;
                        int orderQty = o.get("order_quantity") != null ? ((Number) o.get("order_quantity")).intValue() : 0;
                        int defects = o.get("defects") != null ? ((Number) o.get("defects")).intValue() : 0;
                        String qc = (String) o.get("qc_status");
                        if (qc == null) qc = "Pending";
                        
                        // Only evaluate if all items were physically produced
                        if (prodQty < orderQty) continue;
                        
                        String name = bomNames.getOrDefault(bomId, "Unknown");
                        
                        // If pending, add to combo box so it can be evaluated exactly once
                        if ("Pending".equals(qc)) {
                            orderCombo.addItem(new OrderItem(id, name, prodQty, qc));
                        } else {
                            // If it has a result, add to the history table
                            tableModel.addRow(new Object[]{ id, name, prodQty, defects, qc });
                        }
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
