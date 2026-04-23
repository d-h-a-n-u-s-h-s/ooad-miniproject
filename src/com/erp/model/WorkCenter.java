package com.erp.model;

/**
 * Represents a Manufacturing Work Center.
 */
public class WorkCenter {
    private String id;
    private String name;
    private String type;
    private double capacityHours;
    private double utilizationPct;
    private String location;

    public WorkCenter() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getCapacityHours() { return capacityHours; }
    public void setCapacityHours(double capacityHours) { this.capacityHours = capacityHours; }

    public double getUtilizationPct() { return utilizationPct; }
    public void setUtilizationPct(double utilizationPct) { this.utilizationPct = utilizationPct; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    @Override
    public String toString() {
        return name + " (" + id + ")";
    }
}
