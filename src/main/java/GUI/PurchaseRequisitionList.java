package GUI;

import Management.Controller.PRController;
import Management.Model.LoggedInUser;
import Management.Model.PurchaseRequisition;
import javax.swing.*;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import GUI.Component.Dialog.PRDialog; // Import the PRForm class
import Management.Model.User;
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener
import java.util.stream.Collectors; // Import for stream operations

public class PurchaseRequisitionList extends JPanel {
    private final PRController prController;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton itemEntryButton;
    private JFrame parentFrame;
    private JTextField searchField; // New search field

    public PurchaseRequisitionList() {
        this.prController = new PRController();
        initializeUI();
        refreshTable(null); // Load all PRs initially
    }

    private void initializeUI() {
        setLayout(new BorderLayout()); // Use BorderLayout for the main panel
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);

        // Top panel containing title, search, and action buttons
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        headerPanel.setBackground(UIManager.getColor("Panel.background"));

        // Panel for title and action buttons
        JPanel titleAndActionButtonsPanel = new JPanel(new BorderLayout());
        titleAndActionButtonsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("Purchase Requisition List");
        title.setFont(UIManager.getFont("Label.font").deriveFont(20f).deriveFont(javax.swing.plaf.FontUIResource.BOLD));
        titleAndActionButtonsPanel.add(title, BorderLayout.WEST);

        // Button Panel for Add and Edit buttons
        JPanel actionButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT)); // Buttons on the right
        actionButtonPanel.setBackground(UIManager.getColor("Panel.background"));

        User currentUser = LoggedInUser.getCurrentUser();
        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id2")) {
