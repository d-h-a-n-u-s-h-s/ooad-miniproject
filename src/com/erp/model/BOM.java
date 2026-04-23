package com.erp.model;

/**
 * Represents a Bill of Materials.
 */
public class BOM {
    private int id;
    private String productName;
    private String materialListJson;
    private String version;
    private boolean isActive;

    public BOM() {}

    public BOM(int id, String productName, String version) {
        this.id = id;
        this.productName = productName;
        this.version = version;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getMaterialListJson() { return materialListJson; }
    public void setMaterialListJson(String materialListJson) { this.materialListJson = materialListJson; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return id == -1 ? productName : productName + " (" + version + ")";
    }
}
