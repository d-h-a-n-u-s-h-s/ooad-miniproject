package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class EIMSPanel extends StubTabPanel {
    public EIMSPanel() {
        super(
                "Employee Information Management",
                "Central directory for employee records, roles, and contact details.",
                new String[]{"ID", "Name", "Department", "Role", "Email", "Phone", "Status"},
                "Add Employee", "Edit", "Deactivate", "Export"
        );
    }
}
