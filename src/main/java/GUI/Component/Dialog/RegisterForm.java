package GUI.Component.Dialog;

import GUI.Component.Snackbar;
import Management.Controller.RegisterController;
import Management.Model.UserRole;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class RegisterForm extends JPanel {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel passwordStrengthLabel;
    private JComboBox<UserRole> roleComboBox;
    private JTextField emailField;
    private JTextField emailPasswordField; // Added for email password
    private JTextField phoneNumberField;
    private final RegisterController registerController;

    public RegisterForm() {
        registerController = new RegisterController();
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // -------- TOP: TITLE --------
        JLabel title = new JLabel("Register New User");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setBorder(new EmptyBorder(20, 10, 20, 10));
        add(title);

        // Add some vertical space
        add(Box.createVerticalStrut(10));

        // -------- CENTER: FORM --------
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        passwordStrengthLabel = new JLabel(" ");
        roleComboBox = new JComboBox<>();
        emailField = new JTextField(20);
        emailPasswordField = new JTextField(20); // Initialize emailPasswordField
        phoneNumberField = new JTextField(20);

        // Add fields with consistent styling
        addField(formPanel, "Username:", usernameField);
        addField(formPanel, "Password:", passwordField);
        addField(formPanel, "Role:", roleComboBox);
        addField(formPanel, "Email:", emailField);
        addField(formPanel, "Email Password:", emailPasswordField); // Added field
        addField(formPanel, "Phone Number:", phoneNumberField);

        // Password Strength Label
        JPanel strengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        strengthPanel.setBackground(Color.WHITE);
        strengthPanel.add(Box.createHorizontalStrut(160)); // Match label width + spacing
        passwordStrengthLabel.setFont(new Font("Arial", Font.ITALIC, 12));
        strengthPanel.add(passwordStrengthLabel);
        formPanel.add(strengthPanel);
        formPanel.add(Box.createVerticalStrut(10));

        // Password strength listener
        passwordField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePasswordStrength();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // Not needed for plain text fields
            }
        });

        loadRoles();
        add(formPanel);

        // Add some vertical space before buttons
        add(Box.createVerticalGlue());

        // -------- BOTTOM: BUTTONS --------
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        buttonPanel.add(Box.createHorizontalGlue());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(Color.LIGHT_GRAY);
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            clearFields();
            closeWindow();
        });

        JButton registerButton = new JButton("Register");
        registerButton.setBackground(new Color(0, 123, 255));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        registerButton.setFont(new Font("Arial", Font.BOLD, 14));
        registerButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        registerButton.addActionListener(e -> register());

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(registerButton);

        add(buttonPanel);
    }

    private void addField(JPanel panel, String labelText, JComponent field) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setBackground(Color.WHITE);
        rowPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        rowPanel.add(label);
        rowPanel.add(Box.createHorizontalStrut(10));
        rowPanel.add(field);

        panel.add(rowPanel);
        panel.add(Box.createVerticalStrut(10));
    }

    private void closeWindow() {
        Window window = SwingUtilities.getWindowAncestor(this);
        if (window != null) {
            window.dispose();
        }
    }

    private void loadRoles() {
        List<UserRole> roles = registerController.loadRoles();
        if (roles.isEmpty()) {
            Snackbar.showWarning(this, "No roles found!");
            return;
        }

        DefaultComboBoxModel<UserRole> model = new DefaultComboBoxModel<>();
        for (UserRole role : roles) {
            model.addElement(role);
        }
        roleComboBox.setModel(model);
    }

    private void updatePasswordStrength() {
        String password = new String(passwordField.getPassword());
        if (password.length() < 6) {
            passwordStrengthLabel.setText("Too short");
            passwordStrengthLabel.setForeground(Color.RED);
        } else if (isWeakPassword(password)) {
            passwordStrengthLabel.setText("Very Weak");
            passwordStrengthLabel.setForeground(Color.RED);
        } else if (password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$")) {
            passwordStrengthLabel.setText("Strong");
            passwordStrengthLabel.setForeground(Color.GREEN);
        } else if (password.matches("^(?=.*[a-zA-Z])(?=.*\\d)[A-Za-z\\d]{6,}$")) {
            passwordStrengthLabel.setText("Medium");
            passwordStrengthLabel.setForeground(Color.ORANGE);
        } else {
            passwordStrengthLabel.setText("Weak");
            passwordStrengthLabel.setForeground(Color.RED);
        }
    }

    private boolean isWeakPassword(String password) {
        String lowerCasePassword = password.toLowerCase();
        return lowerCasePassword.equals("abcdef") || lowerCasePassword.equals("123456") ||
                lowerCasePassword.startsWith("abcd") || lowerCasePassword.startsWith("1234") ||
                lowerCasePassword.endsWith("abcd") || lowerCasePassword.endsWith("1234");
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();
        UserRole selectedRole = (UserRole) roleComboBox.getSelectedItem();
        String email = emailField.getText().trim();
        String emailPassword = emailPasswordField.getText().trim(); // Get email password
        String phoneNumber = phoneNumberField.getText().trim();

        if (username.isEmpty() || password.isEmpty() || selectedRole == null || email.isEmpty() || emailPassword.isEmpty() || phoneNumber.isEmpty()) { //check emailPassword
            Snackbar.showWarning(this, "All fields are required.");
            return;
        }

        if (password.length() < 6) {
            Snackbar.showWarning(this, "Password must be at least 6 characters long.");
            return;
        }

        if (isWeakPassword(password)) {
            Snackbar.showWarning(this, "This is a very weak password. Please choose a stronger one.");
            passwordField.setText("");
            passwordStrengthLabel.setText(" ");
            return;
        }

        if (registerController.isUsernameTaken(username)) {
            Snackbar.showWarning(this, "Username already exists");
            return;
        }

        String userRoleID = selectedRole.getUserRoleId();
        boolean success = registerController.registerUser(username, password, userRoleID, email, emailPassword, phoneNumber); //send emailPassword

        if (success) {
            Snackbar.showSuccess(this, "Registration successful");
            clearFields();
            closeWindow();
        } else {
            Snackbar.showError(this, "Registration failed. Please try again.");
        }
    }

    private void clearFields() {
        usernameField.setText("");
        passwordField.setText("");
        passwordStrengthLabel.setText(" ");
        if (roleComboBox.getItemCount() > 0) {
            roleComboBox.setSelectedIndex(0);
        }
        emailField.setText("");
        emailPasswordField.setText(""); //clear emailPassword field
        phoneNumberField.setText("");
    }
}

