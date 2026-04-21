package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class PayrollPanel extends StubTabPanel {
    public PayrollPanel() {
        super(
                "Payroll Management",
                "Monthly pay runs, deductions, reimbursements and payslip generation.",
                new String[]{"Employee", "Gross", "Deductions", "Net Pay", "Status", "Period"},
                "Run Payroll", "Download Payslip", "Adjust", "Approve"
        );
    }
}
