import mysql.connector

def setup():
    conn = mysql.connector.connect(host='localhost', user='root', password='Pixie@1982', database='erp_subsystem')
    cursor = conn.cursor()
    
    # Alter production_orders
    for col in ['produced_quantity INT DEFAULT 0', 'current_line_id INT DEFAULT NULL', 'defects INT DEFAULT 0', 'qc_status VARCHAR(20) DEFAULT "Pending"']:
        try: cursor.execute(f'ALTER TABLE production_orders ADD COLUMN {col}')
        except: pass
        
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS shop_floor_logs (
        log_id INT AUTO_INCREMENT PRIMARY KEY,
        production_order_id INT,
        quantity_logged INT,
        log_timestamp DATETIME
    )''')
    
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS assembly_lines (
        line_id INT AUTO_INCREMENT PRIMARY KEY,
        line_name VARCHAR(100),
        sequence_num INT,
        work_center_id VARCHAR(50)
    )''')
    
    cursor.execute('''
    CREATE TABLE IF NOT EXISTS assembly_movements (
        movement_id INT AUTO_INCREMENT PRIMARY KEY,
        production_order_id INT,
        line_id INT,
        movement_timestamp DATETIME
    )''')
    
    cursor.execute("DELETE FROM permission_matrix WHERE resource_table IN ('production_orders', 'shop_floor_logs', 'assembly_lines', 'assembly_movements')")

    po_cols = '["production_order_id", "bom_id", "order_quantity", "order_status", "start_date", "due_date", "plan_id", "produced_quantity", "current_line_id", "defects", "qc_status"]'
    sfl_cols = '["log_id", "production_order_id", "quantity_logged", "log_timestamp"]'
    al_cols = '["line_id", "line_name", "sequence_num", "work_center_id"]'
    am_cols = '["movement_id", "production_order_id", "line_id", "movement_timestamp"]'
    
    perms = [
        (1, 'production_orders', True, True, True, True, po_cols, po_cols),
        (1, 'shop_floor_logs', True, True, True, True, sfl_cols, sfl_cols),
        (1, 'assembly_lines', True, True, True, True, al_cols, al_cols),
        (1, 'assembly_movements', True, True, True, True, am_cols, am_cols)
    ]
    cursor.executemany(
        'INSERT INTO permission_matrix (subsystem_id, resource_table, can_create, can_read, can_update, can_delete, readable_columns, writable_columns) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)',
        perms
    )
    
    conn.commit()
    cursor.close()
    conn.close()

if __name__ == '__main__':
    setup()
