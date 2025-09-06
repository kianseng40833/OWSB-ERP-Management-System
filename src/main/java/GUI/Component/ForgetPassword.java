package GUI.Component;

import Management.Controller.LoginController;
import Management.Model.User;
import javax.swing.*;
import javax.swing.plaf.ColorUIResource;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ForgetPassword extends JFrame {
    private JTextField emailField;
    private JPasswordField emailPasswordField;
    private JPasswordField newPasswordField;
    private JPasswordField confirmPasswordField;
    private JButton nextButton;
    private JButton saveButton;
    private JButton cancelButton; // Added Cancel button
    private LoginController loginController;
    private JPanel verificationPanel;
    private JPanel passwordResetPanel;
    private User verifiedUser;
    private LoginForm loginForm; // Added to store LoginForm instance

    public ForgetPassword(LoginForm loginForm) { // Modified constructor to accept LoginForm
        this.loginForm = loginForm; // Store the LoginForm instance
        this.loginController = new LoginController();

        setTitle("Forgot Password");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new CardLayout());
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);

        // Create panels
        verificationPanel = createVerificationPanel();
        passwordResetPanel = createPasswordResetPanel();

        // Add panels to card layout
        add(verificationPanel, "verification");
        add(passwordResetPanel, "passwordReset");

        // Show verification panel first
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "verification");

        setVisible(true);
    }

    private JPanel createVerificationPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        // Logo
        JLabel logoLabel = new JLabel("OWSB", SwingConstants.CENTER);
        logoLabel.setBounds(0, 30, 600, 40);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 36));
        logoLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(logoLabel);

        // Title
        JLabel titleLabel = new JLabel("Verify Your Identity", SwingConstants.CENTER);
        titleLabel.setBounds(0, 80, 600, 30);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(titleLabel);

        // Email field
        JLabel emailLabel = new JLabel("Email");
        emailLabel.setBounds(150, 130, 300, 20);
        emailLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(emailLabel);

        emailField = new JTextField();
        emailField.setBounds(150, 150, 300, 30);
        emailField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(emailField);

        // Email Password field
        JLabel emailPassLabel = new JLabel("Email Password");
        emailPassLabel.setBounds(150, 190, 300, 20);
        emailPassLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        emailPassLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(emailPassLabel);

        emailPasswordField = new JPasswordField();
        emailPasswordField.setBounds(150, 210, 300, 30);
        emailPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(emailPasswordField);

        // Next button
        nextButton = new JButton("Next");
        nextButton.setBounds(200, 270, 200, 40);
        nextButton.setFont(new Font("Arial", Font.BOLD, 16));
        nextButton.setForeground(Color.WHITE);
        nextButton.setBackground(new Color(255, 165, 0));
        nextButton.setOpaque(true);
        nextButton.setBorderPainted(false);
        nextButton.addActionListener(e -> verifyIdentity());
        panel.add(nextButton);

        // Cancel Button for Verification Panel
        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 320, 200, 40); // Adjusted position
        cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(100, 100, 100)); // Darker gray
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> {
            loginForm.showLoginForm(); // Use the stored LoginForm instance to go back
            dispose(); // Dispose of the current ForgetPassword frame
        });
        panel.add(cancelButton);

        return panel;
    }

    private JPanel createPasswordResetPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(Color.WHITE);

        // Logo
        JLabel logoLabel = new JLabel("OWSB", SwingConstants.CENTER);
        logoLabel.setBounds(0, 30, 600, 40);
        logoLabel.setFont(new Font("Arial", Font.BOLD, 36));
        logoLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(logoLabel);

        // Title
        JLabel titleLabel = new JLabel("Set New Password", SwingConstants.CENTER);
        titleLabel.setBounds(0, 80, 600, 30);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(titleLabel);

        // New password field
        JLabel newPassLabel = new JLabel("New Password");
        newPassLabel.setBounds(150, 130, 300, 20);
        newPassLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        newPassLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(newPassLabel);

        newPasswordField = new JPasswordField();
        newPasswordField.setBounds(150, 150, 300, 30);
        newPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(newPasswordField);

        // Confirm new password field
        JLabel confirmPassLabel = new JLabel("Confirm New Password");
        confirmPassLabel.setBounds(150, 190, 300, 20);
        confirmPassLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        confirmPassLabel.setForeground(new ColorUIResource(0, 33, 71));
        panel.add(confirmPassLabel);

        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setBounds(150, 210, 300, 30);
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 14));
        panel.add(confirmPasswordField);

        // Save button
        saveButton = new JButton("Save");
        saveButton.setBounds(200, 270, 200, 40);
        saveButton.setFont(new Font("Arial", Font.BOLD, 16));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(255, 165, 0));
        saveButton.setOpaque(true);
        saveButton.setBorderPainted(false);
        saveButton.addActionListener(e -> resetPassword());
        panel.add(saveButton);

        // Cancel Button for Password Reset Panel
        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(200, 320, 200, 40); // Adjusted position
        cancelButton.setFont(new Font("Arial", Font.BOLD, 16));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setBackground(new Color(100, 100, 100)); // Darker gray
        cancelButton.setOpaque(true);
        cancelButton.setBorderPainted(false);
        cancelButton.addActionListener(e -> {
            loginForm.showLoginForm(); // Use the stored LoginForm instance to go back
            dispose(); // Dispose of the current ForgetPassword frame
        });
        panel.add(cancelButton);

        return panel;
    }

    private void verifyIdentity() {
        String email = emailField.getText().trim();
        String emailPassword = new String(emailPasswordField.getPassword()).trim();

        if (email.isEmpty() || emailPassword.isEmpty()) {
            Snackbar.showWarning(this, "Both fields are required.");
            return;
        }

        User user = loginController.getUserByEmail(email);
        if (user == null) {
            Snackbar.showError(this, "User not found.");
            return;
        }

        if (!user.getEmailPassword().equals(emailPassword)) {
            Snackbar.showError(this, "Incorrect email password.");
            return;
        }

        this.verifiedUser = user;
        // Switch to password reset panel
        ((CardLayout) getContentPane().getLayout()).show(getContentPane(), "passwordReset");
    }

    private void resetPassword() {
        String newPassword = new String(newPasswordField.getPassword()).trim();
        String confirmPassword = new String(confirmPasswordField.getPassword()).trim();

        if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
            Snackbar.showWarning(this, "Both fields are required.");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            Snackbar.showWarning(this, "Passwords do not match.");
            return;
        }

        // Only update the password - other details are preserved automatically
        boolean resetSuccess = loginController.resetPassword(
                verifiedUser.getUsername(),
                newPassword
        );

        if (resetSuccess) {
            Snackbar.showSuccess(this, "Password reset successful!");
            this.dispose();
            loginForm.showLoginForm(); // Use the stored LoginForm instance to go back
        } else {
            Snackbar.showError(this, "Failed to reset password.");
        }
    }
}
