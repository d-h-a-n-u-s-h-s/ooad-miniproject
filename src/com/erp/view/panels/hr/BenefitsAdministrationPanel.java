package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class BenefitsAdministrationPanel extends StubTabPanel {
    public BenefitsAdministrationPanel() {
        super(
                "Benefits Administration",
                "Insurance, retirement, and perk enrollment per employee.",
                new String[]{"Employee", "Benefit", "Plan", "Start Date", "Status", "Cost"},
                "Enroll", "Change Plan", "Terminate", "Export"
        );
    }
}
