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
 * AssemblyLinesTab manages assembly lines specifically tied to active production orders.
 */
public class AssemblyLinesTab extends JPanel {

    private JComboBox<OrderItem> orderCombo;
    private JLabel statusLabel;
    private JLabel currentLineLabel;
    
    private JTable linesTable;
    private DefaultTableModel linesTableModel;
    
    static class OrderItem {
        int id; String name; String status; int currentLine;
        OrderItem(int i, String n, String s, int c) { id=i; name=n; status=s; currentLine=c; }
        public String toString() { return "Order #" + id + " - " + name; }
    }

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

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(500);
        splitPane.setOpaque(false);
        
        body.add(splitPane, BorderLayout.CENTER);

        return body;
    }
    
    private void updateOrderDetails() {
        OrderItem o = (OrderItem) orderCombo.getSelectedItem();
        linesTableModel.setRowCount(0);
        if (o == null) {
            statusLabel.setText("Status: -");
            currentLineLabel.setText("Current Line: -");
            return;
        }
        
        if ("Completed".equals(o.status)) {
            statusLabel.setText("Status: COMPLETED");
            statusLabel.setForeground(new Color(34, 139, 34)); // Dark green
        } else {
            statusLabel.setText("Status: " + o.status);
            statusLabel.setForeground(Constants.TEXT_PRIMARY);
        }
        
        try {
            List<Map<String, Object>> lines = BOMService.getInstance().getAllAssemblyLines();
            String currentLineName = "Not Assigned";
            
            if (lines != null) {
                // sort by sequence
                lines.sort((a,b) -> Integer.compare(((Number)a.get("sequence_num")).intValue(), ((Number)b.get("sequence_num")).intValue()));
                for (Map<String, Object> l : lines) {
                    Number orderIdNum = (Number) l.get("production_order_id");
                    if (orderIdNum != null && orderIdNum.intValue() == o.id) {
                        int id = ((Number) l.get("line_id")).intValue();
                        String name = (String) l.get("line_name");
                        linesTableModel.addRow(new Object[]{ id, name, l.get("sequence_num"), l.get("work_center_id") });
                        
                        if (id == o.currentLine) {
                            currentLineName = name;
                        }
                    }
                }
            }
            
            currentLineLabel.setText("Current Line: " + currentLineName);
            if (o.currentLine == -1 && "Completed".equals(o.status)) {
                currentLineLabel.setText("Current Line: Finished");
            }
        } catch (Exception e) {}
    }

    private void createLine() {
        OrderItem o = (OrderItem) orderCombo.getSelectedItem();
        if (o == null) {
            JOptionPane.showMessageDialog(this, "Select an order first.");
            return;
        }
        if ("Completed".equals(o.status)) {
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
            List<Map<String, Object>> wcs = BOMService.getInstance().getAllWorkCenters();
            if (wcs != null) {
                for (Map<String, Object> wc : wcs) {
                    wcCombo.addItem((String) wc.get("work_center_id"));
                }
            }
        } catch (Exception ignored) {}
        panel.add(wcCombo);
        
        int res = JOptionPane.showConfirmDialog(this, panel, "Create Assembly Line for Order #" + o.id, JOptionPane.OK_CANCEL_OPTION);
        if (res == JOptionPane.OK_OPTION) {
            try {
                int seq = Integer.parseInt(seqField.getText().trim());
                String wcId = (String) wcCombo.getSelectedItem();
                if (wcId == null) {
                    JOptionPane.showMessageDialog(this, "A Work Center must be selected.");
                    return;
                }
                BOMService.getInstance().createAssemblyLine(nameField.getText(), seq, wcId, o.id);
                refreshData();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            }
        }
    }

    private void assignOrder() {
        OrderItem o = (OrderItem) orderCombo.getSelectedItem();
        if (o == null) return;
        if (o.currentLine != -1 || "Completed".equals(o.status)) {
            JOptionPane.showMessageDialog(this, "Order is already assigned or completed.");
            return;
        }
        if (linesTable.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Create assembly lines for this order first.");
            return;
        }
        
        int firstLineId = (int) linesTableModel.getValueAt(0, 0); // sorted by seq
        
        try {
            BOMService.getInstance().assignOrderToLine(o.id, firstLineId);
            JOptionPane.showMessageDialog(this, "Assigned successfully!");
            refreshData();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
        }
    }

    private void moveOrder() {
        OrderItem o = (OrderItem) orderCombo.getSelectedItem();
        if (o == null) return;
        if (o.currentLine == -1 || "Completed".equals(o.status)) {
            JOptionPane.showMessageDialog(this, "Order is not currently in assembly or already completed.");
            return;
        }
        
        try {
            BOMService.getInstance().moveOrderToNextLine(o.id, o.currentLine);
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
                selectedOrderId = ((OrderItem) orderCombo.getSelectedItem()).id;
            }
            
            orderCombo.removeAllItems();
            
            List<Map<String, Object>> orders = BOMService.getInstance().getAllProductionOrders();
            List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
            java.util.Map<Integer, String> bomNames = new java.util.HashMap<>();
            if (boms != null) {
                for (Map<String, Object> b : boms) {
                    bomNames.put(((Number) b.get("bom_id")).intValue(), (String) b.get("product_name"));
                }
            }
            
            OrderItem toSelect = null;
            if (orders != null) {
                for (Map<String, Object> o : orders) {
                    String status = (String) o.get("order_status");
                    if ("Active".equals(status) || "Completed".equals(status)) {
                        int id = ((Number) o.get("production_order_id")).intValue();
                        int bomId = ((Number) o.get("bom_id")).intValue();
                        int lineId = o.get("current_line_id") != null ? ((Number) o.get("current_line_id")).intValue() : -1;
                        String name = bomNames.getOrDefault(bomId, "Unknown");
                        
                        OrderItem item = new OrderItem(id, name, status, lineId);
                        orderCombo.addItem(item);
                        
                        if (id == selectedOrderId) {
                            toSelect = item;
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
