package GUI;

import Management.Controller.ItemController;
import Management.Controller.SalesController;
import Management.Model.Item;
import Management.Model.Sales;
import Management.Model.LoggedInUser; // To get the current user for PIC or other details
import Management.Model.User; // Import User model to get username
import com.toedter.calendar.JDateChooser; // Import JDateChooser

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableColumnModel;
import java.awt.*;
import java.text.SimpleDateFormat; // For formatting JDateChooser output
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.Date; // For JDateChooser
import java.util.List;
import java.util.Vector;

public class SalesEntry extends JPanel {

    private ItemController itemController;
    private SalesController salesController;

    // UI Components for Sales Entry Form
    private JComboBox<Item> itemComboBox;
    private JTextField quantityField;
    private JLabel unitPriceLabel;
    private JLabel totalPriceLabel;
    private JButton addSaleButton;
    private JTextField customerNameField;
    private JDateChooser saleDateChooser; // Changed from JTextField to JDateChooser

    // UI Components for Sales List Table
    private JTable salesTable;
    private DefaultTableModel tableModel;
    private JButton deleteSaleButton;

    // New UI Components for Sales Summary
    private JLabel dailySalesLabel;
    private JLabel monthlySalesLabel;
    private JLabel yearlySalesLabel;

    public SalesEntry() {
        itemController = new ItemController();
        salesController = new SalesController();

        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        setBackground(new Color(0, 33, 71));

        // --- Sales Summary Panel ---
        JPanel salesSummaryPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        salesSummaryPanel.setBackground(new Color(1, 16, 30));
        salesSummaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        dailySalesLabel = createSummaryLabel("Daily Sales: RM 0.00");
        monthlySalesLabel = createSummaryLabel("Monthly Sales: RM 0.00");
        yearlySalesLabel = createSummaryLabel("Yearly Sales: RM 0.00");

        salesSummaryPanel.add(dailySalesLabel);
        salesSummaryPanel.add(monthlySalesLabel);
        salesSummaryPanel.add(yearlySalesLabel);

        add(salesSummaryPanel, BorderLayout.NORTH);

        // --- Sales Entry Form Panel ---
        JPanel mainContentPanel = new JPanel(new BorderLayout(10, 10));
        mainContentPanel.setBackground(new Color(0, 33, 71));

        JPanel entryFormPanel = new JPanel(new GridBagLayout());
        entryFormPanel.setBackground(new Color(1, 16, 30));
        entryFormPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Add New Sale",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                Color.WHITE
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Item Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        entryFormPanel.add(createLabel("Select Item:"), gbc);
        gbc.gridx = 1;
        itemComboBox = new JComboBox<>();
        itemComboBox.addActionListener(e -> updatePrices());
        entryFormPanel.add(itemComboBox, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 1;
        entryFormPanel.add(createLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        quantityField = new JTextField("1");
        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { updatePrices(); }
            @Override
            public void removeUpdate(DocumentEvent e) { updatePrices(); }
            @Override
            public void changedUpdate(DocumentEvent e) { updatePrices(); }
        });
        entryFormPanel.add(quantityField, gbc);

        // Unit Price Display
        gbc.gridx = 0;
        gbc.gridy = 2;
        entryFormPanel.add(createLabel("Unit Price:"), gbc);
        gbc.gridx = 1;
        unitPriceLabel = createLabel("RM 0.00");
        entryFormPanel.add(unitPriceLabel, gbc);

        // Total Price Display
        gbc.gridx = 0;
        gbc.gridy = 3;
        entryFormPanel.add(createLabel("Total Price:"), gbc);
        gbc.gridx = 1;
        totalPriceLabel = createLabel("RM 0.00");
        entryFormPanel.add(totalPriceLabel, gbc);

        // Sale Date (JDateChooser)
        gbc.gridx = 0;
        gbc.gridy = 4;
        entryFormPanel.add(createLabel("Sale Date:"), gbc);
        gbc.gridx = 1;
        saleDateChooser = new JDateChooser(new Date()); // Initialize with current date
        saleDateChooser.setDateFormatString("yyyy-MM-dd"); // Set date format
        entryFormPanel.add(saleDateChooser, gbc);

        // Customer Name
        gbc.gridx = 0;
        gbc.gridy = 5;
        entryFormPanel.add(createLabel("Customer Name:"), gbc);
        gbc.gridx = 1;
        customerNameField = new JTextField();
        entryFormPanel.add(customerNameField, gbc);

        // Add Sale Button
        gbc.gridx = 0;
        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        addSaleButton = new JButton("Add Sale");
        addSaleButton.setFont(new Font("Arial", Font.BOLD, 14));
        addSaleButton.setBackground(new Color(0, 100, 0));
        addSaleButton.setForeground(Color.WHITE);
        addSaleButton.setFocusPainted(false);
        addSaleButton.addActionListener(e -> addSale());
        entryFormPanel.add(addSaleButton, gbc);

        mainContentPanel.add(entryFormPanel, BorderLayout.NORTH);

        // --- Sales List Panel ---
        JPanel salesListPanel = new JPanel(new BorderLayout());
        salesListPanel.setBackground(new Color(1, 16, 30));
        salesListPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY),
                "Recent Sales",
                javax.swing.border.TitledBorder.LEFT,
                javax.swing.border.TitledBorder.TOP,
                new Font("Arial", Font.BOLD, 16),
                Color.WHITE
        ));

        String[] columnNames = {"Receipt No.", "Date", "Item ID", "Item Name", "Quantity", "Unit Price", "Total Price", "Sales PIC", "Customer Name"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        salesTable = new JTable(tableModel);
        salesTable.setFillsViewportHeight(true);
        salesTable.setRowHeight(25);
        salesTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        DefaultTableColumnModel columnModel = (DefaultTableColumnModel) salesTable.getColumnModel();
        columnModel.getColumn(0).setPreferredWidth(100);
        columnModel.getColumn(1).setPreferredWidth(100);
        columnModel.getColumn(2).setPreferredWidth(80);
        columnModel.getColumn(3).setPreferredWidth(150);
        columnModel.getColumn(4).setPreferredWidth(70);
        columnModel.getColumn(5).setPreferredWidth(90);
        columnModel.getColumn(6).setPreferredWidth(100);
        columnModel.getColumn(7).setPreferredWidth(120);
        columnModel.getColumn(8).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(salesTable);
        salesListPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(salesListPanel.getBackground());
        deleteSaleButton = new JButton("Delete Selected Sale");
        deleteSaleButton.setFont(new Font("Arial", Font.BOLD, 14));
        deleteSaleButton.setBackground(new Color(150, 0, 0));
        deleteSaleButton.setForeground(Color.WHITE);
        deleteSaleButton.setFocusPainted(false);
        deleteSaleButton.addActionListener(e -> deleteSelectedSale());
        deleteSaleButton.setEnabled(false);
        buttonPanel.add(deleteSaleButton);
        salesListPanel.add(buttonPanel, BorderLayout.SOUTH);

        salesTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                deleteSaleButton.setEnabled(salesTable.getSelectedRow() != -1);
            }
        });

        mainContentPanel.add(salesListPanel, BorderLayout.CENTER);
        add(mainContentPanel, BorderLayout.CENTER);

        populateItemComboBox();
        updatePrices();
        refreshSalesTable();
        updateSalesSummary();
    }

    @Override
    public void setVisible(boolean aFlag) {
        super.setVisible(aFlag);
        if (aFlag) {
            refreshSalesTable();
            updateSalesSummary();
        }
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.PLAIN, 14));
        return label;
    }

    private JLabel createSummaryLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Arial", Font.BOLD, 13));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1),
                BorderFactory.createEmptyBorder(10, 5, 10, 5)
        ));
        return label;
    }

    private void populateItemComboBox() {
        itemComboBox.removeAllItems();
        List<Item> items = itemController.getItemList();
        if (items.isEmpty()) {
            itemComboBox.addItem(null);
            itemComboBox.setEnabled(false);
            if (addSaleButton != null) {
                addSaleButton.setEnabled(false);
            }
        } else {
            for (Item item : items) {
                itemComboBox.addItem(item);
            }
            itemComboBox.setEnabled(true);
            if (addSaleButton != null) {
                addSaleButton.setEnabled(true);
            }
        }
    }

    private void updatePrices() {
        Item selectedItem = (Item) itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            unitPriceLabel.setText("RM 0.00");
            totalPriceLabel.setText("RM 0.00");
            if (addSaleButton != null) {
                addSaleButton.setEnabled(false);
            }
            return;
        }

        unitPriceLabel.setText(String.format("RM %.2f", selectedItem.getPrice()));

        int quantity = 0;
        String quantityText = quantityField.getText().trim();

        if (quantityText.isEmpty()) {
            totalPriceLabel.setText("RM 0.00");
            if (addSaleButton != null) {
                addSaleButton.setEnabled(false);
            }
            return;
        }

        try {
            quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                totalPriceLabel.setText("RM 0.00");
                if (addSaleButton != null) {
                    addSaleButton.setEnabled(false);
                }
                return;
            }
            if (quantity > selectedItem.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "Quantity exceeds available stock (" + selectedItem.getQuantity() + ").",
                        "Insufficient Stock Warning",
                        JOptionPane.WARNING_MESSAGE);
            }

        } catch (NumberFormatException ex) {
            totalPriceLabel.setText("RM 0.00");
            if (addSaleButton != null) {
                addSaleButton.setEnabled(false);
            }
            return;
        }

        double totalPrice = selectedItem.getPrice() * quantity;
        totalPriceLabel.setText(String.format("RM %.2f", totalPrice));
        if (addSaleButton != null) {
            addSaleButton.setEnabled(true);
        }
    }

    private void addSale() {
        Item selectedItem = (Item) itemComboBox.getSelectedItem();
        if (selectedItem == null) {
            JOptionPane.showMessageDialog(this, "Please select an item.", "Input Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int quantity;
        try {
            quantity = Integer.parseInt(quantityField.getText().trim());
            if (quantity <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number.", "Input Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (quantity > selectedItem.getQuantity()) {
                JOptionPane.showMessageDialog(this,
                        "Not enough stock. Only " + selectedItem.getQuantity() + " available.",
                        "Insufficient Stock",
                        JOptionPane.WARNING_MESSAGE);
                quantityField.setText("");
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid whole number for quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Get date from JDateChooser
        Date selectedDate = saleDateChooser.getDate();
        if (selectedDate == null) {
            JOptionPane.showMessageDialog(this, "Please select a valid sale date.", "Date Selection Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String saleDateString = sdf.format(selectedDate); // Format Date object to String

        double unitPrice = selectedItem.getPrice();
        double totalPrice = unitPrice * quantity;
        String receiptNo = salesController.generateNextReceiptNo();
        String customerName = customerNameField.getText().trim();

        User currentUser = LoggedInUser.getCurrentUser();
        String salesPIC = (currentUser != null) ? currentUser.getUsername() : "N/A";

        Sales newSale = new Sales(
                receiptNo,
                saleDateString, // Use the formatted date string
                selectedItem.getItemID(),
                selectedItem.getItemName(),
                quantity,
                unitPrice,
                totalPrice,
                salesPIC,
                customerName
        );

        if (salesController.addSales(newSale)) {
            selectedItem.setQuantity(selectedItem.getQuantity() - quantity);
            itemController.updateItem(selectedItem);

            JOptionPane.showMessageDialog(this, "Sale added successfully!\nReceipt No: " + receiptNo, "Success", JOptionPane.INFORMATION_MESSAGE);
            refreshSalesTable();
            populateItemComboBox();
            quantityField.setText("");
            customerNameField.setText("");
            saleDateChooser.setDate(new Date()); // Reset JDateChooser to today's date
            updatePrices();
            updateSalesSummary();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to add sale.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshSalesTable() {
        tableModel.setRowCount(0);
        List<Sales> sales = salesController.getSalesList();
        for (Sales sale : sales) {
            Object[] row = {
                    sale.getReceiptNo(),
                    sale.getDate(),
                    sale.getItemId(),
                    sale.getItemName(),
                    sale.getQuantitySold(),
                    String.format("RM %.2f", sale.getUnitPrice()),
                    String.format("RM %.2f", sale.getTotalPrice()),
                    sale.getSalesPIC(),
                    sale.getCustomerName()
            };
            tableModel.addRow(row);
        }
    }

    private void deleteSelectedSale() {
        int selectedRow = salesTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a sale to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String receiptNoToDelete = (String) salesTable.getValueAt(selectedRow, 0);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete sale with Receipt No: " + receiptNoToDelete + "?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            Sales saleToDelete = salesController.getSalesByReceiptNo(receiptNoToDelete);
            if (saleToDelete != null) {
                Item itemAffected = itemController.getItemById(saleToDelete.getItemId());
                if (itemAffected != null) {
                    itemAffected.setQuantity(itemAffected.getQuantity() + saleToDelete.getQuantitySold());
                    itemController.updateItem(itemAffected);
                }
            }

            if (salesController.deleteSales(receiptNoToDelete)) {
                JOptionPane.showMessageDialog(this, "Sale deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                refreshSalesTable();
                populateItemComboBox();
                updatePrices();
                updateSalesSummary();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete sale.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSalesSummary() {
        List<Sales> allSales = salesController.getSalesList();
        LocalDate today = LocalDate.now();

        double dailyTotal = 0.0;
        double monthlyTotal = 0.0;
        double yearlyTotal = 0.0;

        for (Sales sale : allSales) {
            try {
                LocalDate saleDate = LocalDate.parse(sale.getDate());
                double salePrice = sale.getTotalPrice();

                if (saleDate.isEqual(today)) {
                    dailyTotal += salePrice;
                }

                if (saleDate.getMonth() == today.getMonth() && saleDate.getYear() == today.getYear()) {
                    monthlyTotal += salePrice;
                }   

                if (saleDate.getYear() == today.getYear()) {
                    yearlyTotal += salePrice;
                }
            } catch (DateTimeParseException e) {
                System.err.println("Failed to parse sale date: " + sale.getDate());
            }
        }

        dailySalesLabel.setText(String.format("Daily Sales: RM %.2f", dailyTotal));
        monthlySalesLabel.setText(String.format("Monthly Sales: RM %.2f", monthlyTotal));
        yearlySalesLabel.setText(String.format("Yearly Sales: RM %.2f", yearlyTotal));
    }
}
