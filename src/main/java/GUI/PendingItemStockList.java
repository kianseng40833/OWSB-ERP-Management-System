package GUI;

import GUI.Component.Dialog.SupplyItemDialog;
import GUI.Component.Snackbar;
import Management.Controller.SupplyItemController;
import Management.Model.LoggedInUser;
import Management.Model.SupplyItem;
import Management.Model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors; // Import for stream operations
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener

public class PendingItemStockList extends JPanel {
    private final SupplyItemController supplyItemController;
    private DefaultTableModel tableModel;
    private JTable table;
//    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField searchField; // New search field

    public PendingItemStockList() {
        this.supplyItemController = new SupplyItemController();
        initializeUI();
        refreshTable(null); // Load all items initially
    }

    private void initializeUI() {
        setLayout(new BorderLayout()); // Use BorderLayout for the main layout
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIManager.getColor("Panel.background"));

        // Top panel for title, search, and buttons
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        // Panel for title and action buttons
        JPanel titleAndActionButtonsPanel = new JPanel(new BorderLayout());
        titleAndActionButtonsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("Pending Item List");
        title.setFont(UIManager.getFont("Label.font").deriveFont(20f).deriveFont(Font.BOLD));
        titleAndActionButtonsPanel.add(title, BorderLayout.WEST); // Title on the left

        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Buttons on the right
        actionButtonPanel.setBackground(UIManager.getColor("Panel.background"));

        User currentUser = LoggedInUser.getCurrentUser();
        // Add button is typically for manual addition, but in our new flow, SupplyItems are created by PO approval.
        // Keeping it for now, but its primary use case might change.
//        addButton = new JButton("Add Supply Item");
//        addButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
////        addButton.addActionListener(this::addSupplyItem);
//        actionButtonPanel.add(addButton);

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id5")) {
            editButton = new JButton("Edit Supply Item");
            editButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            editButton.addActionListener(this::editSupplyItem);
            actionButtonPanel.add(editButton);
        }
        titleAndActionButtonsPanel.add(actionButtonPanel, BorderLayout.EAST);

        // Search bar panel - now on its own line
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBarPanel.setBackground(UIManager.getColor("Panel.background"));
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
                performSearch();
            }
        });

        searchBarPanel.add(new JLabel("Search:"));
        searchBarPanel.add(searchField);

        // Add panels to the headerPanel vertically
        topPanel.add(titleAndActionButtonsPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add some vertical space
        topPanel.add(searchBarPanel);

        add(topPanel, BorderLayout.NORTH); // Add topPanel to the NORTH of the main panel

        // Center panel for the table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(UIManager.getColor("Panel.background"));

        // Updated column names for clarity
        String[] columnNames = {"SupplyItem_ID", "Payment_ID", "Item_ID", "Item_Name", "Quantity Ordered" ,"Quantity Received", "Supplied Date", "Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table non-editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(0).setPreferredWidth(120); // SupplyItem_ID
        table.getColumnModel().getColumn(1).setPreferredWidth(100); // Payment_ID
        table.getColumnModel().getColumn(2).setPreferredWidth(80);  // Item_ID
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Item_Name
        table.getColumnModel().getColumn(4).setPreferredWidth(120); // Quantity Ordered
        table.getColumnModel().getColumn(5).setPreferredWidth(120); // Quantity Received
        table.getColumnModel().getColumn(6).setPreferredWidth(100); // Supplied Date
        table.getColumnModel().getColumn(7).setPreferredWidth(80);  // Status

        // Enable horizontal scrolling
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);  // Add scrollPane to the CENTER of centerPanel
        add(centerPanel, BorderLayout.CENTER); // Add centerPanel to the CENTER of the main panel

        // Bottom panel for Delete button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Delete button on the right
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id5")) {
            deleteButton = new JButton("Delete Supply Item");
            deleteButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            deleteButton.addActionListener(this::deleteSupplyItem);
            bottomPanel.add(deleteButton);
        }
        add(bottomPanel, BorderLayout.SOUTH); // Add bottomPanel to the SOUTH of the main panel
    }

    /**
     * Triggers the search based on the text in the search field.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        refreshTable(query);
    }

    /**
     * Refreshes the table with the latest supply item data, optionally filtering by a search query.
     * @param searchQuery The text to search for, or null/empty to show all supply items.
     */
    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0); // Clear table
        List<SupplyItem> supplyItemLists = supplyItemController.loadSupplyItemData();

        // Filter supply items if a search query is provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            supplyItemLists = supplyItemLists.stream()
                    .filter(item -> item.getSupplyItem_ID().toLowerCase().contains(lowerCaseQuery) ||
                            item.getPayment_ID().toLowerCase().contains(lowerCaseQuery) ||
                            item.getItem_ID().toLowerCase().contains(lowerCaseQuery) ||
                            item.getItem_Name().toLowerCase().contains(lowerCaseQuery) ||
                            item.getQuantityOrdered().toLowerCase().contains(lowerCaseQuery) || // Updated field name
                            item.getQuantityReceived().toLowerCase().contains(lowerCaseQuery) || // Updated field name
                            item.getSuppliedDate().toLowerCase().contains(lowerCaseQuery) ||
                            item.getStatus().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        for (SupplyItem supplyItemList : supplyItemLists) {
            tableModel.addRow(new Object[]{
                    supplyItemList.getSupplyItem_ID(),
                    supplyItemList.getPayment_ID(),
                    supplyItemList.getItem_ID(),
                    supplyItemList.getItem_Name(),
                    supplyItemList.getQuantityOrdered(), // Updated field name
                    supplyItemList.getQuantityReceived(), // Updated field name
                    supplyItemList.getSuppliedDate(),
                    supplyItemList.getStatus()
            });
        }
    }

