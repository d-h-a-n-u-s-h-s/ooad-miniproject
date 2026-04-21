package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Dialog for manually creating a Production Order.
 */
public class AddProductionOrderDialog extends JDialog {

    private JComboBox<BomItem> bomCombo;
    private JTextField qtyField;
    private JTextField startDateField;
    private JTextField endDateField;
    
    private boolean added = false;

    static class BomItem {
        int id; String name;
        BomItem(int i, String n) { id=i; name=n; }
        public String toString() { return name + " [" + id + "]"; }
    }

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
            List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
            if (boms != null) {
                for (Map<String, Object> b : boms) {
                    int id = ((Number) b.get("bom_id")).intValue();
                    bomCombo.addItem(new BomItem(id, (String)b.get("product_name")));
                }
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        BomItem item = (BomItem) bomCombo.getSelectedItem();
        if (item == null) return;

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
            BOMService.getInstance().createProductionOrder(item.id, qty, start, end, -1);
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
