package GUI;

import Management.Controller.SupplierController;
import Management.Model.LoggedInUser;
import Management.Model.Supplierlist;
import GUI.Component.Dialog.SupplierDialog;
import Management.Model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors; // Import for stream operations
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener

public class SupplierLists extends JPanel {
    private final SupplierController supplierController;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField searchField; // New search field
    User currentUser = LoggedInUser.getCurrentUser();
    public SupplierLists() {
        supplierController = new SupplierController();
        supplierController.loadSuppliers();

        setLayout(new BorderLayout()); // Use BorderLayout for overall layout
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel for title, search, and buttons
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Add bottom padding
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        // Panel for title and action buttons
        JPanel titleAndActionButtonsPanel = new JPanel(new BorderLayout());
        titleAndActionButtonsPanel.setBackground(UIManager.getColor("Panel.background"));

        // Title Label
        JLabel title = new JLabel("Supplier List");
        title.setFont(UIManager.getFont("Label.font").deriveFont(20f).deriveFont(Font.BOLD));
        titleAndActionButtonsPanel.add(title, BorderLayout.WEST); // Title on the left

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Buttons on the right, reduced vertical gap
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id2")) {
            addButton = new JButton("Add Supplier");
            addButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            addButton.addActionListener(e -> showSupplierForm(null)); // Pass null for adding
            buttonPanel.add(addButton);

            editButton = new JButton("Edit Supplier");
            editButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            editButton.addActionListener(e -> editSelectedSupplier());
            buttonPanel.add(editButton);
        }
        titleAndActionButtonsPanel.add(buttonPanel, BorderLayout.EAST); // Buttons panel on the right of topPanel

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

        add(topPanel, BorderLayout.NORTH); // Add the combined header to NORTH

        // Center panel for the table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(UIManager.getColor("Panel.background"));

        String[] columnNames = {
                "Supplier ID", "Company Name", "Contact", "Email", "PIC", "Item ID", "Item Name", "Cost", "Status"
        };

        tableModel = new DefaultTableModel(columnNames, 0);
        refreshTable(null); // Load all suppliers initially

        table = new JTable(tableModel); // Initialize the table instance variable
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Ensure single selection

        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(300);
        columnModel.getColumn(2).setPreferredWidth(150);
        columnModel.getColumn(3).setPreferredWidth(300);
        columnModel.getColumn(4).setPreferredWidth(120);
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(6).setPreferredWidth(100);
        columnModel.getColumn(7).setPreferredWidth(100);
        columnModel.getColumn(8).setPreferredWidth(100); // Added width for Status column

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER); // Add scrollPane to the center panel

        // Bottom panel for the delete button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Added horizontal and vertical gap
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));

        deleteButton = new JButton("Delete Supplier");
        deleteButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
        deleteButton.addActionListener(e -> deleteSelectedSupplier());
        bottomPanel.add(deleteButton);

        add(centerPanel, BorderLayout.CENTER); // Add center panel to the CENTER
        add(bottomPanel, BorderLayout.SOUTH); // Add bottom panel to the SOUTH
    }

    /**
     * Triggers the search based on the text in the search field.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        refreshTable(query);
    }

    /**
     * Refreshes the table with the latest supplier data, optionally filtering by a search query.
     * @param searchQuery The text to search for, or null/empty to show all suppliers.
     */
    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0);
        List<Supplierlist> suppliers = supplierController.getSupplierList();

        // Filter suppliers if a search query is provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            suppliers = suppliers.stream()
                    .filter(supplier -> supplier.getSupplierID().toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getSupplierCompanyName().toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getContact().toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getEmail().toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getPIC().toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getItemID().toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getItemName().toLowerCase().contains(lowerCaseQuery) ||
                                        String.valueOf(supplier.getCost()).toLowerCase().contains(lowerCaseQuery) ||
                                        supplier.getStatus().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        for (Supplierlist supplier : suppliers) {
            Object[] row = {
                    supplier.getSupplierID(),
                    supplier.getSupplierCompanyName(),
                    supplier.getContact(),
                    supplier.getEmail(),
                    supplier.getPIC(),
                    supplier.getItemID(),
                    supplier.getItemName(),
                    supplier.getCost(),
                    supplier.getStatus()
            };
            tableModel.addRow(row);
        }
    }

    private void showSupplierForm(Supplierlist supplierToEdit) {
        JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
        String title = (supplierToEdit == null) ? "Add New Supplier" : "Edit Supplier";
        SupplierDialog supplierDialog = new SupplierDialog(parentFrame, title, supplierToEdit);
        boolean saved = supplierDialog.showDialog();

        if (saved) {
            // FIX: SupplierDialog.getSupplierData() now returns a Supplierlist object directly
            Supplierlist newOrUpdatedSupplier = supplierDialog.getSupplierData();
            
            if (newOrUpdatedSupplier != null) {
                if (supplierToEdit == null) {
                    supplierController.addSupplier(newOrUpdatedSupplier);
                } else {
                    supplierController.updateSupplier(newOrUpdatedSupplier);
                }
                refreshTable(searchField.getText().trim()); // Refresh with current search query
            } else {
                // This case should ideally be prevented by SupplierDialog's validation,
                // but added for robustness.
                JOptionPane.showMessageDialog(parentFrame,
                        "Failed to get supplier data from dialog. Please check input.",
                        "Data Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedSupplier() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String supplierID = (String) table.getValueAt(selectedRow, 0);
            Supplierlist supplierToEdit = supplierController.getSupplier(supplierID);
            if (supplierToEdit != null) {
                showSupplierForm(supplierToEdit);
            } else {
                JOptionPane.showMessageDialog(this, "Error loading supplier for editing.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void deleteSelectedSupplier() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String supplierIDToDelete = (String) table.getValueAt(selectedRow, 0);
            int confirmation = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete supplier with ID: " + supplierIDToDelete + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );
            if (confirmation == JOptionPane.YES_OPTION) {
                supplierController.deleteSupplier(supplierIDToDelete);
                refreshTable(searchField.getText().trim()); // Refresh with current search query
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.", "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}