//    private void addSupplyItem(ActionEvent e) {
//        // In the new flow, SupplyItems are primarily created automatically upon PO approval.
//        // This manual "Add" might be for exceptional cases or initial setup.
//        SupplyItemDialog dialog = new SupplyItemDialog(null, "Add Supply Item", null);
//        if (dialog.showDialog()) {
//            SupplyItem newSupplyItem = dialog.getSupplyItem();
//
//            // Set initial status if it's a manual add and not already set
//            if (newSupplyItem.getStatus() == null || newSupplyItem.getStatus().isEmpty()) {
//                newSupplyItem.setStatus("Pending Delivery"); // Default status for manually added
//            }
//
//            if (supplyItemController.addSupplyItem(newSupplyItem)) {
//                refreshTable(searchField.getText().trim()); // Refresh with current search query
//                Snackbar.showSuccess(this, "Supply Item added successfully!");
//            } else {
//                Snackbar.showError(this, "Failed to add Supply Item");
//            }
//        }
//    }

    private void editSupplyItem(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String supplyItemID = (String) tableModel.getValueAt(selectedRow, 0);
            List<SupplyItem> supplyItems = supplyItemController.loadSupplyItemData(); // Load fresh data
            SupplyItem supplyItemToEdit = supplyItems.stream()
                    .filter(u -> u.getSupplyItem_ID().equals(supplyItemID))
                    .findFirst()
                    .orElse(null);
            if (supplyItemToEdit.getStatus().equals("Completed")) {
                JOptionPane.showMessageDialog(this, "Status Completed, Supply Item Cannot Be Edited Anymore!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (supplyItemToEdit != null) {
                SupplyItemDialog dialog = new SupplyItemDialog(null, "Edit Supply Item", supplyItemToEdit);
                if (dialog.showDialog()) {
                    SupplyItem updatedSupplyItem = dialog.getSupplyItem();
                    if (supplyItemController.updateSupplyItemInFile(updatedSupplyItem)) {
                        refreshTable(searchField.getText().trim()); // Refresh with current search query
                        Snackbar.showSuccess(this, "Supply Item updated successfully!");
                    } else {
                        Snackbar.showError(this, "Failed to update Supply Item");
                    }
                }
            }
        } else {
            Snackbar.showInfo(this, "Please select a Supply Item to edit");
        }
    }

    private void deleteSupplyItem(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String supplyItemID = (String) tableModel.getValueAt(selectedRow, 0);

            // 🔍 Look up the full SupplyItem object by ID
            List<SupplyItem> supplyItems = supplyItemController.loadSupplyItemData(); // Load fresh data
            SupplyItem supplyItemToDelete = supplyItems.stream()
                    .filter(p -> p.getSupplyItem_ID().equals(supplyItemID))
                    .findFirst()
                    .orElse(null);

            if (supplyItemToDelete == null) {
                JOptionPane.showMessageDialog(this, "Supply Item not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this Supply Item?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (supplyItemController.deleteFile(supplyItemToDelete)) {
                    refreshTable(searchField.getText().trim()); // Refresh with current search query
                    Snackbar.showSuccess(this, "Supply Item deleted successfully!");
                } else {
                    Snackbar.showError(this, "Failed to delete Supply Item");
                }
            }
        } else {
            Snackbar.showInfo(this, "Please select a Supply Item to delete");
        }
    }
}
