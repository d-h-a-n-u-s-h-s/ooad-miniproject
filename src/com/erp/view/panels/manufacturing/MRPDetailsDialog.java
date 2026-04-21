package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.JSONUtil;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * MRP Details Dialog for entering planning requirements.
 */
public class MRPDetailsDialog extends JDialog {

    private JComboBox<BomItem> bomCombo;
    private JTextField prodNameField;
    private JTextField prodIdField;
    private JTextArea routingArea;
    private JTextField qtyField;
    private JTextField startDateField;
    
    private boolean planReleased = false;

    static class BomItem {
        int id; String name; String json;
        BomItem(int i, String n, String j) { id=i; name=n; json=j; }
        public String toString() { return name + " [" + id + "]"; }
    }

    public MRPDetailsDialog(Window owner) {
        super(owner, "Enter MRP Details", ModalityType.APPLICATION_MODAL);
        setupUI();
        loadCombos();
        pack();
        setSize(500, 500);
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Select BOM:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        bomCombo = new JComboBox<>();
        bomCombo.addActionListener(e -> updateAutofill());
        form.add(bomCombo);

        form.add(UIHelper.createLabel("Product Name:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        prodNameField = new JTextField();
        prodNameField.setEditable(false);
        form.add(prodNameField);

        form.add(UIHelper.createLabel("BOM ID:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        prodIdField = new JTextField();
        prodIdField.setEditable(false);
        form.add(prodIdField);

        form.add(UIHelper.createLabel("Quantity:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        qtyField = new JTextField("100");
        form.add(qtyField);

        form.add(UIHelper.createLabel("Start Date (YYYY-MM-DD):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        startDateField = new JTextField("2026-05-01");
        form.add(startDateField);

        content.add(form, BorderLayout.NORTH);

        JPanel routingPanel = new JPanel(new BorderLayout());
        routingPanel.setOpaque(false);
        routingPanel.add(UIHelper.createLabel("Routing Requirements:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY), BorderLayout.NORTH);
        routingArea = new JTextArea(6, 40);
        routingArea.setEditable(false);
        routingPanel.add(new JScrollPane(routingArea), BorderLayout.CENTER);
        content.add(routingPanel, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Release Plan");
        saveBtn.addActionListener(e -> releasePlan());

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void loadCombos() {
        try {
            List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
            if (boms != null) {
                for (Map<String, Object> b : boms) {
                    int id = ((Number) b.get("bom_id")).intValue();
                    bomCombo.addItem(new BomItem(id, (String)b.get("product_name"), (String)b.get("components_json")));
                }
            }
        } catch (Exception ignored) {}
    }

    private void updateAutofill() {
        BomItem item = (BomItem) bomCombo.getSelectedItem();
        if (item != null) {
            prodNameField.setText(item.name);
            prodIdField.setText(String.valueOf(item.id));
            
            // Determine routing
            StringBuilder sb = new StringBuilder();
            try {
                List<Map<String, Object>> steps = BOMService.getInstance().getRoutingSteps();
                if (steps != null) {
                    for (Map<String, Object> s : steps) {
                        if (((Number) s.get("routing_id")).intValue() == item.id) {
                            sb.append("Seq ").append(s.get("sequence_number"))
                              .append(": ").append(s.get("operation_name"))
                              .append(" (WC: ").append(s.get("work_center_id")).append(")")
                              .append(" | Setup: ").append(s.get("setup_time"))
                              .append(" Run: ").append(s.get("run_time")).append("\n");
                        }
                    }
                }
            } catch (Exception ignored) {}
            if (sb.length() == 0) sb.append("No routing found for this BOM.");
            routingArea.setText(sb.toString());
        }
    }

    private double calculateBOMCost(JSONUtil.BOMNode node, List<Map<String, Object>> allBoms) {
        double unitCost = node.cost;
        
        // If it has 0 explicit cost, it might be a sub-BOM or a material not set up properly
        if (allBoms != null && unitCost == 0) { 
            boolean isBom = false;
            for (Map<String, Object> bom : allBoms) {
                if (node.name.equals(bom.get("product_name"))) {
                    isBom = true;
                    String json = (String) bom.get("components_json");
                    if (json == null) json = (String) bom.get("material_list");
                    if (json != null && !json.trim().isEmpty()) {
                        List<JSONUtil.BOMNode> subNodes = JSONUtil.fromJSON(json);
                        for (JSONUtil.BOMNode subNode : subNodes) {
                            unitCost += calculateBOMCost(subNode, allBoms);
                        }
                    }
                    break;
                }
            }
            // If it wasn't a BOM, check materials list
            if (!isBom) {
                try {
                    List<Map<String, Object>> materials = BOMService.getInstance().getAllMaterials();
                    if (materials != null) {
                        for (Map<String, Object> mat : materials) {
                            if (node.name.equals(mat.get("item_name")) || node.name.equals(mat.get("product_name"))) {
                                Number uc = (Number) mat.get("unit_cost");
                                if (uc != null) unitCost = uc.doubleValue();
                                break;
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        
        // Base cost
        double totalCost = unitCost * node.qty;
        
        if (node.children != null) {
            for (JSONUtil.BOMNode child : node.children) {
                // If it has children defined explicitly, add them (and scale by node.qty if needed)
                totalCost += calculateBOMCost(child, allBoms) * node.qty;
            }
        }
        
        return totalCost;
    }

    private void releasePlan() {
        BomItem item = (BomItem) bomCombo.getSelectedItem();
        if (item == null) return;

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a valid integer.");
            return;
        }

        String startDate = startDateField.getText().trim();
        if (startDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Start date required.");
            return;
        }

        double unitCost = 0;
        try {
            List<Map<String, Object>> allBoms = BOMService.getInstance().getAllBOMs();
            List<JSONUtil.BOMNode> nodes = JSONUtil.fromJSON(item.json);
            for (JSONUtil.BOMNode n : nodes) unitCost += calculateBOMCost(n, allBoms);
        } catch (Exception ignored) {}

        double totalHoursPerUnit = 0;
        try {
            List<Map<String, Object>> steps = BOMService.getInstance().getRoutingSteps();
            if (steps != null) {
                for (Map<String, Object> s : steps) {
                    if (((Number) s.get("routing_id")).intValue() == item.id) {
                        totalHoursPerUnit += ((Number) s.get("setup_time")).doubleValue();
                        totalHoursPerUnit += ((Number) s.get("run_time")).doubleValue();
                    }
                }
            }
        } catch (Exception ignored) {}

        double totalMaterialCost = unitCost * qty;
        double totalHours = totalHoursPerUnit * qty;
        double totalOperationalCost = totalHours * 100.0;
        double totalCost = totalMaterialCost + totalOperationalCost;

        try {
            BOMService.getInstance().createProductionPlan(item.id, qty, startDate, totalCost, totalHours);
            planReleased = true;
            
            // Show report pane
            String report = String.format(
                "Plan Released Successfully!\n\n" +
                "Product: %s\n" +
                "Quantity: %d\n" +
                "Start Date: %s\n\n" +
                "=== Cost Estimation Breakdown ===\n" +
                "Total Material Cost (Multilevel BOM): ₹%.2f\n" +
                "Total Routing Hours Required: %.2f hrs\n" +
                "Operational Cost (₹100 / hr): ₹%.2f\n" +
                "-----------------------------------\n" +
                "Total Estimated Cost: ₹%.2f\n\n" +
                "Capacity Requirement Planning: Passed", 
                item.name, qty, startDate, totalMaterialCost, totalHours, totalOperationalCost, totalCost);
            JOptionPane.showMessageDialog(this, report, "MRP Report", JOptionPane.INFORMATION_MESSAGE);
            
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to create plan: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isPlanReleased() {
        return planReleased;
    }
}
