package GUI;

import GUI.Component.Dialog.UserDialog;
import Management.Controller.UserController;
import Management.Model.User;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener

public class UserListPage extends JPanel {
    private final UserController userListController;
    private DefaultTableModel tableModel;
    private JTable table;
    private HashMap<String, String> roleMap;
    private JTextField searchField; // New search field
    // private JButton searchButton; // Removed search button

    public UserListPage() {
        userListController = new UserController();
        roleMap = userListController.loadRoleMap();

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(240, 240, 245)); // Set a light gray background

        // Top panel containing title, search, and action buttons
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        headerPanel.setBorder(new EmptyBorder(0, 0, 15, 0));
        headerPanel.setBackground(new Color(240, 240, 245));

        // Panel for title and action buttons
        JPanel titleAndActionButtonsPanel = new JPanel(new BorderLayout());
        titleAndActionButtonsPanel.setBackground(new Color(240, 240, 245));

        JLabel titleLabel = new JLabel("User Management System");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.BLACK);
        titleAndActionButtonsPanel.add(titleLabel, BorderLayout.WEST);

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionButtonPanel.setBackground(new Color(240, 240, 245));

        JButton addButton = new JButton("Add User");
        JButton editButton = new JButton("Edit User");
        addButton.addActionListener(this::addUser);
        editButton.addActionListener(this::editUser);

        actionButtonPanel.add(addButton);
        actionButtonPanel.add(editButton);
        titleAndActionButtonsPanel.add(actionButtonPanel, BorderLayout.EAST);

        // Search bar panel - now on its own line
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBarPanel.setBackground(new Color(240, 240, 245));
        searchField = new JTextField(30); // Increased width for better visibility
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add DocumentListener for auto-search as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // This method is for attribute changes, not text content changes in plain text fields.
                // However, it's good practice to include it.
                performSearch();
            }
        });

        searchBarPanel.add(new JLabel("Search:"));
        searchBarPanel.add(searchField);

        // Add panels to the headerPanel vertically
        headerPanel.add(titleAndActionButtonsPanel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add some vertical space
        headerPanel.add(searchBarPanel);


        // Bottom delete button
        JPanel bottomButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        bottomButtonPanel.setBackground(new Color(240, 240, 245));
        JButton deleteButton = new JButton("Delete User");
        deleteButton.addActionListener(this::deleteUser);
        bottomButtonPanel.add(deleteButton);

        // Table setup
        String[] columnNames = {"User ID", "Username", "Role", "Email", "Phone"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setBackground(Color.WHITE);
        table.setRowHeight(30);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setShowGrid(false);

        // Set preferred column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(60);  // User ID
        table.getColumnModel().getColumn(1).setPreferredWidth(120); // Username
        table.getColumnModel().getColumn(2).setPreferredWidth(100); // Role
        table.getColumnModel().getColumn(3).setPreferredWidth(200); // Email
        table.getColumnModel().getColumn(4).setPreferredWidth(100); // Phone

        JScrollPane scrollPane = new JScrollPane(table);

        // Add to layout
        add(headerPanel, BorderLayout.NORTH); // Use the new headerPanel
        add(scrollPane, BorderLayout.CENTER);
        add(bottomButtonPanel, BorderLayout.SOUTH);

        refreshTable(null); // Load all users initially
    }

    /**
     * Triggers the search based on the text in the search field.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        refreshTable(query);
    }

    /**
     * Refreshes the user table, optionally filtering by a search query.
     * @param searchQuery The text to search for, or null/empty to show all users.
     */
    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0); // Clear table
        List<User> users = userListController.loadUsers();

        // Filter users if a search query is provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            users = users.stream()
                    .filter(user -> user.getUserId().toLowerCase().contains(lowerCaseQuery) ||
                            user.getUsername().toLowerCase().contains(lowerCaseQuery) ||
                            roleMap.getOrDefault(user.getUserRoleId(), "Unknown").toLowerCase().contains(lowerCaseQuery) ||
                            user.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                            user.getPhoneNumber().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        for (User user : users) {
            String roleName = roleMap.getOrDefault(user.getUserRoleId(), "Unknown");
            tableModel.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    roleName,
                    user.getEmail(),
                    user.getPhoneNumber()
            });
        }

        // Apply alternating row colors
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(240, 240, 240));
                } else {
                    c.setBackground(new Color(173, 216, 230)); // Light blue for selected row
                }

                return c;
            }
        });
    }

    private void addUser(ActionEvent e) {
        UserDialog dialog = new UserDialog(null, "Add New User", null, roleMap);
        if (dialog.showDialog()) {
            User newUser = dialog.getUser();
            if (userListController.addUser(newUser)) {
                refreshTable(searchField.getText().trim()); // Refresh with current search query
                JOptionPane.showMessageDialog(this, "User added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add user",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editUser(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String userId = (String) tableModel.getValueAt(selectedRow, 0);
            List<User> users = userListController.loadUsers();
            User userToEdit = users.stream()
                    .filter(u -> u.getUserId().equals(userId))
                    .findFirst()
                    .orElse(null);

            if (userToEdit != null) {
                UserDialog dialog = new UserDialog(null, "Edit User", userToEdit, roleMap);
                if (dialog.showDialog()) {
                    User updatedUser = dialog.getUser();
                    if (userListController.updateUserInFile(updatedUser)) {
                        refreshTable(searchField.getText().trim()); // Refresh with current search query
                        JOptionPane.showMessageDialog(this, "User updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update user",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteUser(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String userID = (String) tableModel.getValueAt(selectedRow, 0);

            List<User> users = userListController.loadUsers();
            User userToDelete = users.stream()
                    .filter(p -> p.getUserId().equals(userID))
                    .findFirst()
                    .orElse(null);

            if (userToDelete == null) {
                JOptionPane.showMessageDialog(this, "User not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete user: " + userToDelete.getUsername() + "?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (userListController.deleteFile(userToDelete)) {
                    refreshTable(searchField.getText().trim()); // Refresh with current search query
                    JOptionPane.showMessageDialog(this, "User deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete user",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a user to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}