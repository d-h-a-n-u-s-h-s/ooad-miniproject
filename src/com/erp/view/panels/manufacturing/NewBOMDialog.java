package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.JSONUtil;
import com.erp.util.JSONUtil.BOMNode;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Dialog for creating or revising a Bill of Materials.
 * 
 * Principles & Patterns Used:
 * 1. Single Responsibility Principle (SOLID): Responsible solely for capturing user input for a new/revised BOM.
 * 2. Composite Pattern (Conceptual): The JTree visually manages the BOMNode hierarchical composite structure.
 */
public class NewBOMDialog extends JDialog {

    private JComboBox<String> productCombo;
    private JTextField versionField;
    private JTree bomTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private List<Map<String, Object>> materialsList;
    
    private boolean saved = false;
    private int reviseBomId = -1; // -1 means new
    
    public NewBOMDialog(Window owner) {
        this(owner, null, null, null, -1);
    }
    
    public NewBOMDialog(Window owner, String existingProduct, String existingVersion, List<BOMNode> existingNodes, int bomId) {
        super(owner, bomId == -1 ? "New BOM" : "Revise BOM", ModalityType.APPLICATION_MODAL);
        this.reviseBomId = bomId;
        
        setupUI(existingProduct, existingVersion, existingNodes);
        setSize(600, 500);
        setLocationRelativeTo(owner);
    }
    
