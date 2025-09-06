package GUI.Component.Dialog;

import Management.Controller.PRController;
import Management.Controller.POController;
import Management.Controller.ItemController;
import Management.Controller.SupplierController;
import Management.Model.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

public class PODialog extends JDialog {
    private final JTextField poIdField;
    private final JComboBox<String> prIdComboBox;
    private final JTextField quantityField;
    private final JTextField dateField;
    private final JComboBox<String> statusComboBox;
    private final JTextField itemNameField;
    private final JTextField totalPriceField;
    private final JTextField pridField;
    private final JTextField statusField;
    private boolean confirmed = false;
    private PurchaseOrder initialPOToEdit;

    private final PRController prController;
    private final POController poController;
    private final ItemController itemController;
    private final SupplierController supplierController;
    User currentUser = LoggedInUser.getCurrentUser();
    public PODialog(JFrame parent, String title, PurchaseOrder po) {
        super(parent, title, true);
        this.initialPOToEdit = po;
        setSize(450, 450);
        setLayout(new BorderLayout());
        setLocationRelativeTo(parent);

        prController = new PRController();
        poController = new POController();
        itemController = new ItemController();
        supplierController = new SupplierController();

        poIdField = new JTextField();
        poIdField.setEditable(false);
        prIdComboBox = new JComboBox<>();
        pridField = new JTextField();
        pridField.setEditable(false);
        quantityField = new JTextField();
        dateField = new JTextField();
        statusField = new JTextField();
        statusField.setEditable(false);
        statusComboBox = new JComboBox<>(new String[]{"Pending", "Approved", "Rejected"});
        itemNameField = new JTextField();
        itemNameField.setEditable(false);
        totalPriceField = new JTextField();
        totalPriceField.setEditable(false);

        prIdComboBox.addActionListener(e -> updateItemDetailsAndPrice());

        quantityField.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateItemDetailsAndPrice(); }
            @Override public void removeUpdate(DocumentEvent e) { updateItemDetailsAndPrice(); }
            @Override public void changedUpdate(DocumentEvent e) { updateItemDetailsAndPrice(); }
        });

        JPanel formPanel = createFormPanel(po);
        JPanel buttonPanel = createButtonPanel();

        add(formPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        if (po == null) {
            generatePoId();
            dateField.setText(LocalDate.now().toString());
            statusComboBox.setSelectedItem("Pending");
            loadPrIds();
        } else {
            populateFields(po);
        }
        updateItemDetailsAndPrice();
    }

    private void updateItemDetailsAndPrice() {
        String selectedPrId = (String) prIdComboBox.getSelectedItem();
        if (selectedPrId != null && !selectedPrId.isEmpty()) {
            PurchaseRequisition pr = prController.getPRById(selectedPrId);
            if (pr != null) {
                // Get the item to display its name
                Item item = itemController.getItemById(pr.getItemId());
                if (item != null) {
                    itemNameField.setText(item.getItemName());

                    // Get the supplier for this item to get the cost
                    Supplierlist supplier = supplierController.getSupplierByItemId(pr.getItemId());
                    if (supplier != null) {
                        try {
                            int quantity = quantityField.getText().isEmpty() ? 0 :
                                    Integer.parseInt(quantityField.getText());
                            double unitPrice = supplier.getCost();
                            double totalPrice = quantity * unitPrice;
                            totalPriceField.setText(String.format("%.2f", totalPrice));
                        } catch (NumberFormatException e) {
                            totalPriceField.setText("0.00");
                            JOptionPane.showMessageDialog(this,
                                    "Invalid quantity format",
                                    "Input Error",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    } else {
                        totalPriceField.setText("0.00");
                        JOptionPane.showMessageDialog(this,
                                "No supplier found for this item",
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);
                    }
                }
            }
        }
    }

    private JPanel createFormPanel(PurchaseOrder po) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        Dimension fieldSize = new Dimension(250, 25);  // Adjust width here
        poIdField.setPreferredSize(fieldSize);
        prIdComboBox.setPreferredSize(fieldSize);
        pridField.setPreferredSize(fieldSize);
        quantityField.setPreferredSize(fieldSize);
        dateField.setPreferredSize(fieldSize);
        statusComboBox.setPreferredSize(fieldSize);
        statusField.setPreferredSize(fieldSize);
        itemNameField.setPreferredSize(fieldSize);
        totalPriceField.setPreferredSize(fieldSize);

        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("PO ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(poIdField, gbc);

        if (po == null) {
            gbc.gridx = 0;
            gbc.gridy++;
            formPanel.add(new JLabel("PR ID:"), gbc);
            gbc.gridx = 1;
            formPanel.add(prIdComboBox, gbc);
        } else {
            gbc.gridx = 0;
            gbc.gridy++;
            formPanel.add(new JLabel("PR ID:"), gbc);
            gbc.gridx = 1;
            formPanel.add(pridField, gbc);
        }
        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Quantity:"), gbc);
        gbc.gridx = 1;
        formPanel.add(quantityField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Date:"), gbc);
        gbc.gridx = 1;
        formPanel.add(dateField, gbc);
        if (po != null) {
            if (currentUser.getUserRoleId().equals("ur_id2") || currentUser.getUserRoleId().equals("ur_id3")) {
                gbc.gridx = 0; gbc.gridy++;
                formPanel.add(new JLabel("Status:"), gbc);
                gbc.gridx = 1;
                formPanel.add(statusField, gbc);
            } else {
                gbc.gridx = 0; gbc.gridy++;
                formPanel.add(new JLabel("Status:"), gbc);
                gbc.gridx = 1;
                formPanel.add(statusComboBox, gbc);
            }
        }

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(itemNameField, gbc);

        gbc.gridx = 0; gbc.gridy++;
        formPanel.add(new JLabel("Total Amount:"), gbc);
        gbc.gridx = 1;
        formPanel.add(totalPriceField, gbc);

        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");

        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void loadPrIds() {
        prIdComboBox.removeAllItems();
        List<PurchaseRequisition> prList = prController.loadApprovedPRData();
        for (PurchaseRequisition pr : prList) {
            if ("Approved".equalsIgnoreCase(pr.getStatus())) {
                prIdComboBox.addItem(pr.getPr_id());
            }
        }
    }

    private void generatePoId() {
        poIdField.setText(poController.generateNextPoId());
    }

    private void populateFields(PurchaseOrder po) {
        poIdField.setText(po.getPo_id());
        loadPrIds();
        prIdComboBox.setSelectedItem(po.getPr_id());
        pridField.setText(po.getPr_id());
        quantityField.setText(po.getQuantity());
        dateField.setText(po.getDate());
        statusField.setText(po.getStatus());
        statusComboBox.setSelectedItem(po.getStatus());
        totalPriceField.setText(po.getTotalAmount());
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public PurchaseOrder getPO() {
        return new PurchaseOrder(
                poIdField.getText(),
                (String) prIdComboBox.getSelectedItem(),
                quantityField.getText(),
                dateField.getText(),
                (String) statusComboBox.getSelectedItem(),
                totalPriceField.getText()
        );
    }
}
