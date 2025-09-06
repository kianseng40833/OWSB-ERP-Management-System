package GUI.Component.Dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

import Management.Controller.ItemController;
import Management.Model.*;
import Management.Controller.SupplierController;
import Management.Controller.PRController;

public class PRDialog extends JDialog {
    private final JTextField prIdField;
    private final JComboBox<Item> itemNameComboBox;
//    private final JTextField itemIDNameField;
    private final JTextField stockField; // This is the Current Stock field
    private final JComboBox<Supplierlist> supplierComboBox;
    private final JFormattedTextField dateField;
    private final JTextField picField;
    private final JComboBox<String> statusComboBox;
    private final JTextField statusField;
    private final JTextField remarksField;
    private boolean confirmed = false;
    private final ItemController itemController;
    private final SupplierController supplierController;
    private final PRController prController;
    private final JDialog dialog;
    private PurchaseRequisition initialPRToEdit; // To check if it's a new PR or edit
    User currentUser = LoggedInUser.getCurrentUser();
    public PRDialog(JFrame parent, String title, PurchaseRequisition prToEdit) {
        super(parent, title, true);
        this.dialog = this;
        this.initialPRToEdit = prToEdit; // Store the initial PR state
        setSize(550, 450);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        itemController = new ItemController();
        supplierController = new SupplierController();
        prController = new PRController();

        // Initialize components
        prIdField = new JTextField();
        prIdField.setEditable(false);
        itemNameComboBox = new JComboBox<>();
//        itemIDNameField = new JTextField();
//        itemIDNameField.setEditable(false);
        stockField = new JTextField();
        stockField.setEditable(false);
        supplierComboBox = new JComboBox<>();
        dateField = new JFormattedTextField(new SimpleDateFormat("yyyy-MM-dd"));
        picField = new JTextField();
        statusField = new JTextField();
        statusField.setEditable(false);
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        remarksField = new JTextField();

        // Load data into combo boxes
        loadItemsToComboBox();
        loadSupplierToComboBox();

        // Set up item selection listener BEFORE populating fields to ensure it triggers correctly
        itemNameComboBox.addActionListener(e -> updateSuppliersAndStockComboBox());

        JPanel formPanel = createFormPanel(prToEdit);
        JPanel buttonPanel = createButtonPanel();

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Initialize fields based on whether it's a new PR or editing an existing one
        if (prToEdit == null) {
            String newPrId = prController.generateNewPrId();
            prIdField.setText(newPrId);
            dateField.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            statusComboBox.setSelectedItem("Pending");

            if (itemNameComboBox.getItemCount() > 0) {
                itemNameComboBox.setSelectedIndex(0);
            } else {
                stockField.setText("0");
            }
        } else {
            populateFields(prToEdit);
        }
    }

    private JPanel createFormPanel(PurchaseRequisition pr) {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(null);
        formPanel.setPreferredSize(new Dimension(550, 380));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        int labelX = 30;
        int fieldX = 140;
        int y = 20;
        int height = 25;
        int spacing = 40;

        JLabel prIdLabel = new JLabel("PR ID:");
        prIdLabel.setBounds(labelX, y, 100, height);
        prIdField.setBounds(fieldX, y, 350, height);
        formPanel.add(prIdLabel);
        formPanel.add(prIdField);

//        if (pr != null) {
//            y += spacing;
//            JLabel itemIDNameLabel = new JLabel("Item ID:");
//            itemIDNameLabel.setBounds(labelX, y, 100, height);
//            itemIDNameField.setBounds(fieldX, y, 350, height);
//            formPanel.add(itemIDNameLabel);
//            formPanel.add(itemIDNameField);
//        } else {
            y += spacing;
            JLabel itemNameLabel = new JLabel("Item ID:");
            itemNameLabel.setBounds(labelX, y, 100, height);
            itemNameComboBox.setBounds(fieldX, y, 350, height);
            formPanel.add(itemNameLabel);
            formPanel.add(itemNameComboBox);
//        }

        y += spacing;
        JLabel stockLabel = new JLabel("Current Stock:");
        stockLabel.setBounds(labelX, y, 100, height);
        stockField.setBounds(fieldX, y, 350, height);
        formPanel.add(stockLabel);
        formPanel.add(stockField);

        y += spacing;
        JLabel supplierLabel = new JLabel("Supplier:");
        supplierLabel.setBounds(labelX, y, 100, height);
        supplierComboBox.setBounds(fieldX, y, 350, height);
        formPanel.add(supplierLabel);
        formPanel.add(supplierComboBox);

        y += spacing;
        JLabel dateLabel = new JLabel("Date:");
        dateLabel.setBounds(labelX, y, 100, height);
        dateField.setBounds(fieldX, y, 350, height);
        formPanel.add(dateLabel);
        formPanel.add(dateField);

        y += spacing;
        JLabel picLabel = new JLabel("PIC:");
        picLabel.setBounds(labelX, y, 100, height);
        picField.setBounds(fieldX, y, 350, height);
        User currentUser = LoggedInUser.getCurrentUser();
        picField.setText(currentUser.getUsername());
        picField.setEditable(false);
        formPanel.add(picLabel);
        formPanel.add(picField);

        if (pr != null) {
            if (currentUser.getUserRoleId().equals("ur_id2")) {
                y += spacing;
                JLabel statusLabel = new JLabel("Status:");
                statusLabel.setBounds(labelX, y, 100, height);
                statusField.setBounds(fieldX, y, 350, height);
                formPanel.add(statusLabel);
                formPanel.add(statusField);
            } else {
                y += spacing;
                JLabel statusLabel = new JLabel("Status:");
                statusLabel.setBounds(labelX, y, 100, height);
                statusComboBox.setBounds(fieldX, y, 350, height);
                formPanel.add(statusLabel);
                formPanel.add(statusComboBox);
            }
        }

        y += spacing;
        JLabel remarksLabel = new JLabel("Remarks:");
        remarksLabel.setBounds(labelX, y, 100, height);
        remarksField.setBounds(fieldX, y, 350, height);
        formPanel.add(remarksLabel);
        formPanel.add(remarksField);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(100, 30));

