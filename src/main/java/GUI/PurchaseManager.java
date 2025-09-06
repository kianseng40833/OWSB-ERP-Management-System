package GUI;

import java.awt.*;
import java.awt.event.MouseListener;
import java.util.*;
import java.util.List;
import java.util.Timer;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import GUI.*;
import GUI.Component.*;
import Management.Model.LoggedInUser;
import Management.Model.User;

public class PurchaseManager extends JFrame {

    static ColorUIResource activeColor = new ColorUIResource(255, 255, 255);
    static ColorUIResource inactiveColor = new ColorUIResource(160, 160, 160);
    private JLabel notificationLabel;
    private Timer notificationTimer;
    private boolean hasNewPR = false;

    public PurchaseManager() {
        setTitle("Purchase Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new ColorUIResource(0, 33, 71));
        setResizable(false);

        // Top Panel
        JPanel tp = new JPanel();
        tp.setBackground(ColorUIResource.lightGray);
        tp.setBounds(0, 0, 900, 50);
        tp.setLayout(null);

        // Sidebar Panel
        JPanel sidepanel = new JPanel();
        sidepanel.setBackground(new ColorUIResource(14, 2, 41));
        sidepanel.setBounds(0, 50, 160, 650);
        sidepanel.setLayout(new BoxLayout(sidepanel, BoxLayout.Y_AXIS));

        // Content Panel
        JPanel contentPanel = new JPanel(null);
        contentPanel.setBounds(200, 100, 650, 550);
        contentPanel.setBackground(new ColorUIResource(0, 33, 71));

        // Notification Panel (added at the top)
        JPanel notificationPanel = new JPanel();
        notificationPanel.setBounds(200, 50, 650, 50);
        notificationPanel.setBackground(new ColorUIResource(30, 60, 90));
        notificationPanel.setLayout(new BorderLayout());
        notificationPanel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 1));
        
        notificationLabel = new JLabel("", SwingConstants.CENTER);
        notificationLabel.setForeground(Color.WHITE);
        notificationLabel.setFont(new Font("Arial", Font.BOLD, 14));
        notificationPanel.add(notificationLabel, BorderLayout.CENTER);
        
        // Close notification button
        JButton closeNotificationBtn = new JButton("X");
        closeNotificationBtn.setMargin(new Insets(0, 5, 0, 5));
        closeNotificationBtn.addActionListener(e -> hideNotification());
        notificationPanel.add(closeNotificationBtn, BorderLayout.EAST);
        notificationPanel.setVisible(false);

        // Top Panel Items
        User currentUser = LoggedInUser.getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : "Guest";
        JLabel userLabel = new JLabel("Hello, " + username);
        userLabel.setFont(new FontUIResource("Arial", FontUIResource.BOLD, 14));
        userLabel.setBounds(10, 15, 250, 20);
        tp.add(userLabel);

        JButton settingsButton = new JButton("⚙ Settings");
        settingsButton.setBounds(650, 10, 100, 30);
        tp.add(settingsButton);

        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(760, 10, 100, 30);
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                LoggedInUser.logout();
                dispose();
                new LoginForm();
            }
        });
        tp.add(logoutButton);

        // Sidebar items
        String[] sidebarItems = {
                "Dashboard",
                "List of Item",
                "List of Supplier",
                "Purchase Requisition",
                "Purchase Order",
                "Help",
                "FAQ",
                "About"
        };

        Map<String, JPanel> pageMap = new HashMap<>();
        List<JButton> buttons = new ArrayList<>();

        for (String item : sidebarItems) {
            JButton btn = new JButton(item);
            btn.setForeground(inactiveColor);
            btn.setFont(new FontUIResource("Arial", FontUIResource.PLAIN, 14));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            buttons.add(btn);
            sidepanel.add(btn);

            // Page Content
            JPanel page = new JPanel();
            page.setBackground(new ColorUIResource(1, 16, 30));
            page.setBounds(0, 0, 650, 600);
            JLabel contentLabel = new JLabel("You are viewing: " + item);
            contentLabel.setForeground(ColorUIResource.white);
            page.add(contentLabel);
            page.setVisible(false);
            contentPanel.add(page);
            pageMap.put(item, page);

            btn.addActionListener(e -> {
                for (JButton b : buttons) b.setForeground(inactiveColor);
                btn.setForeground(activeColor);

                switch (item) {
                    case "Dashboard":
                        JPanel dashboardPage = pageMap.get("Dashboard");
                        dashboardPage.setVisible(true);
                        setPageContent(contentPanel, dashboardPage);
                        break;
                    case "List of Item":
                        itemLists itemPanel = new itemLists();
                        itemPanel.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, itemPanel);
                        break;
                    case "List of Supplier":
                        SupplierLists supplierPanel = new SupplierLists();
                        supplierPanel.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, supplierPanel);
                        break;
                    case "Purchase Requisition":
                        PurchaseRequisitionList prPage = new PurchaseRequisitionList();
                        prPage.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, prPage);
                        break;
                    case "Purchase Order":
                        PurchaseOrderList poPage = new PurchaseOrderList();
                        poPage.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, poPage);
                        break;
                    case "Help":
                        Help helpPage = new Help();
                        helpPage.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, helpPage);
                        break;
                    case "FAQ":
                        Faq faqPage = new Faq();
                        faqPage.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, faqPage);
                        break;
                    case "About":
                        About aboutPage = new About();
                        aboutPage.setBounds(0, 0, 650, 550);
                        setPageContent(contentPanel, aboutPage);
                        break;
                    default:
                        Snackbar.showError(this, "Page for " + item + " not implemented yet!");
                }
            });
        }

        // Default View
        buttons.get(0).setForeground(activeColor);
        pageMap.get(sidebarItems[0]).setVisible(true);

        // Add all panels
        add(tp);
        add(sidepanel);
        add(notificationPanel);
        add(contentPanel);

        // Start PR check timer
        startPRCheckTimer();
    }

    private void setPageContent(JPanel contentPanel, JPanel mainContent) {
        contentPanel.removeAll();
        if (mainContent != null) {
            contentPanel.add(mainContent);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void startPRCheckTimer() {
        notificationTimer = new Timer();
        notificationTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // In a real application, you would check your database/backend here
                if (Math.random() > 0.8 && !hasNewPR) {
                    hasNewPR = true;
                    SwingUtilities.invokeLater(() -> 
                        showNotification("New Purchase Requisition created! Click to view."));
                }
            }
        }, 0, 10000);
    }

    private void showNotification(String message) {
        JPanel notificationPanel = (JPanel) getContentPane().getComponent(2);
        notificationLabel.setText(message);
        notificationPanel.setVisible(true);
        
        notificationPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        notificationPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Component[] components = ((JPanel) getContentPane().getComponent(1)).getComponents();
                for (Component c : components) {
                    if (c instanceof JButton) {
                        JButton btn = (JButton) c;
                        if (btn.getText().equals("Purchase Requisition")) {
                            btn.doClick();
                            hideNotification();
                            break;
                        }
                    }
                }
            }
        });

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(() -> hideNotification());
            }
        }, 15000);
    }

    private void hideNotification() {
        JPanel notificationPanel = (JPanel) getContentPane().getComponent(2);
        notificationPanel.setVisible(false);
        hasNewPR = false;
        
        for (java.awt.event.MouseListener listener : notificationPanel.getMouseListeners()) {
            notificationPanel.removeMouseListener(listener);
        }
        notificationPanel.setCursor(Cursor.getDefaultCursor());
    }

    @Override
    public void dispose() {
        if (notificationTimer != null) {
            notificationTimer.cancel();
        }
        super.dispose();
    }
}