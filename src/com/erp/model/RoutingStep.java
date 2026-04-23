package com.erp.model;

/**
 * Represents a step in a Manufacturing Routing sequence.
 */
public class RoutingStep {
    private int routingId;
    private String operationId;
    private int sequenceNumber;
    private String operationName;
    private String workCenterId;
    private double setupTime;
    private double runTime;

    public RoutingStep() {}

    public int getRoutingId() { return routingId; }
    public void setRoutingId(int routingId) { this.routingId = routingId; }

    public String getOperationId() { return operationId; }
    public void setOperationId(String operationId) { this.operationId = operationId; }

    public int getSequenceNumber() { return sequenceNumber; }
    public void setSequenceNumber(int sequenceNumber) { this.sequenceNumber = sequenceNumber; }

    public String getOperationName() { return operationName; }
    public void setOperationName(String operationName) { this.operationName = operationName; }

    public String getWorkCenterId() { return workCenterId; }
    public void setWorkCenterId(String workCenterId) { this.workCenterId = workCenterId; }

    public double getSetupTime() { return setupTime; }
    public void setSetupTime(double setupTime) { this.setupTime = setupTime; }

    public double getRunTime() { return runTime; }
    public void setRunTime(double runTime) { this.runTime = runTime; }
}
