package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class RecruitmentPanel extends StubTabPanel {
    public RecruitmentPanel() {
        super(
                "Recruitment & Applicant Tracking",
                "Track open requisitions, candidates, and hiring stages.",
                new String[]{"Requisition", "Role", "Department", "Stage", "Candidates", "Owner", "Opened"},
                "New Requisition", "Add Candidate", "Advance Stage", "Close"
        );
    }
}
