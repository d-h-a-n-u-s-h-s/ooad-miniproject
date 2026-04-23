package com.erp.model;

/**
 * Represents a log entry for shop floor execution.
 */
public class ShopFloorLog {
    private int productionOrderId;
    private int quantityLogged;
    private String timestamp;

    public ShopFloorLog() {}

    public int getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(int productionOrderId) { this.productionOrderId = productionOrderId; }

    public int getQuantityLogged() { return quantityLogged; }
    public void setQuantityLogged(int quantityLogged) { this.quantityLogged = quantityLogged; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}
