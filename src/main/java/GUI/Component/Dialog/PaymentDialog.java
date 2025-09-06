package GUI.Component.Dialog;

import Management.Model.Payment;
import Management.Controller.PaymentController;
import Management.Controller.POController;
import Management.Model.PurchaseOrder;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.List;

public class PaymentDialog extends JDialog {
    private JTextField paymentIdField;
    private JComboBox<String> poIdComboBox; // Changed to combobox
    private JTextField poIDField; // Changed to combobox
    private JDateChooser paymentDateChooser;
    private JTextField prIDField;
    private JComboBox<String> statusComboBox;
    private boolean confirmed = false;
    private final PaymentController paymentController;
    private final POController poController;
    private List<PurchaseOrder> approvedPOs;

    public PaymentDialog(JFrame parent, String title, Payment payment, PaymentController paymentController) {
        super(parent, title, true);
        this.paymentController = paymentController;
        poController = new POController();
        initialize(payment);
    }

    private void initialize(Payment payment) {
        setSize(400, 300);
        setLayout(new BorderLayout());
        setLocationRelativeTo(getParent());

        JPanel formPanel = new JPanel(new GridLayout(5, 2, 5, 5));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        paymentIdField = new JTextField();
        poIdComboBox = new JComboBox<>();
        prIDField = new JTextField();
        poIDField = new JTextField();
        poIDField.setEditable(false);
        // Load approved POs and setup selection listener
        loadApprovedPOs();

        paymentDateChooser = new JDateChooser();

        statusComboBox = new JComboBox<>(new String[]{"Pending", "Completed", "Cancelled"});

        if (payment != null) {
            paymentIdField.setText(payment.getPaymentId());
            poIDField.setText(payment.getPo_id());
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date paymentDate = sdf.parse(payment.getDate());
                paymentDateChooser.setDateFormatString("yyyy-MM-dd");
                paymentDateChooser.setDate(paymentDate);
            } catch (Exception ex) {
                paymentDateChooser.setDate(new Date());
            }
            prIDField.setText(payment.getPr_id());
            statusComboBox.setSelectedItem(payment.getStatus());
            paymentIdField.setEditable(false);
        } else {
            paymentIdField.setText(paymentController.generateNextPaymentId());
            paymentIdField.setEditable(false);
            statusComboBox.setSelectedItem("Pending");
            paymentDateChooser.setDate(new Date());
//            updatePODateField(); // Initialize date field for new payment
        }

        // Add components to form panel...
        formPanel.add(new JLabel("Payment ID:"));
        formPanel.add(paymentIdField);

        if (payment != null) {
            formPanel.add(new JLabel("PO ID:"));
            formPanel.add(poIDField);
        } else {
            formPanel.add(new JLabel("PO ID:"));
            formPanel.add(poIdComboBox);
        }

        formPanel.add(new JLabel("Payment Date:"));
        formPanel.add(paymentDateChooser);

        if (payment != null) {
            formPanel.add(new JLabel("Status:"));
            formPanel.add(statusComboBox);
        }

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadApprovedPOs() {
        // Clear existing items
        poIdComboBox.removeAllItems();

        // Load approved POs from controller
        approvedPOs = poController.loadApprovedPOData();

        // Add each approved PO ID to the combobox
        for (PurchaseOrder po : approvedPOs) {
            poIdComboBox.addItem(po.getPo_id());
        }

        // Handle case when no approved POs are available
        if (approvedPOs.isEmpty()) {
            poIdComboBox.addItem("No approved POs available");
            poIdComboBox.setEnabled(false);
        }
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }

    public Payment getPayment() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String paymentDateStr = sdf.format(paymentDateChooser.getDate());

        String selectedPoId;
        if (poIdComboBox.isShowing()) {
            selectedPoId = (String) poIdComboBox.getSelectedItem();
        } else {
            selectedPoId = poIDField.getText(); // For edit mode
        }

        String totalAmount = "0.00";
        for (PurchaseOrder po : approvedPOs) {
            if (po.getPo_id().equals(selectedPoId)) {
                totalAmount = po.getTotalAmount();
                break;
            }
        }

        return new Payment(
                paymentIdField.getText(),
                selectedPoId,
                prIDField.getText(),
                paymentDateStr,
                (String) statusComboBox.getSelectedItem(),
                totalAmount
        );
    }
}