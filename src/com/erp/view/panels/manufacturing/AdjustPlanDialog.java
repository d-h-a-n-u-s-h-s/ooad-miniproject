package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.JSONUtil;
import com.erp.util.UIHelper;
import com.erp.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog for adjusting an existing Production Plan.
 */
public class AdjustPlanDialog extends JDialog {

    private int planId;
    private int bomId;
    private JTextField qtyField;
    private JTextField startDateField;
    private boolean adjusted = false;

    public AdjustPlanDialog(Window owner, int planId, int bomId, int qty, String startDate) {
        super(owner, "Adjust Plan", ModalityType.APPLICATION_MODAL);
        this.planId = planId;
        this.bomId = bomId;
        setupUI(qty, startDate);
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI(int initialQty, String initialStart) {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(2, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Quantity:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        qtyField = new JTextField(String.valueOf(initialQty));
        form.add(qtyField);

        form.add(UIHelper.createLabel("Start Date (YYYY-MM-DD):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        startDateField = new JTextField(initialStart);
        form.add(startDateField);

        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Save Changes");
        saveBtn.addActionListener(e -> save());

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private double calculateBOMCost(JSONUtil.BOMNode node, List<BOM> allBoms) {
        double cost = node.cost;
        if (node.children != null) {
            for (JSONUtil.BOMNode child : node.children) {
                cost += calculateBOMCost(child, allBoms);
            }
        }

        // Recursive fetch for multilevel
        if (allBoms != null && cost == 0) { // If it has 0 explicit cost, it might be a sub-BOM
            for (BOM bom : allBoms) {
                if (node.name.equals(bom.getProductName())) {
                    String json = bom.getMaterialListJson();
                    if (json != null && !json.trim().isEmpty()) {
                        List<JSONUtil.BOMNode> subNodes = JSONUtil.fromJSON(json);
                        for (JSONUtil.BOMNode subNode : subNodes) {
                            cost += calculateBOMCost(subNode, allBoms);
                        }
                    }
                    break;
                }
            }
        }
        
        return cost;
    }

    private void save() {
        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be an integer.");
            return;
        }

        String startDate = startDateField.getText().trim();
        if (startDate.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Start date required.");
            return;
        }

        // Recalculate totals
        double unitCost = 0;
        try {
            List<BOM> boms = BOMService.getInstance().getAllBOMs();
            if (boms != null) {
                for (BOM b : boms) {
                    if (b.getId() == bomId) {
                        String json = b.getMaterialListJson();
                        List<JSONUtil.BOMNode> nodes = JSONUtil.fromJSON(json);
                        for (JSONUtil.BOMNode n : nodes) unitCost += calculateBOMCost(n, boms);
                        break;
                    }
                }
            }
        } catch (Exception ignored) {}

        double totalHoursPerUnit = 0;
        try {
            List<RoutingStep> steps = BOMService.getInstance().getRoutingSteps();
            if (steps != null) {
                for (RoutingStep s : steps) {
                    if (s.getRoutingId() == bomId) {
                        totalHoursPerUnit += s.getSetupTime();
                        totalHoursPerUnit += s.getRunTime();
                    }
                }
            }
        } catch (Exception ignored) {}

        double totalMaterialCost = unitCost * qty;
        double totalHours = totalHoursPerUnit * qty;
        double totalOperationalCost = totalHours * 100.0;
        double totalCost = totalMaterialCost + totalOperationalCost;

        try {
            ProductionPlan plan = new ProductionPlan();
            plan.setId(planId);
            plan.setPlannedQuantity(qty);
            plan.setStartDate(startDate);
            plan.setTotalCost(totalCost);
            plan.setTotalHours(totalHours);
            
            BOMService.getInstance().updateProductionPlan(plan);
            adjusted = true;
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAdjusted() {
        return adjusted;
    }
}
