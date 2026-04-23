package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for creating a new raw material.
 * 
 * Principles & Patterns Used:
 * 1. Open/Closed Principle (SOLID): The UI layout can be extended without modifying the underlying BOMService logic.
 * 2. Controller (GRASP): ActionListeners map user gestures to system operations (BOMService calls).
 */
public class AddMaterialDialog extends JDialog {

    private JTextField nameField;
    private JTextField categoryField;
    private JTextField uomField;
    private JTextField stockField;
    private JTextField reorderField;
    
    private boolean added = false;

    public AddMaterialDialog(Window owner) {
        super(owner, "Add Raw Material", ModalityType.APPLICATION_MODAL);
        setupUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(5, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Material Name:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        nameField = new JTextField();
        form.add(nameField);

        form.add(UIHelper.createLabel("Category:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        categoryField = new JTextField();
        form.add(categoryField);

        form.add(UIHelper.createLabel("Unit of Measure (UoM):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        uomField = new JTextField();
        form.add(uomField);

        form.add(UIHelper.createLabel("Initial Stock Qty:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        stockField = new JTextField("0");
        form.add(stockField);
        
        form.add(UIHelper.createLabel("Reorder Level:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        reorderField = new JTextField("10");
        form.add(reorderField);

        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Save Material");
        saveBtn.addActionListener(e -> saveMaterial());

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> setVisible(false));

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void saveMaterial() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        String uom = uomField.getText().trim();
        
        if (name.isEmpty() || category.isEmpty() || uom.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in Name, Category, and UoM.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double stock = 0;
        double reorder = 0;
        try {
            stock = Double.parseDouble(stockField.getText().trim());
            reorder = Double.parseDouble(reorderField.getText().trim());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Stock and Reorder values must be numbers.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.erp.model.Material mat = new com.erp.model.Material();
            mat.setName(name);
            mat.setCategory(category);
            mat.setUom(uom);
            mat.setStockQuantity(stock);
            mat.setReorderLevel(reorder);
            
            BOMService.getInstance().addMaterial(mat);
            added = true;
            setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save material: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAdded() {
        return added;
    }
}
