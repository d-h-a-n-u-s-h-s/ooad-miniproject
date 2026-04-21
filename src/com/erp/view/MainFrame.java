package com.erp.view;

import com.erp.service.UIAuthenticator;
import com.erp.util.Constants;
import com.erp.util.UIHelper;
import com.erp.view.components.Sidebar;
import com.erp.view.components.StatusBar;
import com.erp.view.panels.BasePanel;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;

/**
 * Primary application shell. Displays authenticated user info in header.
 */
public class MainFrame extends JFrame {

    private Sidebar sidebar;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private final Map<String, BasePanel> panelCache = new HashMap<>();
    private JLabel currentModuleLabel;
    private StatusBar statusBar;
    private UIAuthenticator.AuthResult authenticatedUser;

    public MainFrame(UIAuthenticator.AuthResult user) {
        this.authenticatedUser = user;
        setupFrame();
        initializeComponents();
        setupNavigation();
        showPanel("dashboard");
    }

    private void setupFrame() {
        setTitle(Constants.APP_NAME);
        setSize(Constants.MAIN_SIZE);
        setMinimumSize(new Dimension(1024, 600));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        UIHelper.centerWindow(this);
    }

    private void initializeComponents() {
        JPanel mainContainer = new JPanel(new BorderLayout());

        sidebar = new Sidebar();

        JPanel headerBar = createHeaderBar();
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Constants.BG_LIGHT);

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.add(headerBar, BorderLayout.NORTH);
        rightPanel.add(contentPanel, BorderLayout.CENTER);

        mainContainer.add(sidebar, BorderLayout.WEST);
        mainContainer.add(rightPanel, BorderLayout.CENTER);

        statusBar = new StatusBar(authenticatedUser);
        mainContainer.add(statusBar, BorderLayout.SOUTH);

        setContentPane(mainContainer);
    }

    private JPanel createHeaderBar() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, Constants.TATA_BLUE,
                        getWidth(), 0, Constants.TATA_NAVY));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setPreferredSize(new Dimension(0, 56));
        header.setBorder(new EmptyBorder(0, Constants.PADDING_LARGE, 0, Constants.PADDING_LARGE));

        JLabel brand = new JLabel(Constants.APP_NAME);
        brand.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 18));
        brand.setForeground(Constants.TATA_GOLD);

        currentModuleLabel = new JLabel(Constants.MODULE_DASHBOARD);
        currentModuleLabel.setFont(new Font(Constants.FONT_FAMILY, Font.BOLD, 16));
        currentModuleLabel.setForeground(Constants.TATA_WHITE);
        currentModuleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(brand, BorderLayout.WEST);
        header.add(currentModuleLabel, BorderLayout.CENTER);
        header.add(createUserPanel(), BorderLayout.EAST);
        return header;
    }

    private JPanel createUserPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, Constants.PADDING_MEDIUM, 6));
        panel.setOpaque(false);

        String displayName = authenticatedUser != null ? authenticatedUser.displayName : "User";
        String role = authenticatedUser != null ? authenticatedUser.role : "";

        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(Constants.FONT_REGULAR);
        nameLabel.setForeground(Constants.TATA_WHITE);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(Constants.FONT_SMALL);
        logoutButton.setForeground(Constants.TATA_GOLD);
        logoutButton.setBackground(new Color(0, 0, 0, 0));
        logoutButton.setOpaque(false);
        logoutButton.setBorder(BorderFactory.createLineBorder(Constants.TATA_GOLD, 1));
        logoutButton.setFocusPainted(false);
        logoutButton.setContentAreaFilled(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        logoutButton.setPreferredSize(new Dimension(82, 30));
        logoutButton.addActionListener(e -> handleLogout());

        panel.add(nameLabel);
        panel.add(UIHelper.roleBadge(role));
        panel.add(logoutButton);
        return panel;
    }

    private void setupNavigation() {
        sidebar.setMenuActionListener(this::handleNavigation);
    }

    private void handleNavigation(ActionEvent e) { showPanel(e.getActionCommand()); }

    private void showPanel(String command) {
        if (!panelCache.containsKey(command)) {
            try {
                BasePanel panel = PanelRegistry.create(command);
                panel.ensureInitialized();
                panelCache.put(command, panel);
                contentPanel.add(panel, command);
            } catch (Exception e) {
                System.err.println("Error creating panel for: " + command);
                e.printStackTrace();
                return;
            }
        }
        BasePanel panel = panelCache.get(command);
        panel.ensureInitialized();
        sidebar.selectByCommand(command);
        currentModuleLabel.setText(panel.getPanelTitle());
        panel.refreshData();
        cardLayout.show(contentPanel, command);
        contentPanel.revalidate();
        contentPanel.repaint();
        if (statusBar != null) statusBar.refresh();
    }

    private void handleLogout() {
        boolean confirm = UIHelper.showConfirm(this, "Are you sure you want to logout?");
        if (confirm) {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
            this.dispose();
        }
    }
}
