package com.erp.model;

/**
 * Represents an Assembly Line station.
 */
public class AssemblyLine {
    private int id;
    private String name;
    private int sequenceNum;
    private String workCenterId;
    private int productionOrderId;

    public AssemblyLine() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSequenceNum() { return sequenceNum; }
    public void setSequenceNum(int sequenceNum) { this.sequenceNum = sequenceNum; }

    public String getWorkCenterId() { return workCenterId; }
    public void setWorkCenterId(String workCenterId) { this.workCenterId = workCenterId; }

    public int getProductionOrderId() { return productionOrderId; }
    public void setProductionOrderId(int productionOrderId) { this.productionOrderId = productionOrderId; }
}
