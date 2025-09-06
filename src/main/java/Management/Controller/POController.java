package Management.Controller;

import Management.FileIO.FileIO;
import Management.Model.PurchaseOrder;
import Management.Model.Payment;
import Management.Model.PurchaseRequisition;
import Management.Model.Item;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class POController {
    private final String rootFilePath = "src/main/resources/";
    private final String filePath = "purchaseOrder.txt";
    private static final String PO_HEADER = "PO_ID,PR_ID,Quantity,Date,Status,TotalAmount"; // Assuming Quantity is total for PO

    private List<PurchaseOrder> purchaseOrderList = new ArrayList<>();

    // Dependencies
    private PRController prController;
    private ItemController itemController;
    private PaymentController paymentController;

    public POController() {
        loadPOData();
    }

    // Lazy initialization for controllers
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

    private PaymentController getPaymentController() {
        if (paymentController == null) {
            paymentController = new PaymentController();
        }
        return paymentController;
    }

    public List<PurchaseOrder> loadPOData() {
        List<String> data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
        purchaseOrderList.clear();

        int startIndex = 0;
        if (!data.isEmpty() && data.get(0).trim().equalsIgnoreCase(PO_HEADER.trim())) {
            startIndex = 1;
        }

        for (int i = startIndex; i < data.size(); i++) {
            PurchaseOrder po = PurchaseOrder.fromFileString(data.get(i));
            if (po != null) {
                purchaseOrderList.add(po);
            }
        }
        return purchaseOrderList;
    }

    public List<PurchaseOrder> getPOList() {
        return new ArrayList<>(purchaseOrderList);
    }

    public String generateNextPoId() {
        loadPOData();
        int maxIdNum = 0;
        Pattern pattern = Pattern.compile("^PO(\\d{4})$");

        for (PurchaseOrder po : purchaseOrderList) {
            Matcher matcher = pattern.matcher(po.getPo_id());
            if (matcher.matches()) {
                try {
                    int idNum = Integer.parseInt(matcher.group(1));
                    if (idNum > maxIdNum) {
                        maxIdNum = idNum;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing PO ID number: " + po.getPo_id() + " - " + e.getMessage());
                }
            }
        }
        return String.format("PO%04d", maxIdNum + 1);
    }

    public boolean addPO(PurchaseOrder po) {
        if (po == null) {
            System.err.println("Error: PO object is null when trying to add.");
            return false;
        }

        String newPoId = po.getPo_id();

        if (newPoId == null || newPoId.isEmpty() || newPoId.equals("PO0000")) {
            newPoId = generateNextPoId();
            po.setPo_id(newPoId);
        } else {
            if (getPOById(newPoId) != null) {
                JOptionPane.showMessageDialog(null,
                        "Purchase Order with ID " + newPoId + " already exists.",
                        "Duplicate PO ID",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        purchaseOrderList.add(po);
        return saveAllPOs(purchaseOrderList);
    }

    public boolean updatePOInFile(PurchaseOrder purchaseOrder) {
        loadPOData(); // Ensure the in-memory list is fresh
        boolean updated = false;
        boolean wasApproved = false; // Flag to track if status changed to Approved

        for (int i = 0; i < purchaseOrderList.size(); i++) {
            PurchaseOrder existingPo = purchaseOrderList.get(i);
            if (existingPo.getPo_id().equalsIgnoreCase(purchaseOrder.getPo_id())) {
                wasApproved = "Approved".equalsIgnoreCase(purchaseOrder.getStatus()) &&
                        !"Approved".equalsIgnoreCase(existingPo.getStatus());

                // Update the in-memory list with the new purchaseOrder object
                purchaseOrderList.set(i, purchaseOrder);
                updated = true;
                break;
            }
        }

        if (updated) {
            // IMPORTANT FIX: FIRST, save the updated PO data to the file.
            // This ensures PaymentController reads the correct, updated quantity.
            if (!saveAllPOs(purchaseOrderList)) {
                System.err.println("Error: Failed to save updated PO data to file.");
                return false; // If saving fails, the operation should fail
            }

            // THEN, if PO was just approved, create the new Payment record.
            // Now, when createNewPayment (and subsequently PaymentController.addPayment)
            // calls poController.getPOById(), it will read the already saved, updated data.
            if (wasApproved) {
                createNewPayment(purchaseOrder);
            }
            return true; // Return true because the update and save were successful
        }
        return false;
    }

    /**
     * Creates a new Payment record when a Purchase Order is approved.
     * This Payment record will then trigger the creation of SupplyItems.
     * @param po The approved PurchaseOrder.
     */
    private void createNewPayment(PurchaseOrder po) {
        if (po == null) {
            System.err.println("Error: PO object is null when trying to create Payment.");
            return;
        }

        PaymentController paymentController = getPaymentController();
        String paymentId = paymentController.generateNextPaymentId(); // Generate new Payment ID

        // Create a new Payment object
        Payment newPayment = new Payment(
                paymentId,
                po.getPo_id(),
                po.getPr_id(), // Assuming PR_ID is stored in Payment for traceability
                LocalDate.now().toString(), // Payment Date
                "Pending", // Initial status for Payment
                po.getTotalAmount() // Total amount from the PO
        );

        // Add the new Payment. This call will now trigger SupplyItem creation within PaymentController.addPayment()
        if (paymentController.addPayment(newPayment)) {
            JOptionPane.showMessageDialog(null, "New Payment record (" + paymentId + ") created for PO: " + po.getPo_id(), "Success", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null, "Failed to create Payment record for PO: " + po.getPo_id(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean deleteFile(PurchaseOrder purchaseOrder) {
        loadPOData();
        boolean deleted = false;

        if ("Approved".equalsIgnoreCase(purchaseOrder.getStatus())) {
            JOptionPane.showMessageDialog(null,
                    "Approved Purchase Orders cannot be deleted.",
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            if (purchaseOrderList.removeIf(po -> po.getPo_id().equalsIgnoreCase(purchaseOrder.getPo_id()))) {
                deleted = true;
            }

            if (deleted) {
                return saveAllPOs(purchaseOrderList);
            }
            return false;
        }
    }

    public PurchaseOrder getPOById(String poId) {
        loadPOData();
        return purchaseOrderList.stream()
                .filter(po -> po.getPo_id().equalsIgnoreCase(poId))
                .findFirst()
                .orElse(null);
    }

    public List<PurchaseOrder> loadApprovedPOData() {
        loadPOData();
        return purchaseOrderList.stream()
                .filter(po -> "Approved".equalsIgnoreCase(po.getStatus()))
                .collect(Collectors.toList());
    }

    private boolean saveAllPOs(List<PurchaseOrder> pos) {
        if (pos == null) {
            System.err.println("Error: List of POs to save is null.");
            return false;
        }

        List<String> dataToSave = new ArrayList<>();
        dataToSave.add(PO_HEADER);

        for (PurchaseOrder po : pos) {
            dataToSave.add(po.toFileString());
        }
        return FileIO.writeToFile(rootFilePath + filePath, dataToSave, false);
    }
}
