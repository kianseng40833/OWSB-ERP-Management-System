package GUI;

import GUI.Component.Dialog.PaymentDialog;
import Management.Controller.PaymentController;
import Management.Model.LoggedInUser;
import Management.Model.Payment;
import Management.Model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors; // Import for stream operations
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener

public class PaymentInvoiceList extends JPanel {
    private final PaymentController paymentController;
    private DefaultTableModel tableModel;
    private JTable table;
//    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField searchField; // New search field

    public PaymentInvoiceList() {
        this.paymentController = new PaymentController();
        initializeUI();
        refreshTable(null); // Load all payments initially
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIManager.getColor("Panel.background")); // Set background for consistency

        // Top panel for title, search, and buttons
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS)); // Use BoxLayout for vertical stacking
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0)); // Add bottom padding
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        // Panel for title and action buttons
        JPanel titleAndActionButtonsPanel = new JPanel(new BorderLayout());
        titleAndActionButtonsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("Payment Invoices");
        title.setFont(title.getFont().deriveFont(20f).deriveFont(Font.BOLD));
        titleAndActionButtonsPanel.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0)); // Reduced vertical gap
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        User currentUser = LoggedInUser.getCurrentUser();
//        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id4")) {
//            addButton = new JButton("Add Payment");
//            addButton.addActionListener(this::addPayment);
//            buttonPanel.add(addButton);
//        }

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id4")) {
            editButton = new JButton("Edit Payment");
            editButton.addActionListener(this::editPayment);
            buttonPanel.add(editButton);
        }
        titleAndActionButtonsPanel.add(buttonPanel, BorderLayout.EAST);

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

        add(topPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {"Payment ID", "PO ID", "Payment Date", "Status", "Total Amount"}; // Changed "Date" to "PO Date" for clarity
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // Bottom panel
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10)); // Added horizontal and vertical gap
        bottomPanel.setBackground(UIManager.getColor("Panel.background")); // Set background for consistency

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id4")) {
            deleteButton = new JButton("Delete Payment");
            deleteButton.addActionListener(e -> deletePayment());
            bottomPanel.add(deleteButton);
            add(bottomPanel, BorderLayout.SOUTH);
        }
    }

    /**
     * Triggers the search based on the text in the search field.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        refreshTable(query);
    }

    /**
     * Refreshes the table with the latest payment data, optionally filtering by a search query.
     * @param searchQuery The text to search for, or null/empty to show all payments.
     */
    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0);
        List<Payment> payments = paymentController.getPaymentList();

        // Filter payments if a search query is provided
        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            payments = payments.stream()
                    .filter(payment -> payment.getPaymentId().toLowerCase().contains(lowerCaseQuery) ||
                            payment.getPo_id().toLowerCase().contains(lowerCaseQuery) ||// PO Date (assuming pr_id is PO Date)
                            payment.getDate().toLowerCase().contains(lowerCaseQuery) || // Payment Date
                            payment.getStatus().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        for (Payment payment : payments) {
            tableModel.addRow(new Object[]{
                    payment.getPaymentId(),
                    payment.getPo_id(),     // PO Date (assuming pr_id is the PO date, adjust if needed)
                    payment.getDate(),       // Payment Date
                    payment.getStatus(),
                    payment.getTotalAmount() // Add total amount
            });
        }
    }

//    private void addPayment(ActionEvent e) {
//        PaymentDialog dialog = new PaymentDialog((JFrame)SwingUtilities.getWindowAncestor(this),
//                "Add New Payment", null, paymentController);
//
//        if (dialog.showDialog()) {
//            Payment newPayment = dialog.getPayment();
//            if (paymentController.addPayment(newPayment)) {
//                refreshTable(searchField.getText().trim()); // Refresh with current search query
//                JOptionPane.showMessageDialog(this, "Payment added successfully!",
//                        "Success", JOptionPane.INFORMATION_MESSAGE);
//            } else {
//                JOptionPane.showMessageDialog(this, "Failed to add payment",
//                        "Error", JOptionPane.ERROR_MESSAGE);
//            }
//        }
//    }

    private void editPayment(ActionEvent e) {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Please select a payment to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String paymentId = (String) tableModel.getValueAt(selectedRow, 0);
        Payment paymentToEdit = paymentController.loadPaymentData().stream()
                .filter(p -> p.getPaymentId().equals(paymentId))
                .findFirst()
                .orElse(null);
        if (paymentToEdit.getStatus().equals("Completed")) {
            JOptionPane.showMessageDialog(this, "Status Completed, Payment Cannot Be Edited Anymore!",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (paymentToEdit != null) {
            PaymentDialog dialog = new PaymentDialog((JFrame)SwingUtilities.getWindowAncestor(this),
                    "Edit Payment", paymentToEdit, paymentController);

            if (dialog.showDialog()) {
                Payment updatedPayment = dialog.getPayment();
                if (paymentController.updatePaymentInFile(updatedPayment)) {
                    refreshTable(searchField.getText().trim()); // Refresh with current search query
                    JOptionPane.showMessageDialog(this, "Payment updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update payment",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void deletePayment() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String paymentID = (String) tableModel.getValueAt(selectedRow, 0);

            // Get payment from the already loaded list
            Payment paymentToDelete = paymentController.getPaymentList().stream()
                    .filter(p -> p.getPaymentId().equals(paymentID))
                    .findFirst()
                    .orElse(null);

            if (paymentToDelete == null) {
                JOptionPane.showMessageDialog(this, "Payment not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this Payment?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (paymentController.deletePayment(paymentToDelete)) {
                    refreshTable(searchField.getText().trim()); // Refresh with current search query
                    JOptionPane.showMessageDialog(this, "Payment deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete Payment",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Payment to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}
