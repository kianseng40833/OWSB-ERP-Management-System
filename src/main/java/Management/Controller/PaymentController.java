package Management.Controller;

import Management.FileIO.FileIO;
import Management.Model.Payment;
import Management.Model.SupplyItem;
import Management.Model.PurchaseOrder;
import Management.Model.PurchaseRequisition;
import Management.Model.Item;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PaymentController {
    private final String rootFilePath = "src/main/resources/";
    private final String filePath = "payment.txt";
    private static final String PAYMENT_HEADER = "paymentId,po_id,pr_id,date,status,totalAmount";
    private List<Payment> paymentList = new ArrayList<>();

    private SupplyItemController supplyItemController;
    private POController poController;
    private PRController prController;
    private ItemController itemController;

    public PaymentController() {
        loadPaymentData();
    }

    private SupplyItemController getSupplyItemController() {
        if (supplyItemController == null) {
            supplyItemController = new SupplyItemController();
        }
        return supplyItemController;
    }

    private POController getPOController() {
        if (poController == null) {
            poController = new POController();
        }
        return poController;
    }

    private PRController getPRController() {
        if (prController == null) {
            prController = new PRController();
        }
        return prController;
    }

    private ItemController getItemController() {
        if (itemController == null) {
            itemController = new ItemController();
        }
        return itemController;
    }

    public List<Payment> loadPaymentData() {
        List<String> data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
        paymentList.clear();

        // Assuming the first line is a header, skip it.
        int startIndex = 1;
        for (int i = startIndex; i < data.size(); i++) {
            Payment payment = Payment.fromFileString(data.get(i));
            if (payment != null) {
                paymentList.add(payment);
            }
        }
        return paymentList;
    }

    public List<Payment> getPaymentList() {
        return new ArrayList<>(paymentList);
    }

    public boolean addPayment(Payment payment) {
        if (payment == null) {
            System.err.println("Error: Payment object is null when trying to add.");
            return false;
        }

        paymentList.add(payment);
        boolean paymentAddedToFile = saveAllPayments();

        if (paymentAddedToFile) {
            // --- Generate SupplyItem(s) after Payment is successfully added ---
            POController poController = getPOController();
            // No need to loadPOData() here, as POController.updatePOInFile already saved the latest data
            PurchaseOrder po = poController.getPOById(payment.getPo_id());

            if (po != null) {
                // Debug lines removed
                PRController prController = getPRController();
                ItemController itemController = getItemController();

                PurchaseRequisition pr = prController.getPRById(po.getPr_id());
                if (pr != null) {
                    Item item = itemController.getItemById(pr.getItem_id());
                    if (item != null) {
                        SupplyItemController supplyItemController = getSupplyItemController();

                        SupplyItem newSupplyItem = new SupplyItem(
                                supplyItemController.generateNextSupplyItemId(po.getPo_id(), item.getItemID()),
                                payment.getPaymentId(),
                                item.getItemID(),
                                item.getItemName(),
                                po.getQuantity(), // Quantity Ordered from PO
                                "0",
                                "",
                                "Pending Delivery"
                        );
                        // Debug line removed
                        if (!supplyItemController.addSupplyItem(newSupplyItem)) {
                            System.err.println("Error creating SupplyItem for Payment ID: " + payment.getPaymentId() + ", Item ID: " + item.getItemID());
                        } else {
                            JOptionPane.showMessageDialog(null, "Supply Item record created for Payment ID: " + payment.getPaymentId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                        }
                    } else {
                        System.err.println("Warning: Item not found for PR ID: " + pr.getPr_id() + " when generating Supply Item.");
                    }
                } else {
                    System.err.println("Warning: Purchase Requisition not found for PO ID: " + po.getPo_id() + " when generating Supply Item.");
                }
            } else {
                System.err.println("Warning: Could not find Purchase Order for PO ID: " + payment.getPo_id() + " to generate Supply Items.");
            }
            return true;
        }
        return false;
    }

    public boolean updatePaymentInFile(Payment payment) {
        loadPaymentData();

        boolean updated = false;
        for (int i = 0; i < paymentList.size(); i++) {
            Payment existingPayment = paymentList.get(i);
            if (existingPayment.getPaymentId().equals(payment.getPaymentId())) {
                // Check if trying to change status to "Completed" from a non-completed status
                if ("Completed".equalsIgnoreCase(payment.getStatus()) &&
                        !"Completed".equalsIgnoreCase(existingPayment.getStatus())) {

                    SupplyItemController supplyItemController = getSupplyItemController();
                    List<SupplyItem> supplyItems = supplyItemController.loadSupplyItemData();

                    SupplyItem linkedSupplyItem = supplyItems.stream()
                            .filter(si -> si.getPayment_ID().equals(payment.getPaymentId()))
                            .findFirst()
                            .orElse(null);

                    if (linkedSupplyItem == null) {
                        JOptionPane.showMessageDialog(null,
                                "Cannot complete payment. No associated Supply Item record found for Payment ID: " + payment.getPaymentId() + ".",
                                "Payment Approval Error",
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    if (!"Completed".equalsIgnoreCase(linkedSupplyItem.getStatus())) {
                        JOptionPane.showMessageDialog(null,
                                "Cannot complete payment. The associated Supply Item (ID: " + linkedSupplyItem.getSupplyItem_ID() + ") is not yet approved (Status: " + linkedSupplyItem.getStatus() + ").",
                                "Payment Approval Error",
                                JOptionPane.ERROR_MESSAGE);
                        return false;
                    }

                    if (linkedSupplyItem.getSuppliedDate().isEmpty()) {
                        linkedSupplyItem.setSuppliedDate(LocalDate.now().toString());
                        if (!supplyItemController.updateSupplyItemInFile(linkedSupplyItem)) {
                            System.err.println("Warning: Failed to update SupplyItem's supplied date for Payment ID: " + payment.getPaymentId());
                        }
                    }
                }

                paymentList.set(i, payment);
                updated = true;
                break;
            }
        }

        if (updated) {
            return saveAllPayments();
        }
        return false;
    }

    public boolean deletePayment(Payment payment) {
        if (payment == null) {
            System.err.println("Error: Payment object is null when trying to delete.");
            return false;
        }
        if ("Completed".equalsIgnoreCase(payment.getStatus())) {
            JOptionPane.showMessageDialog(null,
                    "Completed payments cannot be deleted.",
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            loadPaymentData();
            boolean deleted = paymentList.removeIf(p -> p.getPaymentId().equals(payment.getPaymentId()));

            if (deleted) {
                return saveAllPayments();
            }
            return false;
        }
    }

    public String generateNextPaymentId() {
        loadPaymentData();
        if (paymentList.isEmpty()) {
            return "P001";
        }

        List<Integer> ids = new ArrayList<>();
        for (Payment p : paymentList) {
            try {
                String numStr = p.getPaymentId().substring(1);
                ids.add(Integer.parseInt(numStr));
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid payment ID format encountered: " + p.getPaymentId());
            }
        }

        if (ids.isEmpty()) {
            return "P001";
        }

        int maxId = Collections.max(ids);
        return String.format("P%03d", maxId + 1);
    }

    public List<Payment> loadPaymentStatusData() {
        loadPaymentData();
        List<Payment> completedPayments = new ArrayList<>();
        for (Payment payment : paymentList) {
            if ("Completed".equalsIgnoreCase(payment.getStatus())) {
                completedPayments.add(payment);
            }
        }
        return completedPayments;
    }

    private boolean saveAllPayments() {
        List<String> linesToWrite = new ArrayList<>();
        String headerLine = null;

        List<String> existingFileContent = FileIO.readFromFile(rootFilePath + filePath, 1);
        if (!existingFileContent.isEmpty()) {
            headerLine = existingFileContent.get(0);
        }

        if (headerLine != null) {
            linesToWrite.add(headerLine);
        } else {
            linesToWrite.add(PAYMENT_HEADER);
        }

        for (Payment payment : paymentList) {
            linesToWrite.add(payment.toFileString());
        }

        return FileIO.writeToFile(rootFilePath + filePath, linesToWrite, false);
    }
}
