package GUI;

import Management.Controller.POController;
import Management.Model.LoggedInUser;
import Management.Model.PurchaseOrder;
import Management.Model.User;
import GUI.Component.Dialog.PODialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class PurchaseOrderList extends JPanel {
    private final POController poController;
    private DefaultTableModel tableModel;
    private JTable table;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JTextField searchField;

    public PurchaseOrderList() {
        poController = new POController();
        setLayout(new BorderLayout());
        setBackground(UIManager.getColor("Panel.background"));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        JPanel titleAndActionButtonsPanel = new JPanel(new BorderLayout());
        titleAndActionButtonsPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("Purchase Order List");
        title.setFont(UIManager.getFont("Label.font").deriveFont(20f).deriveFont(Font.BOLD));
        titleAndActionButtonsPanel.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        User currentUser = LoggedInUser.getCurrentUser();

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id3")) {
            addButton = new JButton("Add PO");
            addButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            addButton.addActionListener(e -> addPO());
            buttonPanel.add(addButton);
        }

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id3") || currentUser.getUserRoleId().equals("ur_id4")) {
            editButton = new JButton("Edit PO");
            editButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            editButton.addActionListener(e -> editSelectedPO());
            buttonPanel.add(editButton);
        }

        titleAndActionButtonsPanel.add(buttonPanel, BorderLayout.EAST);

        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBarPanel.setBackground(UIManager.getColor("Panel.background"));
        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { performSearch(); }
            @Override public void removeUpdate(DocumentEvent e) { performSearch(); }
            @Override public void changedUpdate(DocumentEvent e) { performSearch(); }
        });

        searchBarPanel.add(new JLabel("Search:"));
        searchBarPanel.add(searchField);

        topPanel.add(titleAndActionButtonsPanel);
        topPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        topPanel.add(searchBarPanel);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(UIManager.getColor("Panel.background"));

        String[] columnNames = {"PO ID", "PR ID", "Quantity", "Date", "Status", "Total Amount"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        refreshTable(null);

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomPanel.setBackground(UIManager.getColor("Panel.background"));

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id3")) {
            deleteButton = new JButton("Delete PO");
            deleteButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
            deleteButton.addActionListener(e -> deleteSelectedPO());
            bottomPanel.add(deleteButton);
        }

        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private void performSearch() {
        refreshTable(searchField.getText().trim());
    }

    private void refreshTable(String searchQuery) {
        tableModel.setRowCount(0);
        List<PurchaseOrder> purchaseOrders = poController.loadPOData();

        if (searchQuery != null && !searchQuery.isEmpty()) {
            String lowerCaseQuery = searchQuery.toLowerCase();
            purchaseOrders = purchaseOrders.stream()
                    .filter(po -> po.getPo_id().toLowerCase().contains(lowerCaseQuery) ||
                            po.getPr_id().toLowerCase().contains(lowerCaseQuery) ||
                            po.getQuantity().toLowerCase().contains(lowerCaseQuery) ||
                            po.getDate().toLowerCase().contains(lowerCaseQuery) ||
                            po.getStatus().toLowerCase().contains(lowerCaseQuery) ||
                            po.getTotalAmount().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }

        for (PurchaseOrder purchaseOrder : purchaseOrders) {
            Object[] row = {
                    purchaseOrder.getPo_id(),
                    purchaseOrder.getPr_id(),
                    purchaseOrder.getQuantity(),
                    purchaseOrder.getDate(),
                    purchaseOrder.getStatus(),
                    purchaseOrder.getTotalAmount()
            };
            tableModel.addRow(row);
        }
    }

    private void addPO() {
        PODialog dialog = new PODialog(null, "Add Purchase Order", null);
        if (dialog.showDialog()) {
            PurchaseOrder newPO = dialog.getPO();
            if (poController.addPO(newPO)) {
                refreshTable(searchField.getText().trim());
                JOptionPane.showMessageDialog(this, "Purchase Order added successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add Purchase Order",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void editSelectedPO() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String poID = (String) tableModel.getValueAt(selectedRow, 0);
            PurchaseOrder purchaseOrderToEdit = poController.getPOById(poID);
            if (purchaseOrderToEdit.getStatus().equals("Approved")) {
                JOptionPane.showMessageDialog(this, "Status Approved, Purchase Order Cannot Be Edited Anymore!",
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (purchaseOrderToEdit != null) {
                PODialog dialog = new PODialog(null, "Edit Purchase Order", purchaseOrderToEdit);
                if (dialog.showDialog()) {
                    PurchaseOrder updatedPurchaseOrder = dialog.getPO();
                    if (poController.updatePOInFile(updatedPurchaseOrder)) {
                        refreshTable(searchField.getText().trim());
                        JOptionPane.showMessageDialog(this, "Purchase Order updated successfully!",
                                "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update Purchase Order",
                                "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Order to edit",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void deleteSelectedPO() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow >= 0) {
            String poIDToDelete = (String) tableModel.getValueAt(selectedRow, 0);
            PurchaseOrder purchaseOrderToDelete = poController.getPOById(poIDToDelete);

            if (purchaseOrderToDelete == null) {
                JOptionPane.showMessageDialog(this, "Purchase Order not found.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this Purchase Order?",
                    "Confirm Deletion", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                if (poController.deleteFile(purchaseOrderToDelete)) {
                    refreshTable(searchField.getText().trim());
                    JOptionPane.showMessageDialog(this, "Purchase Order deleted successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete Purchase Order",
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a Purchase Order to delete",
                    "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
}