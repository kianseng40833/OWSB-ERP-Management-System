//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package GUI.Component.Dialog;

import Management.Model.SupplyItem;
import com.toedter.calendar.JDateChooser;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class SupplyItemDialog extends JDialog {
    private final JTextField supplyItemIdField;
    private final JTextField paymentIdField;
    private final JTextField itemIdField;
    private final JTextField itemNameField;
    private final JTextField quantityOrderedField;
    private final JTextField quantityReceivedField;
    private JDateChooser suppliedItemDateChooser;
    private final JComboBox<String> statusComboBox;
    private boolean confirmed = false;

    public SupplyItemDialog(JFrame parent, String title, SupplyItem supplyItem) {
        super(parent, title, true);
        this.setSize(450, 600);
        this.setLayout(new BorderLayout());
        this.setLocationRelativeTo(parent);
        this.supplyItemIdField = new JTextField();
        this.supplyItemIdField.setEditable(false);
        this.paymentIdField = new JTextField();
        this.paymentIdField.setEditable(false);
        this.itemIdField = new JTextField();
        this.itemNameField = new JTextField();
        this.quantityOrderedField = new JTextField();
        this.quantityOrderedField.setEditable(false);
        this.quantityReceivedField = new JTextField();
        this.suppliedItemDateChooser = new JDateChooser();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (supplyItem != null && supplyItem.getSuppliedDate() != null && !supplyItem.getSuppliedDate().isEmpty()) {
                Date suppliedDate = sdf.parse(supplyItem.getSuppliedDate());
                if (suppliedDate == null) {
                    this.suppliedItemDateChooser.setDateFormatString("yyyy-MM-dd");
                    this.suppliedItemDateChooser.setDate(new Date());
                }

                this.suppliedItemDateChooser.setDateFormatString("yyyy-MM-dd");
                this.suppliedItemDateChooser.setDate(suppliedDate);
            } else {
                this.suppliedItemDateChooser.setDate(new Date());
            }
        } catch (Exception ex) {
            System.err.println("Error parsing date in SupplyItemDialog: " + ex.getMessage());
            this.suppliedItemDateChooser.setDate(new Date());
        }

        this.statusComboBox = new JComboBox(new String[]{"Pending Delivery", "Completed", "Cancelled"});
        if (supplyItem != null) {
            this.populateFields(supplyItem);
        }

        JPanel formPanel = this.createFormPanel(supplyItem);
        JPanel buttonPanel = this.createButtonPanel();
        this.add(formPanel, "Center");
        this.add(buttonPanel, "South");
    }

    private JPanel createFormPanel(SupplyItem item) {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = 17;
        gbc.fill = 2;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Supply Item ID:"), gbc);
        gbc.gridx = 1;
        gbc.weightx = (double)1.0F;
        formPanel.add(this.supplyItemIdField, gbc);
        gbc.weightx = (double)0.0F;
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Payment ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.paymentIdField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Item ID:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.itemIdField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Item Name:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.itemNameField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Quantity Ordered:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.quantityOrderedField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Quantity Received:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.quantityReceivedField, gbc);
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Supplied Date:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.suppliedItemDateChooser, gbc);
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Status:"), gbc);
        gbc.gridx = 1;
        formPanel.add(this.statusComboBox, gbc);
        return formPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(2));
        JButton okButton = new JButton("OK");
        JButton cancelButton = new JButton("Cancel");
        okButton.setPreferredSize(new Dimension(100, 30));
        cancelButton.setPreferredSize(new Dimension(100, 30));
        okButton.addActionListener((e) -> {
            String quantityOrdered = this.quantityOrderedField.getText();
            String quantityReceived = this.quantityReceivedField.getText();
            if (quantityOrdered != null && quantityReceived != null && quantityOrdered.trim().equals(quantityReceived.trim())) {
                this.confirmed = true;
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Quantity Received must be equal to Quantity Ordered to submit.", "Validation Error", 0);
            }

        });
        cancelButton.addActionListener((e) -> this.dispose());
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void populateFields(SupplyItem item) {
        this.supplyItemIdField.setText(item.getSupplyItem_ID());
        this.paymentIdField.setText(item.getPayment_ID());
        this.itemIdField.setText(item.getItem_ID());
        this.itemNameField.setText(item.getItem_Name());
        this.quantityOrderedField.setText(item.getQuantityOrdered());
        this.quantityReceivedField.setText(item.getQuantityReceived());

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            if (item.getSuppliedDate() != null && !item.getSuppliedDate().isEmpty()) {
                Date date = sdf.parse(item.getSuppliedDate());
                this.suppliedItemDateChooser.setDate(date);
            } else {
                this.suppliedItemDateChooser.setDate((Date)null);
            }

            this.suppliedItemDateChooser.setDateFormatString("yyyy-MM-dd");
        } catch (Exception ex) {
            System.err.println("Error parsing date in populateFields: " + ex.getMessage());
            this.suppliedItemDateChooser.setDate((Date)null);
        }

        this.statusComboBox.setSelectedItem(item.getStatus());
    }

    public boolean showDialog() {
        this.setVisible(true);
        return this.confirmed;
    }

    public SupplyItem getSupplyItem() {
        String suppliedItemDate = "";
        if (this.suppliedItemDateChooser.getDate() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            suppliedItemDate = sdf.format(this.suppliedItemDateChooser.getDate());
        }

        return new SupplyItem(this.supplyItemIdField.getText(), this.paymentIdField.getText(), this.itemIdField.getText(), this.itemNameField.getText(), this.quantityOrderedField.getText(), this.quantityReceivedField.getText(), suppliedItemDate, (String)this.statusComboBox.getSelectedItem());
    }
}