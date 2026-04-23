package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;
import com.erp.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * AssemblyLinesTab manages assembly lines specifically tied to active production orders.
 */
public class AssemblyLinesTab extends JPanel {

    private JComboBox<ProductionOrder> orderCombo;
    private JLabel statusLabel;
    private JLabel currentLineLabel;
    
    private JTable linesTable;
    private DefaultTableModel linesTableModel;
    
    private JTable historyTable;
    private DefaultTableModel historyTableModel;

    public AssemblyLinesTab() {
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

        JLabel titleLabel = new JLabel("Assembly Lines & Routing");
        titleLabel.setFont(Constants.FONT_SUBTITLE);
        titleLabel.setForeground(Constants.TEXT_PRIMARY);
        titleLabel.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Manage assembly lines dynamically for each production order.");
        subtitleLabel.setFont(Constants.FONT_SMALL);
        subtitleLabel.setForeground(Constants.TEXT_SECONDARY);
        subtitleLabel.setAlignmentX(LEFT_ALIGNMENT);
        
        titles.add(titleLabel);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitleLabel);

        // Order Selector
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanel.setOpaque(false);
        filterPanel.add(new JLabel("Select Order: "));
        orderCombo = new JComboBox<>();
        orderCombo.setPreferredSize(new Dimension(250, 30));
        orderCombo.addActionListener(e -> updateOrderDetails());
        filterPanel.add(orderCombo);
        
        JButton refreshBtn = UIHelper.createSecondaryButton("Refresh");
        refreshBtn.addActionListener(e -> refreshData());
        filterPanel.add(refreshBtn);

