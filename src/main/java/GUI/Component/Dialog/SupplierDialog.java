package GUI.Component.Dialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import Management.Controller.ItemController;
import Management.Model.Item;
import Management.Model.Supplierlist;
import Management.Controller.SupplierController;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.util.List;

public class SupplierDialog extends JDialog {
    private final JDialog dialog;
    private JTextField supplierIDField;
    private JTextField companyNameField;
    private JTextField contactField;
    private JTextField emailField;
    private JTextField picField;
    private JComboBox<Item> itemComboBox;
    private JTextField itemNameField;
    private JTextField costField;
    private JComboBox<String> statusComboBox;
    private boolean confirmed = false;
    private boolean isEditing = false;
    private SupplierController supplierController;
    private ItemController itemController;

    public SupplierDialog(JFrame parent, String title, Supplierlist supplier) {
        dialog = new JDialog(parent, title, true);
        dialog.setSize(400, 450); // Increased size to accommodate new fields
        dialog.setResizable(false);
        supplierController = new SupplierController();
        itemController = new ItemController();
        initComponents(supplier);
    }

    private void initComponents(Supplierlist supplier) {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Initialize fields
        supplierIDField = new JTextField();
        companyNameField = new JTextField();
        contactField = new JTextField();
        emailField = new JTextField();
        picField = new JTextField();
        itemComboBox = new JComboBox<>();
        itemNameField = new JTextField();
        itemNameField.setEditable(false); // Item Name should be read-only
        costField = new JTextField();
        statusComboBox = new JComboBox<>(new String[]{"Active", "Inactive"});
        statusComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        supplierIDField.setEditable(false); // Make supplier ID field non-editable here

        // Load items into the combo box BEFORE populating fields
        loadItemNames();

        // Populate fields if editing
        if (supplier != null) {
            isEditing = true;
            populateFields(supplier);
        } else {
            isEditing = false;
            String uniqueSupplierId = supplierController.generateSupplierId();
            supplierIDField.setText(uniqueSupplierId);
            statusComboBox.setSelectedItem("Active");
            // For new supplier, if there's a default item, set its name
            if (itemComboBox.getItemCount() > 0) {
                Item firstItem = itemComboBox.getItemAt(0);
                if (firstItem != null) {
                    itemNameField.setText(firstItem.getItemName());
                }
            }
        }

        // Add ActionListener to itemComboBox AFTER initial population
        itemComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Object selected = itemComboBox.getSelectedItem();
                if (selected instanceof Item) {
                    Item selectedItem = (Item) selected;
                    if (selectedItem != null) {
                        itemNameField.setText(selectedItem.getItemName());
                    } else {
                        itemNameField.setText(""); // Clear if no item is selected (shouldn't happen with Item objects)
                    }
                } else {
                    itemNameField.setText(""); // Clear if selection is not an Item (e.g., null)
                }
            }
        });


        // Create form panel
        JPanel formPanel = createFormPanel();
        mainPanel.add(formPanel);

        // Create button panel
        JPanel buttonPanel = createButtonPanel();
        mainPanel.add(buttonPanel);

        dialog.add(mainPanel);
    }

    private void populateFields(Supplierlist supplier) {
        supplierIDField.setText(supplier.getSupplierID());
        companyNameField.setText(supplier.getSupplierCompanyName());
        contactField.setText(supplier.getContact());
        emailField.setText(supplier.getEmail());
        picField.setText(supplier.getPIC());
        itemNameField.setText(supplier.getItemName());
        // Set selected item in the JComboBox
        // This will automatically trigger the ActionListener and update itemNameField
        for (int i = 0; i < itemComboBox.getItemCount(); i++) {
            Item currentItem = itemComboBox.getItemAt(i);
            if (currentItem != null && currentItem.getItemID().equals(supplier.getItemID())) {
                itemComboBox.setSelectedItem(currentItem); // Use setSelectedItem to trigger listener
                break;
            }
        }

        // itemNameField.setText(supplier.getItemName()); // This line is no longer strictly needed if listener works
        costField.setText(String.valueOf(supplier.getCost()));
        statusComboBox.setSelectedItem(supplier.getStatus());
        supplierIDField.setEditable(false);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(java.awt.Color.WHITE);
        formPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        formPanel.add(createFormRow("Supplier ID:", supplierIDField));
        formPanel.add(createFormRow("Company Name:", companyNameField));
        formPanel.add(createFormRow("Contact:", contactField));
        formPanel.add(createFormRow("Email:", emailField));
        formPanel.add(createFormRow("PIC:", picField));
        formPanel.add(createFormRow("Item ID:", itemComboBox));
        formPanel.add(createFormRow("Item Name:", itemNameField)); // Changed label for consistency
        formPanel.add(createFormRow("Cost:", costField));
        formPanel.add(createFormRow("Status:", statusComboBox));

        return formPanel;
    }

    private JPanel createFormRow(String labelText, JComponent field) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
        rowPanel.setBackground(java.awt.Color.WHITE);

        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(150, 25));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));

        rowPanel.add(label);
        rowPanel.add(Box.createHorizontalStrut(10));
        rowPanel.add(field);

        return rowPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> validateAndClose());
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(okButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void validateAndClose() {
        if (validateInput()) {
            confirmed = true;
            dialog.dispose();
        }
    }

    private boolean validateInput() {
       if (companyNameField.getText().trim().isEmpty() ||
            contactField.getText().trim().isEmpty() ||
            emailField.getText().trim().isEmpty() ||
            picField.getText().trim().isEmpty() ||
            itemNameField.getText().trim().isEmpty() ||
            costField.getText().trim().isEmpty())
       {
            showMessage("Please fill in all required fields", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        if (!emailField.getText().matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            showMessage("Invalid email format", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        Object selected = itemComboBox.getSelectedItem();
        if (selected == null || !(selected instanceof Item) || ((Item)selected).getItemID().isEmpty()) {
            showMessage("Please select an item", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        try {
            Double.parseDouble(costField.getText());
        } catch (NumberFormatException e) {
            showMessage("Invalid cost format. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
//        try {
//            Double.parseDouble(stockField.getText()); // Assuming stock can be a double, otherwise use Integer.parseInt
//        } catch (NumberFormatException e) {
//            showMessage("Invalid stock format. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
//            e.printStackTrace(); // Print stack trace for debugging
//            return false;
//        }

        return true;
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(dialog, message, title, messageType);
    }

    public boolean showDialog() {
        dialog.setLocationRelativeTo(dialog.getParent());
        dialog.setVisible(true);
        return confirmed;
    }

    public Supplierlist getSupplierData() {
        Object selected = itemComboBox.getSelectedItem();
        String itemId = "";
        String itemName = "";

        if (selected instanceof Item) {
            Item item = (Item) selected;
            itemId = item.getItemID();
            itemName = item.getItemName();
        }

        double cost = 0.0;
        try {
            cost = Double.parseDouble(costField.getText());
        } catch (NumberFormatException e) {
            System.err.println("Error parsing cost in getSupplierData: " + costField.getText());
        }

        return new Supplierlist(
                supplierIDField.getText(),
                companyNameField.getText(),
                contactField.getText(),
                emailField.getText(),
                picField.getText(),
                itemId,
                itemName, // Use the itemName from the selected item
                cost,
                (String) statusComboBox.getSelectedItem()
        );
    }


    private void loadItemNames() {
        List<Item> items = itemController.getItemList();
        itemComboBox.removeAllItems(); // Clear existing items
        for (Item item : items) {
            itemComboBox.addItem(item); // Add Item object directly
        }

        itemComboBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        // This listener is now added AFTER initial population and initial setting
        // Its purpose is to react to user changes, not initial setup.
    }
}