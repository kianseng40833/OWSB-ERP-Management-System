package GUI.Component.Dialog;

import Management.Model.Item;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;

public class StockAdjustmentDialog extends JDialog {
    private JTextField stockField;
    private JTextField storageStockField;
    private JButton transferButton;
    private JTextField transferField;
    private boolean confirmed = false;

    public StockAdjustmentDialog(JFrame parent, Item item) {
        super(parent, "Stock Adjustment", true);
        setSize(400, 250);
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parent);

        initializeFields(item);
        add(createFormPanel(), BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private void initializeFields(Item item) {
        stockField = new JTextField(String.valueOf(item.getQuantity()), 20);
        stockField.setEditable(false);

        storageStockField = new JTextField(String.valueOf(item.getStock()), 20);
        storageStockField.setEditable(false);

        transferField = new JTextField(20);
        setIntegerOnly(transferField); // apply numeric filter


        transferButton = new JButton("Transfer to Stock");
        transferButton.addActionListener(this::transferStock);
    }

    private JPanel createFormPanel() {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        formPanel.add(createFieldPanel("Current Stock:", stockField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Storage Stock:", storageStockField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Transfer Amount:", transferField));
        formPanel.add(Box.createVerticalStrut(10));

        JPanel transferPanel = new JPanel(new BorderLayout(5, 5));

        JPanel spinnerPanel = new JPanel();
        spinnerPanel.add(transferButton);

        transferPanel.add(spinnerPanel, BorderLayout.CENTER);
        formPanel.add(transferPanel);

        return formPanel;
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel jLabel = new JLabel(label);
        jLabel.setPreferredSize(new Dimension(120, jLabel.getPreferredSize().height));
        panel.add(jLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        return buttonPanel;
    }

    private void transferStock(ActionEvent e) {
        try {
            int transferAmount = Integer.parseInt(transferField.getText().trim());
            int currentStorage = Integer.parseInt(storageStockField.getText());
            int currentStock = Integer.parseInt(stockField.getText());

            if (transferAmount <= 0) {
                showValidationError("Transfer amount must be greater than 0");
                return;
            }

            if (transferAmount > currentStorage) {
                showValidationError("Not enough stock in storage");
                return;
            }

            if (transferAmount > 500) {
                showValidationError("Maximum transfer amount is 500");
                return;
            }

            if (currentStock + transferAmount > 500) {
                showValidationError("Resulting stock cannot exceed 500 items");
                return;
            }

            storageStockField.setText(String.valueOf(currentStorage - transferAmount));
            stockField.setText(String.valueOf(currentStock + transferAmount));

        } catch (NumberFormatException ex) {
            showValidationError("Please enter a valid number for transfer amount");
        }
    }



    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public int getUpdatedStock() {
        return Integer.parseInt(stockField.getText());
    }

    public int getUpdatedStorageStock() {
        return Integer.parseInt(storageStockField.getText());
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    private void setIntegerOnly(JTextField textField) {
        ((AbstractDocument) textField.getDocument()).setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (string == null) return;
                if (string.matches("\\d+")) { // only digits
                    super.insertString(fb, offset, string, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (text == null) return;
                String newText = fb.getDocument().getText(0, fb.getDocument().getLength()) + text;
                if (newText.matches("\\d*")) { // allow empty or digits
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }
}