        header.add(titles, BorderLayout.WEST);
        header.add(filterPanel, BorderLayout.EAST);
        return header;
    }

    private JPanel buildBody() {
        JPanel body = new JPanel(new BorderLayout(10, 10));
        body.setOpaque(false);

        // Top Status Section
        JPanel statusPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        statusPanel.setBorder(BorderFactory.createTitledBorder("Order Status"));
        statusPanel.setBackground(Constants.BG_WHITE);
        
        statusLabel = new JLabel("Status: -");
        statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        currentLineLabel = new JLabel("Current Line: -");
        currentLineLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        
        JPanel p1 = new JPanel(new FlowLayout(FlowLayout.CENTER)); p1.setOpaque(false); p1.add(statusLabel);
        JPanel p2 = new JPanel(new FlowLayout(FlowLayout.CENTER)); p2.setOpaque(false); p2.add(currentLineLabel);
        
        statusPanel.add(p1);
        statusPanel.add(p2);
        
        body.add(statusPanel, BorderLayout.NORTH);

        // Left Panel: Lines Table + Create Line
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        leftPanel.setOpaque(false);
        leftPanel.setBorder(BorderFactory.createTitledBorder("Assembly Lines for Order"));
        
        String[] cols = {"Line ID", "Name", "Sequence", "Work Center"};
        linesTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        linesTable = new JTable(linesTableModel);
        JScrollPane scroll = new JScrollPane(linesTable);
        leftPanel.add(scroll, BorderLayout.CENTER);
        
        JPanel createLinePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        createLinePanel.setOpaque(false);
        JButton createLineBtn = UIHelper.createSecondaryButton("Create Line for Order");
        createLineBtn.addActionListener(e -> createLine());
        createLinePanel.add(createLineBtn);
        leftPanel.add(createLinePanel, BorderLayout.SOUTH);

        // History Panel
        JPanel historyPanel = new JPanel(new BorderLayout(5, 5));
        historyPanel.setOpaque(false);
        historyPanel.setBorder(BorderFactory.createTitledBorder("Movement History"));
        
        String[] histCols = {"Line Name", "Timestamp"};
        historyTableModel = new DefaultTableModel(histCols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        historyPanel.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        
        JSplitPane innerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftPanel, historyPanel);
        innerSplit.setDividerLocation(150);
        innerSplit.setOpaque(false);

        // Right Panel: Actions
        JPanel rightPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        rightPanel.setOpaque(false);
        
        JPanel assignPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        assignPanel.setBorder(BorderFactory.createTitledBorder("Start Assembly"));
        assignPanel.setOpaque(false);
        JButton assignBtn = UIHelper.createPrimaryButton("Assign to First Line");
        assignBtn.addActionListener(e -> assignOrder());
        assignPanel.add(assignBtn);
        
        JPanel movePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        movePanel.setBorder(BorderFactory.createTitledBorder("Progress Assembly"));
        movePanel.setOpaque(false);
        JButton moveBtn = UIHelper.createPrimaryButton("Advance to Next Line");
        moveBtn.addActionListener(e -> moveOrder());
        movePanel.add(moveBtn);
        
        rightPanel.add(assignPanel);
        rightPanel.add(movePanel);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, innerSplit, rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setOpaque(false);
        
        body.add(splitPane, BorderLayout.CENTER);

        return body;
    }
    
    private void updateOrderDetails() {
        ProductionOrder order = (ProductionOrder) orderCombo.getSelectedItem();
        linesTableModel.setRowCount(0);
        if (historyTableModel != null) historyTableModel.setRowCount(0);
        if (order == null) {
            statusLabel.setText("Status: -");
            currentLineLabel.setText("Current Line: -");
            return;
        }
        
        if ("Completed".equals(order.getOrderStatus())) {
            statusLabel.setText("Status: COMPLETED");
            statusLabel.setForeground(new Color(34, 139, 34)); // Dark green
        } else {
            statusLabel.setText("Status: " + order.getOrderStatus());
            statusLabel.setForeground(Constants.TEXT_PRIMARY);
        }
        
        try {
            List<AssemblyLine> lines = BOMService.getInstance().getAllAssemblyLines();
            List<AssemblyMovement> movements = BOMService.getInstance().getAllAssemblyMovements();
            String currentLineName = "Not Assigned";
            
            java.util.Map<Integer, String> lineNames = new java.util.HashMap<>();
            if (lines != null) {
                // sort by sequence
                lines.sort((a,b) -> Integer.compare(a.getSequenceNum(), b.getSequenceNum()));
                for (AssemblyLine l : lines) {
                    if (l.getProductionOrderId() == order.getId()) {
                        System.out.println("DEBUG UI: Match found! Line '" + l.getName() + "' for Order #" + order.getId());
                        lineNames.put(l.getId(), l.getName());
                        linesTableModel.addRow(new Object[]{ l.getId(), l.getName(), l.getSequenceNum(), l.getWorkCenterId() });
                        
                        if (l.getId() == order.getCurrentLineId()) {
                            currentLineName = l.getName();
                        }
                    }
                }
            }
            
            if (movements != null) {
                movements.sort((a,b) -> {
                    String ta = a.getTimestamp();
                    String tb = b.getTimestamp();
                    if (ta == null) return -1;
                    if (tb == null) return 1;
                    return ta.compareTo(tb);
                });
                for (AssemblyMovement m : movements) {
                    if (m.getProductionOrderId() == order.getId()) {
                        String lineName = lineNames.getOrDefault(m.getLineId(), "Unknown Line (ID: " + m.getLineId() + ")");
                        historyTableModel.addRow(new Object[]{ lineName, m.getTimestamp() });
                    }
                }
            }
            
            currentLineLabel.setText("Current Line: " + currentLineName);
            if (order.getCurrentLineId() == -1 && "Completed".equals(order.getOrderStatus())) {
                currentLineLabel.setText("Current Line: Finished");
            }
            
            linesTable.revalidate();
            linesTable.repaint();
        } catch (Exception e) {
            System.err.println("ERROR: Failed to update order details for Order #" + (order != null ? order.getId() : "NULL"));
            e.printStackTrace();
        }
    }

    private void createLine() {
        ProductionOrder order = (ProductionOrder) orderCombo.getSelectedItem();
        if (order == null) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }
        if ("Completed".equals(order.getOrderStatus())) {
            JOptionPane.showMessageDialog(this, "Cannot create lines for a completed order.");
            return;
        }

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Line Name:"));
        JTextField nameField = new JTextField();
        panel.add(nameField);
        panel.add(new JLabel("Sequence Number:"));
        JTextField seqField = new JTextField();
        panel.add(seqField);
        panel.add(new JLabel("Work Center:"));
        JComboBox<String> wcCombo = new JComboBox<>();
        try {
            List<WorkCenter> wcs = BOMService.getInstance().getAllWorkCenters();
            if (wcs != null) {
                for (WorkCenter wc : wcs) {
                    wcCombo.addItem(wc.getId());
                }
            }
        } catch (Exception ignored) {}
        panel.add(wcCombo);
        
        int res = JOptionPane.showConfirmDialog(this, panel, "Create Assembly Line for Order #" + order.getId(), JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int seq = Integer.parseInt(seqField.getText().trim());
                String wcId = (String) wcCombo.getSelectedItem();
                AssemblyLine l = new AssemblyLine();
                l.setName(nameField.getText());
                l.setSequenceNum(seq);
                l.setWorkCenterId(wcId);
                l.setProductionOrderId(order.getId());
                
                BOMService.getInstance().createAssemblyLine(l);
                refreshData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void assignOrder() {
        ProductionOrder order = (ProductionOrder) orderCombo.getSelectedItem();
        if (order == null) return;
        if (order.getCurrentLineId() != -1 || "Completed".equals(order.getOrderStatus())) {
            JOptionPane.showMessageDialog(this, "Order is already assigned or completed.");
            return;
        }
        if (linesTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Create assembly lines for this order first.");
            return;
        }
        
        int firstLineId = (int) linesTableModel.getValueAt(0, 0); // sorted by seq
        
        try {
            BOMService.getInstance().assignOrderToLine(order.getId(), firstLineId);
            JOptionPane.showMessageDialog(this, "Assigned successfully!");
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void moveOrder() {
        ProductionOrder order = (ProductionOrder) orderCombo.getSelectedItem();
        if (order == null) return;
        if (order.getCurrentLineId() == -1 || "Completed".equals(order.getOrderStatus())) {
            JOptionPane.showMessageDialog(this, "Order is not currently in assembly or already completed.");
            return;
        }
        
        try {
            BOMService.getInstance().moveOrderToNextLine(order.getId(), order.getCurrentLineId());
            JOptionPane.showMessageDialog(this, "Order advanced successfully!");
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void refreshData() {
        try {
            // Store current selection
            int selectedOrderId = -1;
            if (orderCombo.getSelectedItem() != null) {
                selectedOrderId = ((ProductionOrder) orderCombo.getSelectedItem()).getId();
            }
            
            orderCombo.removeAllItems();
            
            List<ProductionOrder> orders = BOMService.getInstance().getAllProductionOrders();
            
            ProductionOrder toSelect = null;
            if (orders != null) {
                for (ProductionOrder o : orders) {
                    if ("Active".equals(o.getOrderStatus()) || "Completed".equals(o.getOrderStatus())) {
                        orderCombo.addItem(o);
                        if (o.getId() == selectedOrderId) {
                            toSelect = o;
                        }
                    }
                }
            }
            
            if (toSelect != null) {
                orderCombo.setSelectedItem(toSelect);
            }
            updateOrderDetails();
            
        } catch (Exception e) {
            // ignore
        }
    }
}