    private void setupUI(String existingProduct, String existingVersion, List<BOMNode> existingNodes) {
        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setBorder(new EmptyBorder(15, 15, 15, 15));
        content.setBackground(Constants.BG_LIGHT);
        
        // Header
        JPanel header = new JPanel(new GridLayout(2, 2, 10, 10));
        header.setOpaque(false);
        
        header.add(UIHelper.createLabel("Product Name:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        productCombo = new JComboBox<>();
        productCombo.setEditable(true); // Allow custom entry
        loadProducts();
        if (existingProduct != null) productCombo.setSelectedItem(existingProduct);
        if (reviseBomId != -1) productCombo.setEnabled(false); // Locked for existing
        header.add(productCombo);
        
        header.add(UIHelper.createLabel("BOM Version:", Constants.FONT_REGULAR, Constants.TEXT_PRIMARY));
        versionField = new JTextField();
        if (existingVersion != null) versionField.setText(existingVersion);
        header.add(versionField);
        
        content.add(header, BorderLayout.NORTH);
        
        // Tree
        rootNode = new DefaultMutableTreeNode("Root");
        if (existingNodes != null) {
            for (BOMNode n : existingNodes) {
                rootNode.add(buildTreeNode(n));
            }
        }
        treeModel = new DefaultTreeModel(rootNode);
        bomTree = new JTree(treeModel);
        bomTree.setRootVisible(false);
        
        JScrollPane scroll = new JScrollPane(bomTree);
        scroll.setBorder(BorderFactory.createTitledBorder("BOM Structure"));
        content.add(scroll, BorderLayout.CENTER);
        
        // Buttons
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setOpaque(false);
        
        JPanel treeButtons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        treeButtons.setOpaque(false);
        JButton addBtn = UIHelper.createSecondaryButton("Add Child Component");
        addBtn.addActionListener(e -> addChild());
        JButton removeBtn = UIHelper.createSecondaryButton("Remove Selected");
        removeBtn.addActionListener(e -> removeSelected());
        treeButtons.add(addBtn);
        treeButtons.add(removeBtn);
        
        JPanel actionButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionButtons.setOpaque(false);
        JButton saveBtn = UIHelper.createPrimaryButton("Save BOM");
        saveBtn.addActionListener(e -> saveBOM());
        JButton cancelBtn = UIHelper.createSecondaryButton("Cancel");
        cancelBtn.addActionListener(e -> setVisible(false));
        actionButtons.add(cancelBtn);
        actionButtons.add(saveBtn);
        
        buttonPanel.add(treeButtons, BorderLayout.WEST);
        buttonPanel.add(actionButtons, BorderLayout.EAST);
        
        content.add(buttonPanel, BorderLayout.SOUTH);
        setContentPane(content);
    }
    
    private void loadProducts() {
        try {
            materialsList = BOMService.getInstance().getAllMaterials();
            for (Map<String, Object> m : materialsList) {
                productCombo.addItem((String) m.get("product_name"));
            }
        } catch (Exception ignored) {}
    }
    
    private DefaultMutableTreeNode buildTreeNode(BOMNode node) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
        for (BOMNode child : node.children) {
            treeNode.add(buildTreeNode(child));
        }
        return treeNode;
    }
    
    private void addChild() {
        TreePath path = bomTree.getSelectionPath();
        DefaultMutableTreeNode parent = rootNode;
        if (path != null) {
            parent = (DefaultMutableTreeNode) path.getLastPathComponent();
        }
        
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        panel.add(new JLabel("Component Name:"));
        JComboBox<String> compCombo = new JComboBox<>();
        compCombo.setEditable(true);
        for (int i=0; i<productCombo.getItemCount(); i++) compCombo.addItem(productCombo.getItemAt(i));
        panel.add(compCombo);
        
        panel.add(new JLabel("Quantity:"));
        JTextField qtyField = new JTextField("1.0");
        panel.add(qtyField);
        
        panel.add(new JLabel("Unit of Measure (UoM):"));
        JTextField uomField = new JTextField("pcs");
        panel.add(uomField);

        panel.add(new JLabel("Total Cost:"));
        JTextField costField = new JTextField("1000.00");
        panel.add(costField);
        
        compCombo.addActionListener(e -> {
            String sel = (String) compCombo.getSelectedItem();
            if (materialsList != null && sel != null) {
                for (Map<String, Object> m : materialsList) {
                    if (sel.equals(m.get("product_name"))) {
                        Number cost = (Number) m.get("unit_cost");
                        if (cost != null) costField.setText(cost.toString());
                        break;
                    }
                }
            }
        });

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Component", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            String name = (String) compCombo.getSelectedItem();
            String uom = uomField.getText().trim();
            double qty = 1.0;
            double cost = 1000.0;
            try { 
                qty = Double.parseDouble(qtyField.getText().trim()); 
                if (qty <= 0) {
                    throw new com.erp.exceptions.InvalidQuantityInputException("Quantity entered must be a positive non-zero number. Value '" + qtyField.getText().trim() + "' is invalid.");
                }
            } catch (com.erp.exceptions.InvalidQuantityInputException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                return;
            } catch (Exception ex) { 
                JOptionPane.showMessageDialog(this, "Quantity must be a valid number.", "Error", JOptionPane.ERROR_MESSAGE);
                return; 
            }
            
            try { cost = Double.parseDouble(costField.getText().trim()); } catch (Exception ignored) {}
            
            try {
                String rootProdName = (String) productCombo.getSelectedItem();
                if (name != null && name.equals(rootProdName)) {
                    throw new com.erp.exceptions.InvalidBomStructureException("Bill of Materials contains circular reference (Product cannot contain itself)");
                }
                
                javax.swing.tree.TreeNode[] ancestors = parent.getPath();
                for (javax.swing.tree.TreeNode anc : ancestors) {
                    javax.swing.tree.DefaultMutableTreeNode aNode = (javax.swing.tree.DefaultMutableTreeNode) anc;
                    if (aNode.getUserObject() instanceof BOMNode) {
                        BOMNode bn = (BOMNode) aNode.getUserObject();
                        if (name.equals(bn.name)) {
                            throw new com.erp.exceptions.InvalidBomStructureException("Bill of Materials contains circular reference (Node '" + name + "' is already an ancestor)");
                        }
                    }
                }
            } catch (com.erp.exceptions.InvalidBomStructureException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Structure Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            if (name != null && !name.isEmpty()) {
                BOMNode newNode = new BOMNode(name, qty, uom, cost);
                DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(newNode);
                treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
                bomTree.expandPath(new TreePath(parent.getPath()));
            }
        }
    }
    
    private void removeSelected() {
        TreePath path = bomTree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node != rootNode) {
                treeModel.removeNodeFromParent(node);
            }
        }
    }
    
    private void saveBOM() {
        String prod = (String) productCombo.getSelectedItem();
        String ver = versionField.getText().trim();
        
        if (prod == null || prod.isEmpty() || ver.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Product and Version are required.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (rootNode.getChildCount() == 0) {
            JOptionPane.showMessageDialog(this, "BOM must have at least one component.", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        List<BOMNode> nodes = extractNodes(rootNode);
        String json = JSONUtil.toJSON(nodes);
        
        try {
            if (reviseBomId != -1) {
                // If version changed, save as new. Otherwise update.
                // It's usually better to check existence.
                if (BOMService.getInstance().bomExists(prod, ver)) {
                    // Update existing
                    BOMService.getInstance().updateBOM(reviseBomId, json);
                } else {
                    // Save as new version
                    BOMService.getInstance().createBOM(prod, ver, json);
                }
            } else {
                BOMService.getInstance().createBOM(prod, ver, json);
            }
            saved = true;
            setVisible(false);
        } catch (com.erp.exceptions.DuplicateBomVersionException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Duplicate BOM", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error saving BOM: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private List<BOMNode> extractNodes(DefaultMutableTreeNode treeNode) {
        List<BOMNode> list = new ArrayList<>();
        for (int i = 0; i < treeNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) treeNode.getChildAt(i);
            BOMNode bomNode = (BOMNode) child.getUserObject();
            bomNode.children = extractNodes(child);
            list.add(bomNode);
        }
        return list;
    }
    
    public boolean isSaved() {
        return saved;
    }
}
