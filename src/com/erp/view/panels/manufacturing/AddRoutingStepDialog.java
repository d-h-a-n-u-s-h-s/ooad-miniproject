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
 * Dialog for creating a new Routing Step.
 * Principles & Patterns Used:
 * 1. Controller (GRASP): Relays input data to the Service layer.
 * 2. Information Expert (GRASP): Validates sequence numbering based on current data.
 */
public class AddRoutingStepDialog extends JDialog {

    private JComboBox<BomItem> bomCombo;
    private JTextField opIdField;
    private JTextField seqField;
    private JTextField opNameField;
    private JComboBox<WorkCenterItem> wcCombo;
    private JTextField setupField;
    private JTextField runField;
    
    private boolean added = false;

    static class BomItem {
        int id; String name; String version;
        BomItem(int i, String n, String v) { id=i; name=n; version=v; }
        public String toString() { return name + " (" + version + ")"; }
    }

    static class WorkCenterItem {
        String id; String name; String type;
        WorkCenterItem(String i, String n, String t) { id=i; name=n; type=t; }
        public String toString() { return name + " [" + id + "]"; }
    }

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
            WorkCenterItem wc = (WorkCenterItem) wcCombo.getSelectedItem();
            if (wc != null && wc.type != null) {
                opNameField.setText(wc.type);
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
            List<Map<String, Object>> boms = BOMService.getInstance().getAllBOMs();
            for (Map<String, Object> b : boms) {
                int id = ((Number) b.get("bom_id")).intValue();
                bomCombo.addItem(new BomItem(id, (String)b.get("product_name"), (String)b.get("bom_version")));
            }
            List<Map<String, Object>> wcs = BOMService.getInstance().getAllWorkCenters();
            for (Map<String, Object> w : wcs) {
                wcCombo.addItem(new WorkCenterItem((String)w.get("work_center_id"), (String)w.get("work_center_name"), (String)w.get("work_center_type")));
            }
        } catch (Exception ignored) {}
    }

    private void save() {
        BomItem bom = (BomItem) bomCombo.getSelectedItem();
        WorkCenterItem wc = (WorkCenterItem) wcCombo.getSelectedItem();
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
            BOMService.getInstance().addRoutingStep(bom.id, opId, seq, opName, wc.id, setup, run);
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
