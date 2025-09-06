package GUI.Dashboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;


import GUI.*;
import GUI.Component.*;
import Management.Model.LoggedInUser;
import Management.Model.User;

public class PurchaseManager extends JFrame {

    static ColorUIResource activeColor = new ColorUIResource(255, 255, 255);
    static ColorUIResource inactiveColor = new ColorUIResource(160, 160, 160);

    private JPanel contentPanel;
    private List<JButton> sidebarButtons;

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
        contentPanel = new JPanel(null);
        contentPanel.setBounds(200, 80, 650, 550);
        contentPanel.setBackground(new Color(0, 33, 71));

        // Top Panel Items
// 🧑 Display the logged-in username on the left
        User currentUser = LoggedInUser.getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : "Guest";
        JLabel userLabel = new JLabel("Hello, " + username);
        userLabel.setFont(new FontUIResource("Arial", FontUIResource.BOLD, 14));
        userLabel.setBounds(10, 15, 250, 20);
        tp.add(userLabel);

// ⚙️ Settings button (right side)
        JButton profileButton = new JButton("⚙ Profile");
        profileButton.setBounds(650, 10, 100, 30);
        profileButton.addActionListener(e -> {
            // This will load the Setting panel directly into the main content area
            Setting settingPanel = new Setting();
            settingPanel.setBounds(0, 0, contentPanel.getWidth(), contentPanel.getHeight());
            setPageContent(contentPanel, settingPanel);
        });
        tp.add(profileButton);

// 🚪 Logout button (right side)
        JButton logoutButton = new JButton("Logout");
        logoutButton.setBounds(760, 10, 100, 30);
        logoutButton.addActionListener(e -> {
            int confirm = Snackbar.showConfirmDialog(this, "Are you sure you want to log out?", "Logout");
            if (confirm == JOptionPane.YES_OPTION) {
                LoggedInUser.logout(); // Clear session
                dispose();             // Close dashboard
                new LoginForm();   // Return to login form
            }
        });
        tp.add(logoutButton);


        // Sidebar items
        String[] sidebarItems = {
                "List of Item", // Placeholder
                "List of Supplier", // Will load supplierListPanel
                "Purchase Requisition", // Placeholder
                "Purchase Order",
                "Help",
                "FAQ",
                "About"
        };

        Map<String, JPanel> pageMap = new HashMap<>();
        sidebarButtons = new ArrayList<>();

        for (String item : sidebarItems) {
            JButton btn = new JButton(item);
            btn.setForeground(inactiveColor);
            btn.setFont(new FontUIResource("Arial", FontUIResource.PLAIN, 14));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            sidebarButtons.add(btn);
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

            // Button Action
            btn.addActionListener(e -> {
                for (JButton b : sidebarButtons) b.setForeground(inactiveColor);
                btn.setForeground(activeColor);
                loadPageContent(item);
            });
        }
        // Default View
        sidebarButtons.get(0).setForeground(activeColor);
        loadPageContent("List of Item");

        // Add all panels
        add(tp);
        add(sidepanel);
        add(contentPanel);
    }
    private void loadPageContent(String item) {
        contentPanel.removeAll();
        JPanel newContent = null;
        // Switch based on button text
                switch (item) {
                    case "List of Item":
                        newContent = new itemLists();
                        break;
                    case "List of Supplier":
                        newContent= new SupplierLists();
                        break;
                    case "Purchase Requisition":
                        newContent = new PurchaseRequisitionList();
                        break;
                    case "Purchase Order":
                        newContent = new PurchaseOrderList();
                        break;
                    case "Help":
                        newContent = new Help();
                        break;
                    case "FAQ":
                        newContent = new Faq();
                        break;
                    case "About":
                        newContent = new About();
                        break;
                    default:
                        Snackbar.showError(this, "Page for " + item + " not implemented yet!");
                        newContent = new JPanel();
                        newContent.setBackground(new Color(1, 16, 30));
                        JLabel errorLabel = new JLabel("Error: " + item + " page not found.");
                        errorLabel.setForeground(Color.RED);
                        newContent.add(errorLabel);
                }
        if (newContent != null) {
            newContent.setBounds(0, 0, contentPanel.getWidth(), contentPanel.getHeight());
            contentPanel.add(newContent);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void setPageContent(JPanel contentPanel, JPanel mainContent) {
        contentPanel.removeAll();
        if (mainContent != null) {
            contentPanel.add(mainContent);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
}