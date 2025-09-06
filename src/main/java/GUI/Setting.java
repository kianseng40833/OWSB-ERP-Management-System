package GUI;

import Management.Controller.UserController;
import Management.Model.LoggedInUser;
import Management.Model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.HashMap;

public class Setting extends JPanel {
    private JTextField usernameField;
    private JTextField userRoleField;
    private JTextField passwordField;
    private JTextField emailField;
    private JTextField emailPasswordField; // Added field for email password
    private JTextField phoneField;
    private UserController userController;

    public Setting() {
        userController = new UserController();
        User currentUser = LoggedInUser.getCurrentUser();

        // Use GridBagLayout for a more flexible and professional layout
        setLayout(new GridBagLayout());
        setBorder(new EmptyBorder(30, 30, 30, 30)); // Increased padding around the panel

        // Set preferred size for the panel to make it larger
        setPreferredSize(new Dimension(600, 450));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10); // Increased padding between components
        gbc.fill = GridBagConstraints.HORIZONTAL; // Components fill horizontally

        // User Role
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST; // Align labels to the west
        add(new JLabel("User Role:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        userRoleField = new JTextField();
        userRoleField.setEditable(false);
        HashMap<String, String> roleMap = userController.loadRoleMap();
        String roleName = roleMap.getOrDefault(currentUser.getUserRoleId(), "Unknown");
        userRoleField.setText(roleName);
        add(userRoleField, gbc);

        // Username
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0; // Reset weight for label
        add(new JLabel("Username:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        usernameField = new JTextField(currentUser.getUsername());
        add(usernameField, gbc);

        // Password
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0; // Reset weight for label
        add(new JLabel("Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        passwordField = new JTextField(currentUser.getPassword());
        add(passwordField, gbc);

        // Email
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0; // Reset weight for label
        add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        emailField = new JTextField(currentUser.getEmail());
        add(emailField, gbc);

        // Email Password
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.weightx = 0; // Reset weight for label
        add(new JLabel("Email Password:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        emailPasswordField = new JTextField(currentUser.getEmailPassword());
        add(emailPasswordField, gbc);

        // Phone Number
        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.weightx = 0; // Reset weight for label
        add(new JLabel("Phone Number:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1.0; // Allow field to expand horizontally
        phoneField = new JTextField(currentUser.getPhoneNumber());
        add(phoneField, gbc);

        // Buttons
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 1; // Spans 1 column
        gbc.weightx = 0; // Reset weight for buttons
        gbc.anchor = GridBagConstraints.EAST; // Align to the right
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> onSave());
        add(saveButton, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST; // Align to the left
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> resetFields(currentUser));
        add(cancelButton, gbc);
    }

    private void onSave() {
        User currentUser = LoggedInUser.getCurrentUser();
        String updatedUsername = usernameField.getText().trim();
        String updatedPassword = passwordField.getText();
        String updatedEmail = emailField.getText().trim();
        String updatedEmailPassword = emailPasswordField.getText();
        String updatedPhone = phoneField.getText().trim();

        // Input Validation
        if (updatedUsername.isEmpty() || updatedPassword.isEmpty() || updatedEmail.isEmpty() || updatedPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields must be filled.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidEmail(updatedEmail)) {
            JOptionPane.showMessageDialog(this, "Invalid email format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!isValidPhoneNumber(updatedPhone)) {
            JOptionPane.showMessageDialog(this, "Invalid phone number format. Please use digits only.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Build updated User object
        User updatedUser = new User(
                currentUser.getUserId(),
                updatedUsername,
                updatedPassword,
                currentUser.getUserRoleId(),
                updatedEmail,
                updatedEmailPassword,
                updatedPhone
        );

        boolean success = userController.updateUserInFile(updatedUser);

        if (success) {
            LoggedInUser.setCurrentUser(updatedUser);
            JOptionPane.showMessageDialog(this, "Profile updated successfully!");
        } else {
            JOptionPane.showMessageDialog(this, "Failed to update profile.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetFields(User user) {
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        emailField.setText(user.getEmail());
        emailPasswordField.setText(user.getEmailPassword());
        phoneField.setText(user.getPhoneNumber());
    }

    // Basic email validation
    private boolean isValidEmail(String email) {
        return email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    // Basic phone number validation (digits only)
    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("^\\d+$");
    }
}
