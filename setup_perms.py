import mysql.connector

def setup():
    conn = mysql.connector.connect(host='localhost', user='root', password='Pixie@1982', database='erp_subsystem')
    cursor = conn.cursor()
    
    # Create tables
    ddl = [
        '''
        CREATE TABLE IF NOT EXISTS work_centers (
            work_center_id VARCHAR(50) PRIMARY KEY,
            work_center_name VARCHAR(100),
            work_center_type VARCHAR(50),
            capacity_hours DECIMAL(10,2),
            utilization_pct DECIMAL(5,2),
            location VARCHAR(100),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )
        ''',
        '''
        CREATE TABLE IF NOT EXISTS routings (
            routing_id INT AUTO_INCREMENT PRIMARY KEY,
            bom_id INT,
            work_center_id VARCHAR(50),
            assigned_operator_id INT,
            setup_time DECIMAL(10,2),
            run_time DECIMAL(10,2)
        )
        ''',
        '''
        CREATE TABLE IF NOT EXISTS routing_steps (
            routing_step_id INT AUTO_INCREMENT PRIMARY KEY,
            routing_id INT,
            operation_id VARCHAR(50),
            sequence_number INT,
            operation_name VARCHAR(100),
            work_center_id VARCHAR(50),
            setup_time DECIMAL(10,2),
            run_time DECIMAL(10,2),
            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
        )
        '''
    ]
    for q in ddl: cursor.execute(q)
    
    # Delete old overlapping perms just in case
    cursor.execute("DELETE FROM permission_matrix WHERE resource_table IN ('work_centers', 'routings', 'routing_steps')")

    # Add permissions
    w_cols = '["work_center_id", "work_center_name", "work_center_type", "capacity_hours", "utilization_pct", "location"]'
    r_cols = '["routing_id", "bom_id", "work_center_id", "assigned_operator_id", "setup_time", "run_time"]'
    rs_cols = '["routing_step_id", "routing_id", "operation_id", "sequence_number", "operation_name", "work_center_id", "setup_time", "run_time"]'
    
    perms = [
        (1, 'work_centers', True, True, True, True, w_cols, w_cols),
        (1, 'routings', True, True, True, True, r_cols, r_cols),
        (1, 'routing_steps', True, True, True, True, rs_cols, rs_cols)
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
