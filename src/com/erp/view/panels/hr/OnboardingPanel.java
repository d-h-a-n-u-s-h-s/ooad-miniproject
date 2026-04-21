package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class OnboardingPanel extends StubTabPanel {
    public OnboardingPanel() {
        super(
                "Onboarding Management",
                "Checklist-driven onboarding for new joiners — docs, assets, access.",
                new String[]{"Employee", "Joining Date", "Checklist Progress", "Assigned Buddy", "Status"},
                "Start Onboarding", "Mark Complete", "Remind", "Cancel"
        );
    }
}
