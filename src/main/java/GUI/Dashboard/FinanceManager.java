package GUI.Dashboard;

import GUI.*;
import GUI.Component.LoginForm;
import GUI.Component.Snackbar;
import Management.Model.LoggedInUser;
import Management.Model.User;
import Management.Model.PurchaseOrder;
import Management.Controller.POController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class FinanceManager extends JFrame {

    static final Color activeColor = Color.WHITE;
    static final Color inactiveColor = new Color(160, 160, 160);

    private JPanel contentPanel;
    private List<JButton> sidebarButtons;

    private POController poController;

    public FinanceManager() {
        setTitle("Finance Dashboard");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(0, 33, 71));
        setResizable(false);

        poController = new POController();

        JPanel tp = new JPanel();
        tp.setBackground(Color.lightGray);
        tp.setBounds(0, 0, 900, 50);
        tp.setLayout(null);

        JPanel sidepanel = new JPanel();
        sidepanel.setBackground(new Color(14, 2, 41));
        sidepanel.setBounds(0, 50, 160, 650);
        sidepanel.setLayout(new BoxLayout(sidepanel, BoxLayout.Y_AXIS));

        contentPanel = new JPanel(null);
        contentPanel.setBounds(200, 80, 650, 550);
        contentPanel.setBackground(new Color(0, 33, 71));

        User currentUser = LoggedInUser.getCurrentUser();
        String username = (currentUser != null) ? currentUser.getUsername() : "Guest";
        JLabel userLabel = new JLabel("Hello, " + username);
        userLabel.setFont(new Font("Arial", Font.BOLD, 14));
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

        String[] sidebarItems = {
                "Purchase Requisition",
                "Purchase Order",
                "Process Payment",
                "Finance report",
                "Help",
                "FAQ",
                "About"
        };

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
        loadPageContent("Purchase Requisition");

        add(tp);
        add(sidepanel);
        add(contentPanel);

    }

    private void loadPageContent(String item) {
        contentPanel.removeAll();
        JPanel newContent = null;

        switch (item) {
            case "Purchase Requisition":
                newContent = new PurchaseRequisitionList();
                break;
            case "Process Payment":
                newContent = new PaymentInvoiceList();
                break;
            case "Finance report":
                newContent = new FinanceReport();
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

    /**
     * Updates the notification display panel based on the current pending POs
     * reported by the PONotificationSystem. This method only affects the UI.
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