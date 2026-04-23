package com.erp.model;

/**
 * Represents an Inventory Item or Material in the ERP system.
 */
public class Material {
    private int id;
    private String name;
    private String category;
    private String uom;
    private double stockQuantity;
    private double reorderLevel;
    private double unitCost;

    public Material() {}

    public Material(int id, String name, String category, String uom, double stockQuantity, double reorderLevel) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.uom = uom;
        this.stockQuantity = stockQuantity;
        this.reorderLevel = reorderLevel;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getUom() { return uom; }
    public void setUom(String uom) { this.uom = uom; }

    public double getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(double stockQuantity) { this.stockQuantity = stockQuantity; }

    public double getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(double reorderLevel) { this.reorderLevel = reorderLevel; }

    public double getUnitCost() { return unitCost; }
    public void setUnitCost(double unitCost) { this.unitCost = unitCost; }

    @Override
    public String toString() {
        return name;
    }
}
