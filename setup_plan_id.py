import mysql.connector

def setup():
    conn = mysql.connector.connect(host='localhost', user='root', password='Pixie@1982', database='erp_subsystem')
    cursor = conn.cursor()
    
    # Alter production_orders
    try: cursor.execute('ALTER TABLE production_orders ADD COLUMN plan_id INT')
    except: pass
    
    # Delete old overlapping perms
    cursor.execute("DELETE FROM permission_matrix WHERE resource_table = 'production_orders'")

    # Add permissions
    po_cols = '["production_order_id", "bom_id", "order_quantity", "order_status", "start_date", "due_date", "plan_id"]'
    
    perms = [
        (1, 'production_orders', True, True, True, True, po_cols, po_cols)
    ]
    cursor.executemany(
        'INSERT INTO permission_matrix (subsystem_id, resource_table, can_create, can_read, can_update, can_delete, readable_columns, writable_columns) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)',
        perms
    )
    
    conn.commit()
    cursor.close()
    conn.close()
    print("production_orders plan_id updated.")

if __name__ == '__main__':
    setup()
