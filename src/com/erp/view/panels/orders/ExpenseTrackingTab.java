package com.erp.view.panels.orders;

import com.erp.view.panels.StubTabPanel;

public class ExpenseTrackingTab extends StubTabPanel {
    public ExpenseTrackingTab() {
        super(
                "Expense Tracking",
                "Employee expense claims and approvals.",
                new String[]{"Claim #", "Employee", "Category", "Amount", "Submitted", "Status"},
                "New Claim", "Approve", "Reject", "Reimburse"
        );
    }
}
