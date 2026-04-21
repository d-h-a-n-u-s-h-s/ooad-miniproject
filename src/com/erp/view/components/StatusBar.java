package com.erp.view.components;

import com.erp.service.UIAuthenticator;
import com.erp.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 24px persistent status ribbon at the bottom of the application shell.
 * Displays authenticated user info, plant, fiscal year, and time.
 */
public class StatusBar extends JPanel {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final JLabel label = new JLabel();
    private final Timer timer;
    private final UIAuthenticator.AuthResult user;

    public StatusBar(UIAuthenticator.AuthResult user) {
        this.user = user;
        setLayout(new BorderLayout());
        setBackground(Constants.TATA_NAVY);
        setBorder(new EmptyBorder(4, 18, 4, 18));
        setPreferredSize(new Dimension(0, 24));

        label.setFont(Constants.FONT_SMALL);
        label.setForeground(Constants.TATA_WHITE);
        add(label, BorderLayout.CENTER);

        refresh();
        timer = new Timer(60_000, e -> refresh());
        timer.setRepeats(true);
        timer.start();
    }

    public void refresh() {
        String userInfo = user != null ? user.displayName + " (" + user.role + ")" : "Guest";
        String now = LocalTime.now().format(HH_MM);
        label.setText(Constants.APP_SLOGAN
                + "   ·   Logged in: " + userInfo
                + "   ·   " + Constants.PLANT_NAME
                + "   ·   " + Constants.FISCAL_YEAR
                + "   ·   " + now);
    }
}
