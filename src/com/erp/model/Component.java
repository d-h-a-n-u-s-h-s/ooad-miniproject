package com.erp.model;

/**
 * Represents a Component linked to an item.
 */
public class Component {
    private int id;
    private int itemId;
    private String componentCode;
    private String specification;

    public Component() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }

    public String getComponentCode() { return componentCode; }
    public void setComponentCode(String componentCode) { this.componentCode = componentCode; }

    public String getSpecification() { return specification; }
    public void setSpecification(String specification) { this.specification = specification; }
}
