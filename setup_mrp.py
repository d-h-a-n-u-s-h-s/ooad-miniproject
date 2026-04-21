import mysql.connector

def setup():
    conn = mysql.connector.connect(host='localhost', user='root', password='Pixie@1982', database='erp_subsystem')
    cursor = conn.cursor()
    
    # Alter inventory_items
    try: cursor.execute('ALTER TABLE inventory_items ADD COLUMN unit_cost DECIMAL(10,2) DEFAULT 1000.00')
    except: pass
    cursor.execute('UPDATE inventory_items SET unit_cost = 1000.00 WHERE unit_cost IS NULL')
    
    # Create production_plans
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS production_plans (
        plan_id INT AUTO_INCREMENT PRIMARY KEY,
        bom_id INT,
        planned_quantity INT,
        start_date DATE,
        total_cost DECIMAL(15,2),
        total_hours DECIMAL(10,2),
        status VARCHAR(20) DEFAULT 'Draft'
    )
    ''')
    
    # Create production_orders (if not exists)
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS production_orders (
        production_order_id INT AUTO_INCREMENT PRIMARY KEY,
        bom_id INT,
        order_quantity INT,
        order_status VARCHAR(30),
        start_date DATE,
        due_date DATE
    )
    ''')
    
    # Delete old overlapping perms
    cursor.execute("DELETE FROM permission_matrix WHERE resource_table IN ('production_plans', 'production_orders', 'inventory_items')")

    # Add permissions
    pp_cols = '["plan_id", "bom_id", "planned_quantity", "start_date", "total_cost", "total_hours", "status"]'
    po_cols = '["production_order_id", "bom_id", "order_quantity", "order_status", "start_date", "due_date"]'
    i_cols = '["item_id", "item_name", "category", "uom", "stock_qty", "reorder_level", "unit_cost"]'
    
    perms = [
        (1, 'production_plans', True, True, True, True, pp_cols, pp_cols),
        (1, 'production_orders', True, True, True, True, po_cols, po_cols),
        (1, 'inventory_items', True, True, True, True, i_cols, i_cols)
    ]
    cursor.executemany(
        'INSERT INTO permission_matrix (subsystem_id, resource_table, can_create, can_read, can_update, can_delete, readable_columns, writable_columns) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)',
        perms
    )
    
    conn.commit()
    cursor.close()
    conn.close()
    print("MRP tables and permissions updated.")

if __name__ == '__main__':
    setup()
