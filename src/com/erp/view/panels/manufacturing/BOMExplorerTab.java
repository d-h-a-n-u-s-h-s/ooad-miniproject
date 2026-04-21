package com.erp.view.panels.manufacturing;

import com.erp.service.BOMService;
import com.erp.util.Constants;
import com.erp.util.JSONUtil;
import com.erp.util.JSONUtil.BOMNode;
import com.erp.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * BOMExplorerTab represents the View in the MVC pattern.
 * 
 * Principles & Patterns Used:
 * 1. Low Coupling (GRASP): Does not contain any direct database logic, delegates to BOMService.
 * 2. Observer Pattern: Uses ListSelectionListener to observe table changes and update the JTree.
 */
public class BOMExplorerTab extends JPanel {

    private JTable bomTable;
    private DefaultTableModel tableModel;
    private JTree bomTree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;
    private List<Map<String, Object>> currentBoms;

    public BOMExplorerTab() {
        setLayout(new BorderLayout(10, 10));
        setBackground(Constants.BG_LIGHT);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(buildToolbar(), BorderLayout.NORTH);
        add(buildSplitPane(), BorderLayout.CENTER);
        
        refreshData();
    }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        toolbar.setOpaque(false);

        JButton newBomBtn = UIHelper.createPrimaryButton("New BOM");
        newBomBtn.addActionListener(e -> {
            NewBOMDialog dialog = new NewBOMDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
            if (dialog.isSaved()) refreshData();
        });

        JButton addCompBtn = UIHelper.createSecondaryButton("Add Component");
        addCompBtn.addActionListener(e -> {
            AddComponentDialog dialog = new AddComponentDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });

        JButton addMatBtn = UIHelper.createSecondaryButton("Add Material");
        addMatBtn.addActionListener(e -> {
            AddMaterialDialog dialog = new AddMaterialDialog(SwingUtilities.getWindowAncestor(this));
            dialog.setVisible(true);
        });

        JButton reviseBtn = UIHelper.createSecondaryButton("Revise");
        reviseBtn.addActionListener(e -> reviseSelectedBOM());

        JButton exportBtn = UIHelper.createSecondaryButton("Export");
        exportBtn.addActionListener(e -> exportSelectedBOM());

        toolbar.add(newBomBtn);
        toolbar.add(addMatBtn);
        toolbar.add(addCompBtn);
        toolbar.add(reviseBtn);
        toolbar.add(exportBtn);

        return toolbar;
    }

    private JSplitPane buildSplitPane() {
        // Table setup
        String[] cols = {"ID", "Product", "Version", "Active"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        bomTable = new JTable(tableModel);
        bomTable.setRowHeight(26);
        bomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        // Observer Pattern: Listens to selection changes to trigger an update in another component
        bomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) loadSelectedBOMTree();
        });
        JScrollPane tableScroll = new JScrollPane(bomTable);
        tableScroll.setBorder(BorderFactory.createTitledBorder("Bills of Materials"));

        // Tree setup
        rootNode = new DefaultMutableTreeNode("Select a BOM");
        treeModel = new DefaultTreeModel(rootNode);
        bomTree = new JTree(treeModel);
        JScrollPane treeScroll = new JScrollPane(bomTree);
        treeScroll.setBorder(BorderFactory.createTitledBorder("BOM Structure"));

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tableScroll, treeScroll);
        splitPane.setDividerLocation(400);
        splitPane.setOpaque(false);
        return splitPane;
    }

    private void refreshData() {
        try {
            currentBoms = BOMService.getInstance().getAllBOMs();
            tableModel.setRowCount(0);
            if (currentBoms != null) {
                for (Map<String, Object> bom : currentBoms) {
                    tableModel.addRow(new Object[]{
                        bom.get("bom_id"),
                        bom.get("product_name"),
                        bom.get("bom_version"),
                        bom.get("is_active")
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load BOMs: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadSelectedBOMTree() {
        int row = bomTable.getSelectedRow();
        if (row == -1) return;

        Map<String, Object> bom = currentBoms.get(row);
        String productName = (String) bom.get("product_name");
        String version = (String) bom.get("bom_version");
        String json = (String) bom.get("components_json");
        if (json == null) json = (String) bom.get("material_list");

        rootNode.removeAllChildren();
        rootNode.setUserObject(productName + " (v" + version + ")");

        List<BOMNode> nodes = JSONUtil.fromJSON(json);
        java.util.Set<String> visited = new java.util.HashSet<>();
        visited.add(productName);
        for (BOMNode n : nodes) {
            rootNode.add(buildTreeNode(n, visited));
        }
        
        treeModel.reload();
        for (int i = 0; i < bomTree.getRowCount(); i++) bomTree.expandRow(i);
    }

    private DefaultMutableTreeNode buildTreeNode(BOMNode node, java.util.Set<String> visited) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(node);
        
        java.util.Set<String> newVisited = new java.util.HashSet<>(visited);
        newVisited.add(node.name);

        for (BOMNode child : node.children) {
            treeNode.add(buildTreeNode(child, newVisited));
        }
        
        // Dynamically add children if this component has its own BOM defined
        if (currentBoms != null) {
            for (Map<String, Object> bom : currentBoms) {
                if (node.name.equals(bom.get("product_name"))) {
                    String json = (String) bom.get("components_json");
                    if (json == null) json = (String) bom.get("material_list");
                    if (json != null && !json.trim().isEmpty()) {
                        List<BOMNode> subNodes = JSONUtil.fromJSON(json);
                        for (BOMNode subNode : subNodes) {
                            if (!newVisited.contains(subNode.name)) {
                                treeNode.add(buildTreeNode(subNode, newVisited));
                            } else {
                                treeNode.add(new DefaultMutableTreeNode(subNode.name + " (Recursive)"));
                            }
                        }
                    }
                    break;
                }
            }
        }
        
        return treeNode;
    }

    private void reviseSelectedBOM() {
        int row = bomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a BOM to revise.");
            return;
        }
        Map<String, Object> bom = currentBoms.get(row);
        int bomId = ((Number) bom.get("bom_id")).intValue();
        String prod = (String) bom.get("product_name");
        String ver = (String) bom.get("bom_version");
        String json = (String) bom.get("material_list");

        List<BOMNode> nodes = JSONUtil.fromJSON(json);
        
        NewBOMDialog dialog = new NewBOMDialog(SwingUtilities.getWindowAncestor(this), prod, ver, nodes, bomId);
        dialog.setVisible(true);
        if (dialog.isSaved()) refreshData();
    }

    private void exportSelectedBOM() {
        int row = bomTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a BOM to export.");
            return;
        }
        
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export BOM");
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try (FileWriter fw = new FileWriter(file)) {
                writeTreeText(fw, rootNode, 0);
                JOptionPane.showMessageDialog(this, "Export successful!");
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void writeTreeText(FileWriter fw, DefaultMutableTreeNode node, int depth) throws IOException {
        for (int i = 0; i < depth; i++) fw.write("  ");
        fw.write("- " + node.getUserObject().toString() + "\n");
        for (int i = 0; i < node.getChildCount(); i++) {
            writeTreeText(fw, (DefaultMutableTreeNode) node.getChildAt(i), depth + 1);
        }
    }
}