        okButton.addActionListener(e -> validateAndClose(e));
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void populateFields(PurchaseRequisition pr) {
        prIdField.setText(pr.getPRID());
        dateField.setText(pr.getDate());
        picField.setText(pr.getPic());
        stockField.setText(pr.getStock());
        statusField.setText(pr.getStatus());
        statusComboBox.setSelectedItem(pr.getStatus());
        remarksField.setText(pr.getRemarks());
//        Item itemList = itemController.getItemById(pr.getItem_id());
//        itemIDNameField.setText(pr.getItem_id() + " - " + itemList.getItemName());

        for (int i = 0; i < itemNameComboBox.getItemCount(); i++) {
            if (itemNameComboBox.getItemAt(i).getItemID().equals(pr.getItem_id())) {
                itemNameComboBox.setSelectedIndex(i);
                break;
            }
        }

        // Set selected supplier (this will happen after updateSuppliersAndStockComboBox() runs)
        for (int i = 0; i < supplierComboBox.getItemCount(); i++) {
            if (supplierComboBox.getItemAt(i).getSupplierID().equals(pr.getSupplier_id())) {
                supplierComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    private void loadItemsToComboBox() {
        itemNameComboBox.removeAllItems();
        List<Item> items = itemController.getItemList();
        if (items != null) {
            items.forEach(itemNameComboBox::addItem);
        }
    }

    private void loadSupplierToComboBox() {
        supplierComboBox.removeAllItems();
        List<Supplierlist> suppliers = supplierController.getSupplierList();
        if (suppliers != null) {
            suppliers.forEach(supplierComboBox::addItem);
        }
    }

    private void updateSuppliersAndStockComboBox() {
        supplierComboBox.removeAllItems();
        Item selectedItem = (Item) itemNameComboBox.getSelectedItem();

        if (selectedItem != null) {
            stockField.setText(String.valueOf(selectedItem.getQuantity()));

            List<Supplierlist> suppliers = supplierController.getSupplierList().stream()
                    .filter(supplier -> supplierHasItem(supplier, selectedItem.getItemID()))
                    .collect(Collectors.toList());

            if (!suppliers.isEmpty()) {
                suppliers.forEach(supplierComboBox::addItem);
            } else {
                supplierComboBox.addItem(new Supplierlist("", "No suppliers for this item", "", "", "",
                        selectedItem.getItemID(), selectedItem.getItemName(), 0.0, ""));
            }
        } else {
            stockField.setText("");
            supplierComboBox.removeAllItems();
            supplierComboBox.addItem(new Supplierlist("", "Select an Item First", "", "", "", "", "", 0.0, ""));
        }
    }

    private boolean supplierHasItem(Supplierlist supplier, String itemId) {
        if (supplier == null || supplier.getItemID() == null) {
            return false;
        }
        return supplier.getItemID().equals(itemId);
    }

    private void validateAndClose(ActionEvent e) {
        if (validateInput()) {
            confirmed = true;
            dialog.dispose();
        }
    }

    private boolean validateInput() {
        if (dateField.getText().trim().isEmpty() ||
                picField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Please fill in all required fields (Date, PIC)", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (itemNameComboBox.getSelectedItem() == null || ((Item)itemNameComboBox.getSelectedItem()).getItemID().isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Please select an Item ID.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Supplierlist selectedSupplier = (Supplierlist) supplierComboBox.getSelectedItem();
        if (selectedSupplier == null || selectedSupplier.getSupplierID().isEmpty()) {
            JOptionPane.showMessageDialog(dialog,
                    "Please select a valid Supplier.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Integer.parseInt(stockField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog,
                    "Current Stock must be a number", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            LocalDate.parse(dateField.getText(), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(dialog,
                    "Invalid date format (YYYY-MM-DD)", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        return true;
    }

    public boolean showDialog() {
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.setVisible(true);
        return confirmed;
    }

    public PurchaseRequisition getPR() {
        Item selectedItem = (Item) itemNameComboBox.getSelectedItem();
        Supplierlist selectedSupplier = (Supplierlist) supplierComboBox.getSelectedItem();

        PurchaseRequisition pr = new PurchaseRequisition(
                prIdField.getText(),
                selectedItem != null ? selectedItem.getItemID() : "",
                stockField.getText(),
                selectedSupplier != null ? selectedSupplier.getSupplierID() : "",
                dateField.getText(),
                picField.getText(),
                statusComboBox.getSelectedItem().toString(),
                remarksField.getText()
        );

        return pr;
    }
}