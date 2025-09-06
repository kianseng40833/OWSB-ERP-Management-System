package GUI.Component;

import GUI.Dashboard.AdminDashboard;
import GUI.Dashboard.FinanceManager;
import GUI.Dashboard.InventoryManager;
import GUI.Dashboard.PurchaseManager;
import GUI.Dashboard.SalesManager;
import Management.Controller.LoginController;
import GUI.Component.Snackbar;
import Management.Model.User;

import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static Management.Model.LoggedInUser.setCurrentUser;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton forgotButton;
    private final LoginController loginController;
    private String userEmail;
    private ForgetPassword forgetPasswordPanel; // Added ForgetPassword panel

    public LoginForm() {
        loginController = new LoginController();

        setTitle("Welcome to OWSB - Login");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new ColorUIResource(0, 33, 71));

        // Logo
        JLabel logoLabel = new JLabel("OWSB", SwingConstants.CENTER);
        logoLabel.setBounds(0, 30, 600, 40);
        logoLabel.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.BOLD, 36));
        logoLabel.setForeground(new ColorUIResource(255, 255, 255));
        add(logoLabel);

        // Login label
        JLabel loginLabel = new JLabel("Login", SwingConstants.CENTER);
        loginLabel.setBounds(0, 80, 600, 30);
        loginLabel.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.BOLD, 24));
        loginLabel.setForeground(new ColorUIResource(255, 255, 255));
        add(loginLabel);

        // Username field
        JLabel userLabel = new JLabel("Username");
        userLabel.setBounds(150, 130, 300, 20);
        userLabel.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.PLAIN, 14));
        userLabel.setForeground(new ColorUIResource(255, 255, 255));
        add(userLabel);

        usernameField = new JTextField();
        usernameField.setBounds(150, 150, 300, 30);
        usernameField.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.PLAIN, 14));
        // Add ActionListener to trigger login on Enter key press
        usernameField.addActionListener(e -> login());
        add(usernameField);

        // Password field
        JLabel passLabel = new JLabel("Password");
        passLabel.setBounds(150, 190, 300, 20);
        passLabel.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.PLAIN, 14));
        passLabel.setForeground(new ColorUIResource(255, 255, 255));
        add(passLabel);

        passwordField = new JPasswordField();
        passwordField.setBounds(150, 210, 300, 30);
        passwordField.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.PLAIN, 14));
        // Add ActionListener to trigger login on Enter key press
        passwordField.addActionListener(e -> login());
        add(passwordField);

        // Forgot password
        forgotButton = new JButton("Forgot Password?");
        forgotButton.setBounds(150, 250, 150, 20);
        forgotButton.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.PLAIN, 12));
        forgotButton.setForeground(new ColorUIResource(200, 200, 200));
        forgotButton.setContentAreaFilled(false);
        forgotButton.setBorderPainted(false);
        forgotButton.setFocusPainted(false);
        forgotButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                showForgotPasswordForm();
            }
        });
        add(forgotButton);

        // Login button
        loginButton = new JButton("Sign in");
        loginButton.setBounds(200, 290, 200, 40);
        loginButton.setFont(new javax.swing.plaf.FontUIResource("Arial", javax.swing.plaf.FontUIResource.BOLD, 16));
        loginButton.setForeground(new ColorUIResource(255, 255, 255));
        loginButton.setBackground(new ColorUIResource(255, 165, 0));
        loginButton.setOpaque(true);
        loginButton.setBorderPainted(false);
        loginButton.addActionListener(e -> login());
        add(loginButton);

        setVisible(true);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            Snackbar.showWarning(this, "Both fields are required.");
            return;
        }

        boolean success = loginController.login(username, password);
        User user = loginController.getAuthenticatedUser();
        setCurrentUser(user);
        if (success) {
            Snackbar.showSuccess(this, "Login successful!");
            dispose();

            switch (user.getUserRoleId()) {
                case "ur_id1":
                    new AdminDashboard().setVisible(true);
                    break;
                case "ur_id2":
                    new SalesManager().setVisible(true);
                    break;
                case "ur_id3":
                    new PurchaseManager().setVisible(true);
                    break;
                case "ur_id4":
                    new FinanceManager().setVisible(true);
                    break;
                case "ur_id5":
                    new InventoryManager().setVisible(true);
                    break;
                default:
                    Snackbar.showError(this, "Invalid role.");
                    return;
            }
        } else {
            Snackbar.showError(this, "Invalid username or password.");
        }
    }

    private void showForgotPasswordForm() {
        // Dispose of the current login form
        this.dispose();

        // Create and show the ForgetPassword panel
        forgetPasswordPanel = new ForgetPassword(this); // Pass LoginForm instance
        forgetPasswordPanel.setVisible(true);
    }

    // Method to go back to the login form from ForgetPassword
    public void showLoginForm() {
        // Dispose of the ForgetPassword panel
        if (forgetPasswordPanel != null) {
            forgetPasswordPanel.dispose();
        }

        // Re-create and show the login form
        new LoginForm().setVisible(true);
    }

}