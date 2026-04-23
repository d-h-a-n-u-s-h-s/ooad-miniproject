package com.erp.model;

/**
 * Represents a Production Order on the shop floor.
 */
public class ProductionOrder {
    private int id;
    private int bomId;
    private int orderQuantity;
    private String orderStatus;
    private String startDate;
    private String dueDate;
    private int planId;
    private int producedQuantity;
    private int defects;
    private String qcStatus;
    private int currentLineId;

    public ProductionOrder() {}

    public ProductionOrder(int id, int bomId, String orderStatus, int currentLineId) {
        this.id = id;
        this.bomId = bomId;
        this.orderStatus = orderStatus;
        this.currentLineId = currentLineId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBomId() { return bomId; }
    public void setBomId(int bomId) { this.bomId = bomId; }

    public int getOrderQuantity() { return orderQuantity; }
    public void setOrderQuantity(int orderQuantity) { this.orderQuantity = orderQuantity; }

    public String getOrderStatus() { return orderStatus; }
    public void setOrderStatus(String orderStatus) { this.orderStatus = orderStatus; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public String getDueDate() { return dueDate; }
    public void setDueDate(String dueDate) { this.dueDate = dueDate; }

    public int getPlanId() { return planId; }
    public void setPlanId(int planId) { this.planId = planId; }

    public int getProducedQuantity() { return producedQuantity; }
    public void setProducedQuantity(int producedQuantity) { this.producedQuantity = producedQuantity; }

    public int getDefects() { return defects; }
    public void setDefects(int defects) { this.defects = defects; }

    public String getQcStatus() { return qcStatus; }
    public void setQcStatus(String qcStatus) { this.qcStatus = qcStatus; }

    public int getCurrentLineId() { return currentLineId; }
    public void setCurrentLineId(int currentLineId) { this.currentLineId = currentLineId; }

    @Override
    public String toString() {
        return "Order #" + id;
    }
}
