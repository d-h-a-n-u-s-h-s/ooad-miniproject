package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;
import com.erp.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * ShopFloorExecutionTab allows users to log execution data for production orders.
 */
public class ShopFloorExecutionTab extends JPanel {

    private JComboBox<ProductionOrder> orderCombo;
    private JTextField qtyField;
    private JTable logsTable;
    private DefaultTableModel logsTableModel;
    private TableRowSorter<DefaultTableModel> sorter;

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

        String[] cols = {"Order ID", "Qty Logged", "Timestamp"};
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
        ProductionOrder order = (ProductionOrder) orderCombo.getSelectedItem();
        if (order == null) {
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
            BOMService.getInstance().logShopFloorExecution(order.getId(), order.getProducedQuantity(), qty);
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
            List<ProductionOrder> orders = BOMService.getInstance().getAllProductionOrders();
            
            if (orders != null) {
                for (ProductionOrder o : orders) {
                    if ("Active".equals(o.getOrderStatus())) {
                        orderCombo.addItem(o);
                    }
                }
            }

            // Load logs
            logsTableModel.setRowCount(0);
            List<ShopFloorLog> logs = BOMService.getInstance().getAllShopFloorLogs();
            if (logs != null) {
                for (ShopFloorLog log : logs) {
                    logsTableModel.addRow(new Object[]{
                        log.getProductionOrderId(),
                        log.getQuantityLogged(),
                        log.getTimestamp()
                    });
                }
            }
        } catch (Exception e) {
            // ignore
        }
    }
}
