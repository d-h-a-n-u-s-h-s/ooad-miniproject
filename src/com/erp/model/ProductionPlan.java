package com.erp.model;

/**
 * Represents a Production Plan (MRP output).
 */
public class ProductionPlan {
    private int id;
    private int bomId;
    private int plannedQuantity;
    private String startDate;
    private double totalCost;
    private double totalHours;
    private String status;

    public ProductionPlan() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBomId() { return bomId; }
    public void setBomId(int bomId) { this.bomId = bomId; }

    public int getPlannedQuantity() { return plannedQuantity; }
    public void setPlannedQuantity(int plannedQuantity) { this.plannedQuantity = plannedQuantity; }

    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    public double getTotalCost() { return totalCost; }
    public void setTotalCost(double totalCost) { this.totalCost = totalCost; }

    public double getTotalHours() { return totalHours; }
    public void setTotalHours(double totalHours) { this.totalHours = totalHours; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
