package com.erp.view.panels.hr;

import com.erp.view.panels.StubTabPanel;

public class PerformancePanel extends StubTabPanel {
    public PerformancePanel() {
        super(
                "Performance Management",
                "Goals, reviews, and 1:1 feedback for every employee.",
                new String[]{"Employee", "Cycle", "Goals Met", "Rating", "Reviewer", "Status"},
                "Start Review", "Set Goals", "Submit Rating", "Close Cycle"
        );
    }
}
