package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;
import com.erp.model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialog for manually creating a Production Order.
 */
public class AddProductionOrderDialog extends JDialog {

    private JComboBox<BOM> bomCombo;
    private JTextField qtyField;
    private JTextField startDateField;
    private JTextField endDateField;
    
    private boolean added = false;

    public AddProductionOrderDialog(Window owner) {
        super(owner, "Create Production Order", ModalityType.APPLICATION_MODAL);
        setupUI();
        loadCombos();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(4, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Select BOM:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        bomCombo = new JComboBox<>();
        form.add(bomCombo);

        form.add(UIHelper.createLabel("Quantity Planned:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        qtyField = new JTextField("10");
        form.add(qtyField);

        form.add(UIHelper.createLabel("Start Date (YYYY-MM-DD):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        startDateField = new JTextField("2026-05-01");
        form.add(startDateField);

        form.add(UIHelper.createLabel("End Date (YYYY-MM-DD):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        endDateField = new JTextField("2026-05-15");
        form.add(endDateField);

        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Save");
        saveBtn.addActionListener(e -> save());

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> dispose());

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void loadCombos() {
        try {
            List<BOM> boms = BOMService.getInstance().getAllBOMs();
            if (boms != null) {
                for (BOM b : boms) {
                    bomCombo.addItem(b);
                }
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        BOM bom = (BOM) bomCombo.getSelectedItem();
        if (bom == null) return;

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be an integer.");
            return;
        }

        String start = startDateField.getText().trim();
        String end = endDateField.getText().trim();

        if (start.isEmpty() || end.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Dates are required.");
            return;
        }

        try {
            ProductionOrder order = new ProductionOrder();
            order.setBomId(bom.getId());
            order.setOrderQuantity(qty);
            order.setStartDate(start);
            order.setDueDate(end);
            order.setPlanId(-1);
            order.setOrderStatus("Active");
            
            BOMService.getInstance().createProductionOrder(order);
            added = true;
            dispose();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAdded() {
        return added;
    }
}
