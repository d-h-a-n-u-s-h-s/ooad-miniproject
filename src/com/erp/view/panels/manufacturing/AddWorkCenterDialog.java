package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialog for creating a new Work Center.
 * Principles & Patterns Used:
 * 1. Controller (GRASP): Relays input data to the Service layer.
 * 2. Single Responsibility Principle (SOLID).
 */
public class AddWorkCenterDialog extends JDialog {

    private JTextField idField;
    private JTextField nameField;
    private JComboBox<String> typeCombo;
    private JTextField capacityField;
    private JTextField locationField;
    
    private boolean added = false;

    public AddWorkCenterDialog(Window owner) {
        super(owner, "Add Work Center", ModalityType.APPLICATION_MODAL);
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

        form.add(UIHelper.createLabel("Work Center ID:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        idField = new JTextField();
        form.add(idField);

        form.add(UIHelper.createLabel("Center Name:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        nameField = new JTextField();
        form.add(nameField);

        form.add(UIHelper.createLabel("Operation Type:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        typeCombo = new JComboBox<>(new String[]{"Assembly", "Welding", "Paint", "Testing", "Packaging"});
        form.add(typeCombo);

        form.add(UIHelper.createLabel("Capacity Hours:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        capacityField = new JTextField("40.0");
        form.add(capacityField);
        
        form.add(UIHelper.createLabel("Location:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        locationField = new JTextField();
        form.add(locationField);

        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Save Work Center");
        saveBtn.addActionListener(e -> {
            System.out.println("Save button clicked!");
            save();
        });

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> {
            System.out.println("Cancel button clicked!");
            dispose();
        });

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void save() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String type = (String) typeCombo.getSelectedItem();
        String location = locationField.getText().trim();
        
        if (id.isEmpty() || name.isEmpty() || location.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double capacity = 0;
        try {
            capacity = Double.parseDouble(capacityField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Capacity must be a number.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            BOMService.getInstance().addWorkCenter(id, name, type, capacity, location);
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
