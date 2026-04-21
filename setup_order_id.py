import mysql.connector
def setup():
    conn = mysql.connector.connect(host='localhost', user='root', password='Pixie@1982', database='erp_subsystem')
    cursor = conn.cursor()
    try: cursor.execute('ALTER TABLE assembly_lines ADD COLUMN production_order_id INT DEFAULT NULL')
    except: pass
    cursor.execute("DELETE FROM permission_matrix WHERE resource_table = 'assembly_lines'")
    al_cols = '["line_id", "line_name", "sequence_num", "work_center_id", "production_order_id"]'
    cursor.execute('INSERT INTO permission_matrix (subsystem_id, resource_table, can_create, can_read, can_update, can_delete, readable_columns, writable_columns) VALUES (1, "assembly_lines", True, True, True, True, %s, %s)', (al_cols, al_cols))
    conn.commit()
    cursor.close()
    conn.close()
setup()
