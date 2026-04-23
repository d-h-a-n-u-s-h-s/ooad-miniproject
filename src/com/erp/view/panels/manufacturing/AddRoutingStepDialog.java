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
 * Dialog for creating a new Routing Step.
 * Principles & Patterns Used:
 * 1. Controller (GRASP): Relays input data to the Service layer.
 * 2. Information Expert (GRASP): Validates sequence numbering based on current data.
 */
public class AddRoutingStepDialog extends JDialog {

    private JComboBox<BOM> bomCombo;
    private JTextField opIdField;
    private JTextField seqField;
    private JTextField opNameField;
    private JComboBox<WorkCenter> wcCombo;
    private JTextField setupField;
    private JTextField runField;
    
    private boolean added = false;

    public AddRoutingStepDialog(Window owner) {
        super(owner, "Add Routing Step", ModalityType.APPLICATION_MODAL);
        setupUI();
        pack();
        setLocationRelativeTo(owner);
    }

    private void setupUI() {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(20, 20, 20, 20));
        content.setBackground(Constants.BG_LIGHT);

        JPanel form = new JPanel(new GridLayout(7, 2, 10, 15));
        form.setOpaque(false);

        form.add(UIHelper.createLabel("Select BOM:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        bomCombo = new JComboBox<>();
        form.add(bomCombo);

        form.add(UIHelper.createLabel("Operation ID:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        opIdField = new JTextField();
        form.add(opIdField);

        form.add(UIHelper.createLabel("Sequence Number:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        seqField = new JTextField("1");
        form.add(seqField);

        form.add(UIHelper.createLabel("Operation Name:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        opNameField = new JTextField();
        form.add(opNameField);

        form.add(UIHelper.createLabel("Work Center:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        wcCombo = new JComboBox<>();
        wcCombo.addActionListener(e -> {
            WorkCenter wc = (WorkCenter) wcCombo.getSelectedItem();
            if (wc != null && wc.getType() != null) {
                opNameField.setText(wc.getType());
            }
        });
        form.add(wcCombo);

        form.add(UIHelper.createLabel("Setup Time (hrs):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        setupField = new JTextField("0.5");
        form.add(setupField);
        
        form.add(UIHelper.createLabel("Run Time (hrs):", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        runField = new JTextField("1.0");
        form.add(runField);

        content.add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.setOpaque(false);

        JButton saveBtn = UIHelper.createPrimaryButton("Save Step");
        saveBtn.addActionListener(e -> {
            System.out.println("Save Step clicked!");
            save();
        });

        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> {
            System.out.println("Cancel Step clicked!");
            dispose();
        });

        buttons.add(cancelBtn);
        buttons.add(saveBtn);

        content.add(buttons, BorderLayout.SOUTH);
        setContentPane(content);
        
        loadCombos();
    }

    private void loadCombos() {
        try {
            List<BOM> boms = BOMService.getInstance().getAllBOMs();
            if (boms != null) {
                for (BOM b : boms) {
                    bomCombo.addItem(b);
                }
            }
            List<WorkCenter> wcs = BOMService.getInstance().getAllWorkCenters();
            if (wcs != null) {
                for (WorkCenter w : wcs) {
                    wcCombo.addItem(w);
                }
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        BOM bom = (BOM) bomCombo.getSelectedItem();
        WorkCenter wc = (WorkCenter) wcCombo.getSelectedItem();
        String opId = opIdField.getText().trim();
        String opName = opNameField.getText().trim();
        
        if (bom == null || wc == null || opId.isEmpty() || opName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int seq;
        double setup, run;
        try {
            seq = Integer.parseInt(seqField.getText().trim());
            setup = Double.parseDouble(setupField.getText().trim());
            run = Double.parseDouble(runField.getText().trim());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Sequence, Setup Time, and Run Time must be valid numbers.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            RoutingStep step = new RoutingStep();
            step.setRoutingId(bom.getId());
            step.setOperationId(opId);
            step.setSequenceNumber(seq);
            step.setOperationName(opName);
            step.setWorkCenterId(wc.getId());
            step.setSetupTime(setup);
            step.setRunTime(run);
            
            BOMService.getInstance().addRoutingStep(step);
            added = true;
            dispose();
        } catch (com.erp.exceptions.RoutingStepSequenceGapException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Sequence Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isAdded() {
        return added;
    }
}
