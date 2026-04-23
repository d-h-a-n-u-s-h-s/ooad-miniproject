package com.erp.model;

/**
 * Represents a movement history entry for an assembly line.
 */
public class AssemblyMovement {
    private int productionOrderId;
    private int lineId;
    private String timestamp;

    public AssemblyMovement() {}

    public int getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(int productionOrderId) { this.productionOrderId = productionOrderId; }

    public int getLineId() { return lineId; }
    public void setLineId(int lineId) { this.lineId = lineId; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