//            itemEntryButton = new JButton("Item Entry Lists");
//            itemEntryButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
//            itemEntryButton.addActionListener(e -> showItemEntry());
//            actionButtonPanel.add(itemEntryButton);

            addButton = new JButton("Add PR");
            addButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            addButton.addActionListener(e -> addPRForm());
            actionButtonPanel.add(addButton);
        }

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id2") || currentUser.getUserRoleId().equals("ur_id3")) {
            editButton = new JButton("Edit PR");
            editButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            editButton.addActionListener(e -> editSelectedPR());
            actionButtonPanel.add(editButton);
        }
        titleAndActionButtonsPanel.add(actionButtonPanel, BorderLayout.EAST);

        // Search bar panel - now on its own line
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBarPanel.setBackground(UIManager.getColor("Panel.background"));
        searchField = new JTextField(30); // Increased width for better visibility
        searchField.setFont(UIManager.getFont("TextField.font").deriveFont(14f));

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
                performSearch();
            }
        });

        searchBarPanel.add(new JLabel("Search:"));
        searchBarPanel.add(searchField);

        // Add panels to the headerPanel vertically
        headerPanel.add(titleAndActionButtonsPanel);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 10))); // Add some vertical space
        headerPanel.add(searchBarPanel);


        // Center panel for the table
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(UIManager.getColor("Panel.background"));

        String[] columnNames = {
                "PR_ID", "Item_id", "Stock", "Supplier_ID", "Date", "PIC", "Status", "Remarks"
        };

        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table cells non-editable
            }
        };
        refreshTable(null); // Initial load of all data

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) table.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(100);
        columnModel.getColumn(3).setPreferredWidth(100);
        columnModel.getColumn(4).setPreferredWidth(100);
        columnModel.getColumn(5).setPreferredWidth(100);
        columnModel.getColumn(6).setPreferredWidth(100); // Status column
        columnModel.getColumn(7).setPreferredWidth(200); // Remarks column

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER); // Add scrollPane to the center panel

        // Bottom panel for the delete button
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT)); // Delete button on the right
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id2")) {
            deleteButton = new JButton("Delete PR");
            deleteButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            deleteButton.addActionListener(e -> deleteSelectedPR());
            bottomPanel.add(deleteButton);
        }
        add(headerPanel, BorderLayout.NORTH); // Add header panel to the NORTH
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
     * Refreshes the PR table, optionally filtering by a search query.
     * @param searchQuery The text to search for, or null/empty to show all PRs.
     */
    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0); // Clear table
        // Changed from loadPRData() to getPRList()
        List<PurchaseRequisition> purchaseRequisitions = prController.getPRList();

        // Filter PRs if a search query is provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            purchaseRequisitions = purchaseRequisitions.stream()
                    .filter(pr -> pr.getPRID().toLowerCase().contains(lowerCaseQuery) ||
                            pr.getItem_id().toLowerCase().contains(lowerCaseQuery) ||
                            pr.getSupplier_id().toLowerCase().contains(lowerCaseQuery) ||
                            pr.getPic().toLowerCase().contains(lowerCaseQuery) ||
                            pr.getStatus().toLowerCase().contains(lowerCaseQuery) ||
                            pr.getRemarks().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        for (PurchaseRequisition purchaseRequisition : purchaseRequisitions) {
            Object[] row = {
                    purchaseRequisition.getPRID(),
                    purchaseRequisition.getItem_id(),
                    purchaseRequisition.getStock(),
                    purchaseRequisition.getSupplier_id(),
                    purchaseRequisition.getDate(),
                    purchaseRequisition.getPic(),
                    purchaseRequisition.getStatus(),
                    purchaseRequisition.getRemarks(),
            };
            tableModel.addRow(row);
        }
    }

    private void addPRForm() {
        PRDialog dialog = new PRDialog(null, "Add Purchase Requisition", null);
        if (dialog.showDialog()) {
            PurchaseRequisition newPR = dialog.getPR();
            if (prController.addPR(newPR)) {
                refreshTable(searchField.getText().trim()); // Refresh with current search query
                JOptionPane.showMessageDialog(this, "Purchase Requisition added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add Purchase Requisition",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedPR() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String prID = (String) tableModel.getValueAt(selectedRow, 0);
            // Changed from loadPRData() to getPRList()
            List<PurchaseRequisition> purchaseRequisitions = prController.getPRList();
            PurchaseRequisition purchaseRequisitionToEdit = purchaseRequisitions.stream()
                    .filter(u -> u.getPRID().equals(prID))
                    .findFirst()
                    .orElse(null);
            if (purchaseRequisitionToEdit.getStatus().equals("Approved")) {
                JOptionPane.showMessageDialog(this, "Status Approved, Purchase Requisition Cannot Be Edited Anymore!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (purchaseRequisitionToEdit != null) {
                PRDialog dialog = new PRDialog(null, "Edit Purchase Requisition", purchaseRequisitionToEdit);
                if (dialog.showDialog()) {
                    PurchaseRequisition updatedPurchaseRequisition = dialog.getPR();
                    if (prController.updatePRInFile(updatedPurchaseRequisition)) {
                        refreshTable(searchField.getText().trim()); // Refresh with current search query
                        JOptionPane.showMessageDialog(this, "Purchase Requisition updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update Purchase Requisition",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Requisition to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedPR() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String prID = (String) tableModel.getValueAt(selectedRow, 0);

            // Changed from loadPRData() to getPRList()
            List<PurchaseRequisition> purchaseRequisitions = prController.getPRList();
            PurchaseRequisition purchaseRequisitionToDelete = purchaseRequisitions.stream()
                    .filter(p -> p.getPRID().equals(prID))
                    .findFirst()
                    .orElse(null);

            if (purchaseRequisitionToDelete == null) {
                JOptionPane.showMessageDialog(this, "Purchase Requisition not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this Purchase Requisition?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (prController.deleteFile(purchaseRequisitionToDelete)) {
                    refreshTable(searchField.getText().trim()); // Refresh with current search query
                    JOptionPane.showMessageDialog(this, "Purchase Requisition deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete Purchase Requisition",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Requisition to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void refreshData() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshData'");
    }
}
