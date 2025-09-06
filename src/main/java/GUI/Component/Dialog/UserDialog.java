package GUI.Component.Dialog;

import Management.Model.User;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class UserDialog extends JDialog {
    private JTextField userIdField;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JTextField emailField;
    private JTextField emailPasswordField; // Added field for email password
    private JTextField phoneField;
    private boolean confirmed = false;
    private Map<String, String> roleMap;
    private boolean isNewUser; // Flag to indicate if it's a new user creation

    public UserDialog(JFrame parent, String title, User user, HashMap<String, String> roleMap) {
        super(parent, title, true); // Call super constructor for JDialog
        this.roleMap = roleMap;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Initialize fields
        userIdField = new JTextField();
        usernameField = new JTextField();
        passwordField = new JPasswordField();
        emailField = new JTextField();
        emailPasswordField = new JTextField(); // Initialize the new field
        phoneField = new JTextField();

        // Set up role combo box
        roleComboBox = new JComboBox<>();
        for (Map.Entry<String, String> entry : roleMap.entrySet()) {
            String roleId = entry.getKey();
            String roleName = entry.getValue();
            roleComboBox.addItem(roleName + " (" + roleId + ")");
        }

        // If editing, populate fields
        if (user != null) {
            this.isNewUser = false;
            userIdField.setText(user.getUserId());
            usernameField.setText(user.getUsername());
            passwordField.setText(user.getPassword());
            emailField.setText(user.getEmail());
            emailPasswordField.setText(user.getEmailPassword()); // Set email password field
            phoneField.setText(user.getPhoneNumber());

            // Select the correct role in combo box
            String roleDisplay = roleMap.get(user.getUserRoleId()) + " (" + user.getUserRoleId() + ")";
            roleComboBox.setSelectedItem(roleDisplay);

            userIdField.setEditable(false); // Don't allow ID changes for existing users
        } else {
            this.isNewUser = true;
            userIdField.setEditable(true); // Allow setting ID for new users (will be overwritten by controller)
        }

        // Add components using GridBagConstraints
        addComponent(this, new JLabel("User ID:"), gbc, 0, 0, 1, 1);
        addComponent(this, userIdField, gbc, 1, 0, 1, 1);

        addComponent(this, new JLabel("Username:"), gbc, 0, 1, 1, 1);
        addComponent(this, usernameField, gbc, 1, 1, 1, 1);

        addComponent(this, new JLabel("Password:"), gbc, 0, 2, 1, 1);
        addComponent(this, passwordField, gbc, 1, 2, 1, 1);

        addComponent(this, new JLabel("Role:"), gbc, 0, 3, 1, 1);
        addComponent(this, roleComboBox, gbc, 1, 3, 1, 1);

        addComponent(this, new JLabel("Email Address:"), gbc, 0, 4, 1, 1);
        addComponent(this, emailField, gbc, 1, 4, 1, 1);

        addComponent(this, new JLabel("Email Password:"), gbc, 0, 5, 1, 1); // Label for email password
        addComponent(this, emailPasswordField, gbc, 1, 5, 1, 1); // Field for email password

        addComponent(this, new JLabel("Phone Number:"), gbc, 0, 6, 1, 1); // Changed to 6
        addComponent(this, phoneField, gbc, 1, 6, 1, 1);  // Changed to 6

        // Add buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose(); // Use dispose() instead of dialog.dispose()
        });
        buttonPanel.add(okButton);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose()); // Use dispose() instead of dialog.dispose()
        buttonPanel.add(cancelButton);

        gbc.gridx = 0;
        gbc.gridy = 7; // Changed to 7
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        add(buttonPanel, gbc); // Use add() instead of dialog.add()

        pack();
    }

    private void addComponent(Container container, Component component, GridBagConstraints gbc, int gridx, int gridy, int gridwidth, int gridheight) {
        gbc.gridx = gridx;
        gbc.gridy = gridy;
        gbc.gridwidth = gridwidth;
        gbc.gridheight = gridheight;
        container.add(component, gbc);
    }

    public boolean showDialog() {
        setLocationRelativeTo(getParent()); // Use setLocationRelativeTo(getParent())
        setVisible(true);
        return confirmed;
    }

    public User getUser() {
        // Extract role ID from combo box selection (format: "Role Name (roleId)")
        String roleSelection = (String) roleComboBox.getSelectedItem();
        String roleId = roleSelection.substring(roleSelection.lastIndexOf("(") + 1, roleSelection.lastIndexOf(")"));

        User user = new User(
                userIdField.getText(),
                usernameField.getText(),
                new String(passwordField.getPassword()),
                roleId,
                emailField.getText(),
                emailPasswordField.getText(), // Get value from the field
                phoneField.getText()
        );
        return user;
    }

    public boolean isNewUser() {
        return isNewUser;
    }
}

