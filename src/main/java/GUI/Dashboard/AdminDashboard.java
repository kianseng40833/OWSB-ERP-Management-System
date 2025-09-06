package GUI.Dashboard;

import GUI.*; // Keep this for other GUI components in GUI package
import GUI.Component.*;
import GUI.PurchaseRequisitionList;
import GUI.UserListPage;
import Management.Model.LoggedInUser;
import Management.Model.User;
import GUI.SalesEntry; // Corrected import for SalesEntry

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap; // Added import for HashMap
import java.util.List; // Added import for List
import java.util.Map; // Added import for Map

public class AdminDashboard extends JFrame {
    static ColorUIResource activeColor = new ColorUIResource(255, 255, 255);
    static ColorUIResource inactiveColor = new ColorUIResource(160, 160, 160);

    private JPanel contentPanel;
    private List<JButton> sidebarButtons;// This panel will hold the dynamically loaded pages

    public AdminDashboard() {
        setTitle("OWSB - Admin Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null); // Using null layout for absolute positioning as in your original
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
        sidepanel.setBackground(new Color(14, 2, 41));
        sidepanel.setBounds(0, 50, 160, 650);
        sidepanel.setLayout(new BoxLayout(sidepanel, BoxLayout.Y_AXIS));

        // Content Panel - This will hold the main content for each sidebar selection
        contentPanel = new JPanel(null); // Using null layout for content panel as well
        contentPanel.setBounds(200, 80, 650, 550); // Adjusted bounds to match your original
        contentPanel.setBackground(new ColorUIResource(0, 33, 71));

        // Top Panel Items
        User currentUser = LoggedInUser.getCurrentUser();
        JLabel userLabel = new JLabel("Hello, " + currentUser.getUsername());
        userLabel.setFont(new FontUIResource("Arial", FontUIResource.BOLD, 14));
        userLabel.setBounds(10, 15, 250, 20);
        tp.add(userLabel);

        JButton profileButton = new JButton("⚙ Profile");
        profileButton.setBounds(650, 10, 100, 30);
        profileButton.addActionListener(e -> {
            // This will load the Setting panel directly into the main content area
            Setting settingPanel = new Setting();
            settingPanel.setBounds(0, 0, contentPanel.getWidth(), contentPanel.getHeight());
            setPageContent(contentPanel, settingPanel);
        });
        tp.add(profileButton);

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

        // Sidebar items - ALL items directly in this sidebar
        String[] sidebarItems = {
                "Dashboard", // This will be a general summary dashboard
                "Sales Entry", // New item for Sales Entry
                "List of Item",
                "List of Supplier",
                "List of User",
                "Purchase Requisition",
                "Purchase Order",
                "Process Payment",
                "Pending Item List",
                "Stock Level",
                "Finance report",
                "Help",
                "FAQ",
                "About"
        };

        // Using a Map to store references to buttons for easy state management
        // and a List for iteration order.
        Map<String, JPanel> pageMap = new HashMap<>();
        sidebarButtons = new ArrayList<>();

        for (String item : sidebarItems) {
            JButton btn = new JButton(item);
            btn.setForeground(inactiveColor);
            btn.setFont(new Font("Arial", Font.PLAIN, 14));
            btn.setContentAreaFilled(false);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            btn.setHorizontalAlignment(SwingConstants.LEFT);
            sidebarButtons.add(btn);
            sidepanel.add(btn);

            JPanel page = new JPanel();
            page.setBackground(new Color(1, 16, 30));
            page.setBounds(0, 0, 650, 500);
            JLabel contentLabel = new JLabel("You are viewing: " + item);
            contentLabel.setForeground(Color.WHITE);
            page.add(contentLabel);
            page.setVisible(false);
            contentPanel.add(page);
            pageMap.put(item, page);

            btn.addActionListener(e -> {
                for (JButton b : sidebarButtons) b.setForeground(inactiveColor);
                btn.setForeground(activeColor);
                loadPageContent(item);
            });
        }

        sidebarButtons.get(0).setForeground(activeColor);
        loadPageContent("Dashboard");

        add(tp);
        add(sidepanel);
//        add(notificationDisplayPanel);
        add(contentPanel);

//        PONotificationSystem.getInstance().addPropertyChangeListener(this);
//        System.out.println("FinanceManager: Registered as PropertyChangeListener for PONotificationSystem.");
    }

    private void loadPageContent(String item) {
        contentPanel.removeAll();
        JPanel newContent = null;
                // Load content based on selected item
        switch (item) {
            case "Dashboard":
                newContent = new DashboardPanel();
                break;
            case "Sales Entry":
                newContent = new SalesEntry();
                break;
            case "List of Item":
                newContent = new itemLists();
                break;
            case "List of Supplier":
                newContent = new SupplierLists();
                break;
            case "Purchase Requisition":
                newContent = new PurchaseRequisitionList();
                break;
            case "Purchase Order":
                newContent = new PurchaseOrderList();
                break;
            case "List of User":
                newContent = new UserListPage();
                break;
            case "Process Payment" :
                newContent = new PaymentInvoiceList();
                break;
            case "Pending Item List" :
                newContent = new PendingItemStockList();
                break;
            case "Stock Level":
                newContent = new StockLevelList();
                break;
            case "Finance report":
                newContent = new FinanceReport();
                break;
            case "Help" :
                newContent = new Help();
                break;
            case "FAQ":
                newContent = new Faq();
                break;
            case "About" :
                newContent = new About();
                break;
            default:
                Snackbar.showError(this, "Page for " + item + " not implemented yet!");
        }
        if (newContent != null) {
            newContent.setBounds(0, 0, contentPanel.getWidth(), contentPanel.getHeight());
            contentPanel.add(newContent);
        }
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    /**
     * Helper method to set content in the main content panel.
     */
    private void setPageContent(JPanel targetContentPanel, JPanel newContent) {
        targetContentPanel.removeAll();
        if (newContent != null) {
            targetContentPanel.add(newContent);
        }
        targetContentPanel.revalidate();
        targetContentPanel.repaint();
    }
}
