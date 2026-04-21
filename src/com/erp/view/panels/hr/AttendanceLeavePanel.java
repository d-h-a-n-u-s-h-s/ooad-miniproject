package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class AttendanceLeavePanel extends StubTabPanel {
    public AttendanceLeavePanel() {
        super(
                "Attendance & Leave Management",
                "Daily attendance, leave balances, and time-off requests.",
                new String[]{"Employee", "Date", "Check-In", "Check-Out", "Hours", "Leave Type", "Status"},
                "Mark Attendance", "Apply Leave", "Approve", "Reject"
        );
    }
}
