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
 * Dialog for creating a new component.
 * 
 * Principles & Patterns Used:
 * 1. Open/Closed Principle (SOLID): Extends JDialog (open for extension) but doesn't modify base behavior.
 * 2. Controller (GRASP): ActionListeners map user gestures to system operations (BOMService calls).
 */
public class AddComponentDialog extends JDialog {

    private JComboBox<Material> materialCombo;
    private JTextField codeField;
    private JTextField specField;
    private boolean added = false;

    public AddComponentDialog(Window owner) {
        super(owner, "Add Component", ModalityType.APPLICATION_MODAL);
        setupUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(3, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Select Material:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        materialCombo = new JComboBox<>();
        loadMaterials();
        form.add(materialCombo);

        form.add(UIHelper.createLabel("Component Code:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        codeField = new JTextField();
        form.add(codeField);

        form.add(UIHelper.createLabel("Specification:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        specField = new JTextField();
        form.add(specField);

        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Save Component");
        saveBtn.addActionListener(e -> saveComponent());

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> setVisible(false));

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void loadMaterials() {
        try {
            List<Material> materials = BOMService.getInstance().getAllMaterials();
            if (materials != null) {
                for (Material m : materials) {
                    materialCombo.addItem(m);
                }
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to load materials: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveComponent() {
        Material selected = (Material) materialCombo.getSelectedItem();
        String code = codeField.getText().trim();
        String spec = specField.getText().trim();

        if (selected == null || code.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select a material and enter a code.", "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            com.erp.model.Component c = new com.erp.model.Component();
            c.setItemId(selected.getId());
            c.setComponentCode(code);
            c.setSpecification(spec);
            
            BOMService.getInstance().addComponent(c);
            added = true;
            setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to save component: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAdded() {
        return added;
    }
}
