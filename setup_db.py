import mysql.connector

def setup():
    conn = mysql.connector.connect(
        host="localhost",
        user="root",
        password="Pixie@1982",
        database="erp_subsystem"
    )
    cursor = conn.cursor()

    # Clear old permissions
    cursor.execute("DELETE FROM permission_matrix")
    
    # Update Permissions with explicit JSON arrays of column names
    c_cols = '["component_id", "item_id", "component_code", "specification", "created_at", "updated_at"]'
    b_cols = '["bom_id", "product_name", "material_list", "bom_version", "is_active"]'
    i_cols = '["item_id", "item_name", "category", "uom", "stock_qty", "reorder_level"]'
    m_cols = '["product_id", "product_name", "category", "uom", "stock_qty", "reorder_level"]'

    perms = [
        (1, "components", True, True, True, True, c_cols, c_cols),
        (1, "bills_of_materials", True, True, True, True, b_cols, b_cols),
        (1, "inventory_items", True, True, True, True, i_cols, i_cols),
        (1, "materials", True, True, True, True, m_cols, m_cols)
    ]
    cursor.executemany(
        "INSERT INTO permission_matrix (subsystem_id, resource_table, can_create, can_read, can_update, can_delete, readable_columns, writable_columns) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
        perms
    )

    # Seed initial data
    cursor.execute("DELETE FROM bills_of_materials")
    cursor.execute("DELETE FROM components")
    cursor.execute("DELETE FROM inventory_items")

    # Insert items
    items = [
        (1, "High-Carbon Steel Sheet", "Raw Material", "sqm", 500.0, 100.0),
        (2, "V8 Engine Block", "Sub-Assembly", "pcs", 50.0, 10.0),
        (3, "Rubber Tire", "Component", "pcs", 200.0, 40.0)
    ]
    cursor.executemany("INSERT INTO inventory_items (item_id, item_name, category, uom, stock_qty, reorder_level) VALUES (%s, %s, %s, %s, %s, %s)", items)

    # Insert components
    comps = [
        (1, 1, "CHASSIS-ST-01", "Laser-cut 5x5 steel chassis base"),
        (2, 2, "ENG-V8-M1", "Fully assembled V8 Engine Block"),
        (3, 3, "WHL-RBB-01", "Standard 18-inch rubber tire")
    ]
    cursor.executemany("INSERT INTO components (component_id, item_id, component_code, specification) VALUES (%s, %s, %s, %s)", comps)

    # Insert BOMs
    bom_json = '[{"name":"High-Carbon Steel Sheet","qty":4.0,"uom":"sqm","children":[]},{"name":"V8 Engine Block","qty":1.0,"uom":"pcs","children":[]},{"name":"Rubber Tire","qty":4.0,"uom":"pcs","children":[]}]'
    boms = [
        ("Sports Car Model S", bom_json, "v1.0", True)
    ]
    cursor.executemany("INSERT INTO bills_of_materials (product_name, material_list, bom_version, is_active) VALUES (%s, %s, %s, %s)", boms)

    conn.commit()
    cursor.close()
    conn.close()
    print("Seeded Database and Permissions successfully.")

if __name__ == "__main__":
    setup()
