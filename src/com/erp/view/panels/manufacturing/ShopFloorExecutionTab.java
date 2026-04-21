package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * ShopFloorExecutionTab allows users to log execution data for production orders.
 */
public class ShopFloorExecutionTab extends JPanel {

    private JComboBox<OrderItem> orderCombo;
    private JTextField qtyField;
    private JTable logsTable;
    private DefaultTableModel logsTableModel;
    private TableRowSorter<DefaultTableModel> sorter;

    static class OrderItem {
        int id; int currentQty; int totalQty; String name;
        OrderItem(int i, int c, int t, String n) { id=i; currentQty=c; totalQty=t; name=n; }
        public String toString() { return "Order #" + id + " - " + name + " (Produced: " + currentQty + "/" + totalQty + ")"; }
    }

    public ShopFloorExecutionTab() {
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

        JLabel titleLabel = new JLabel("Shop Floor Execution");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Log produced quantities for active production orders.");
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

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Select Active Order:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        orderCombo = new JComboBox<>();
        form.add(orderCombo);

        form.add(UIHelper.createLabel("Quantity Produced (Now):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        qtyField = new JTextField();
        form.add(qtyField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonPanel.setOpaque(false);
        JButton logBtn = UIHelper.createPrimaryButton("Log Execution");
        logBtn.addActionListener(e -> logExecution());
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh Orders");
        refreshBtn.addActionListener(e -> refreshData());
        
        buttonPanel.add(logBtn);
        buttonPanel.add(refreshBtn);

        body.add(form, BorderLayout.NORTH);

        // Logs table
        JPanel logsPanel = new JPanel(new BorderLayout(5, 5));
        logsPanel.setOpaque(false);
        logsPanel.setBorder(BorderFactory.createTitledBorder("Execution Logs"));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Filter by Order #:"));
        JTextField filterField = new JTextField(10);
        filterField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = filterField.getText().trim();
                if (text.length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("^" + text + "$", 1)); // Filter on Order ID column
            }
        });
        filterPanel.add(filterField);
        
        logsPanel.add(filterPanel, BorderLayout.NORTH);

        String[] cols = {"Log ID", "Order ID", "Product Name", "Qty Logged", "Timestamp"};
        logsTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        logsTable = new JTable(logsTableModel);
        sorter = new TableRowSorter<>(logsTableModel);
        logsTable.setRowSorter(sorter);
        
        logsPanel.add(new JScrollPane(logsTable), BorderLayout.CENTER);
        
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, buttonPanel, logsPanel);
        split.setOpaque(false);
        split.setBorder(null);
        
        body.add(split, BorderLayout.CENTER);

        return body;
    }

    private void logExecution() {
        OrderItem item = (OrderItem) orderCombo.getSelectedItem();
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Select an order.");
            return;
        }

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) {
                throw new com.erp.exceptions.InvalidQuantityInputException("Quantity entered must be a positive non-zero number. Value '" + qtyField.getText().trim() + "' is invalid.");
            }
        } catch (com.erp.exceptions.InvalidQuantityInputException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Valid integer quantity required.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            BOMService.getInstance().logShopFloorExecution(item.id, item.currentQty, qty);
            JOptionPane.showMessageDialog(this, "Execution logged successfully!");
            qtyField.setText("");
            refreshData();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshData() {
        orderCombo.removeAllItems();
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
                    if ("Active".equals(status)) {
                        int id = ((Number) o.get("production_order_id")).intValue();
                        int bomId = ((Number) o.get("bom_id")).intValue();
                        int orderQty = ((Number) o.get("order_quantity")).intValue();
                        int prodQty = o.get("produced_quantity") != null ? ((Number) o.get("produced_quantity")).intValue() : 0;
                        String name = bomNames.getOrDefault(bomId, "Unknown");
                        
                        orderCombo.addItem(new OrderItem(id, prodQty, orderQty, name));
                    }
                }
            }

            // Load logs
            logsTableModel.setRowCount(0);
            List<Map<String, Object>> logs = BOMService.getInstance().getAllShopFloorLogs();
            if (logs != null) {
                for (Map<String, Object> log : logs) {
                    int orderId = ((Number) log.get("production_order_id")).intValue();
                    // Find product name
                    String pName = "Unknown";
                    if (orders != null) {
                        for (Map<String, Object> o : orders) {
                            if (((Number) o.get("production_order_id")).intValue() == orderId) {
                                int bId = ((Number) o.get("bom_id")).intValue();
                                pName = bomNames.getOrDefault(bId, "Unknown");
                                break;
                            }
                        }
                    }
                    
                    logsTableModel.addRow(new Object[]{
                        log.get("log_id"),
                        orderId,
                        pName,
                        log.get("quantity_logged"),
                        log.get("log_timestamp")
                    });
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
