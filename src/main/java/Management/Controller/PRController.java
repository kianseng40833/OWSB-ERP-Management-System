package Management.Controller;

import Management.Model.PurchaseOrder;
import Management.Model.PurchaseRequisition;
import Management.FileIO.FileIO;
import Management.Model.Item;
import Management.Model.Supplierlist;

import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PRController {
    private final String rootFilePath = "src/main/resources/";
    private final String filePath = "purchaseRequisition.txt";
    private static final String PR_HEADER = "pr_id,item_id,stock,supplier_id,date,pic,status,remarks";

    private List<PurchaseRequisition> prList = new ArrayList<>();

    public PRController() {
        loadPRData(); // Initial load into the internal list
    }
    POController poController = new POController();

    // Changed return type to void, as this method's purpose is to load data into the internal prList
    public void loadPRData() {
        List<String> data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
        prList.clear(); // Clear the internal list before repopulating

        int startIndex = 0;
        if (data.size() > 0 && data.get(0).trim().equalsIgnoreCase(PR_HEADER.trim())) {
            startIndex = 1;
        } else if (data.size() > 0 && data.get(0).startsWith("PR_id") && !data.get(0).trim().equalsIgnoreCase(PR_HEADER.trim())) {
            startIndex = 1;
        }

        for (int i = startIndex; i < data.size(); i++) {
            String currentLine = data.get(i);
            PurchaseRequisition purchaseRequisition = PurchaseRequisition.fromFileString(currentLine);
            if (purchaseRequisition != null) {
                prList.add(purchaseRequisition);
            } else {
                System.err.println("ERROR: fromFileString returned null for line: '" + currentLine + "'. This line was NOT added to prList.");
            }
        }
        // No return statement needed as it's void
    }

    // This method now returns a *copy* of the internal prList to prevent ConcurrentModificationException
    public List<PurchaseRequisition> getPRList() {
        loadPRData(); // Ensure the internal list is up-to-date before returning a copy
        return new ArrayList<>(this.prList); // Return a new ArrayList containing all elements of the internal prList
    }

    public boolean addPR(PurchaseRequisition pr) {
        // Ensure the PR ID is generated if not already set (e.g., from dialog)
        if (pr.getPRID() == null || pr.getPRID().isEmpty() || pr.getPRID().equals("PR_id0")) {
            pr.setPr_id(generateNewPrId());
        }

        // Check for duplicate PR ID before adding
        if (getPRById(pr.getPRID()) != null) {
            JOptionPane.showMessageDialog(null,
                    "Purchase Requisition with ID " + pr.getPRID() + " already exists. Please use a different ID.",
                    "Duplicate PR ID",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        prList.add(pr); // Add to the internal list
        // Append the new PR to the file. Ensure header is written only once if file is new.
        // For existing files, just append.
        List<String> linesToWrite = new ArrayList<>();
        // Check if file is empty or only contains header before adding header
        List<String> currentFileContent = FileIO.readFromFile(rootFilePath + filePath, 1);
        linesToWrite.add(pr.toFileString());
        return FileIO.writeToFile(rootFilePath + filePath, linesToWrite, true); // Append mode
    }

    public String generateNewPrId() {
        loadPRData(); // Ensure prList is up-to-date
        int maxNumber = 0;
        // Pattern to match "PR_id" followed by digits (e.g., PR_id1, PR_id123)
        Pattern pattern = Pattern.compile("PR_id(\\d+)");

        for (PurchaseRequisition pr : prList) {
            String currentId = pr.getPRID();
            Matcher matcher = pattern.matcher(currentId);
            if (matcher.matches()) {
                try {
                    int number = Integer.parseInt(matcher.group(1));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("ERROR: Invalid PR ID format encountered (non-numeric part after 'PR_id'): '" + currentId + "'");
                }
            }
        }

        int nextNumber = maxNumber + 1;
        return String.format("PR_id%d", nextNumber);
    }

    public boolean updatePRInFile(PurchaseRequisition purchaseRequisition) {
        // Load the latest data from file to ensure we're working with the most current state
        loadPRData();
        boolean updated = false;

        for (int i = 0; i < prList.size(); i++) {
            PurchaseRequisition existingPr = prList.get(i);
            if (existingPr.getPRID().equalsIgnoreCase(purchaseRequisition.getPRID())) {
                boolean isBeingApproved = "Approved".equalsIgnoreCase(purchaseRequisition.getStatus()) &&
                        !"Approved".equalsIgnoreCase(existingPr.getStatus());

                // Update the PR in the in-memory list
                prList.set(i, purchaseRequisition);
                updated = true;

                if (isBeingApproved) {
                    createNewPO(purchaseRequisition);
                }
                break;
            }
        }

        if (updated) {
            // Save all PRs back to the file, overwriting its contents
            List<String> dataToSave = new ArrayList<>();
            dataToSave.add(PR_HEADER); // Add header
            for (PurchaseRequisition pr : prList) {
                dataToSave.add(pr.toFileString());
            }
            boolean success = FileIO.writeToFile(rootFilePath + filePath, dataToSave, false); // Overwrite
            if (!success) {
                System.err.println("ERROR: Failed to rewrite file after update for PR: " + purchaseRequisition.getPRID());
            }
            return success;
        }
        return false;
    }

    private void createNewPO(PurchaseRequisition pr) {
        // Generate PO ID using the PR ID as a base
        String poId = "PO" + pr.getPRID().substring(pr.getPRID().indexOf("id") + 2);
        String currentDate = LocalDate.now().toString();

        // Get related item and supplier information
        ItemController itemController = new ItemController();
        SupplierController supplierController = new SupplierController();

        Item item = itemController.getItemById(pr.getItem_id());
        Supplierlist supplier = supplierController.getSupplierByItemId(pr.getItem_id());

        // Calculate total amount
        String totalAmount = "0.00";
        if (item != null && supplier != null) {
            try {
                // The quantity for the newly created PO should initially be 0 or blank, not from the PR's stock.
                // The user will manually enter the quantity in the PO dialog.
                int quantity = 0; // Set initial quantity to 0
                double cost = supplier.getCost();
                totalAmount = String.format("%.2f", quantity * cost); // Calculate total based on 0 quantity
            } catch (NumberFormatException e) {
                System.err.println("Error calculating total amount for PO: " + e.getMessage());
            }
        }

        PurchaseOrder newPO = new PurchaseOrder(
                poId,
                pr.getPRID(),
                "0", // Set quantity to "0" for the new PO
                currentDate,
                "Pending",
                totalAmount
        );

        if (poController.addPO(newPO)) {
            System.out.println("Successfully created new PO " + newPO.getPo_id());
        } else {
            System.err.println("Failed to create new PO for PR " + pr.getPRID());
        }
    }

    public boolean deleteFile(PurchaseRequisition purchaseRequisition) {
        if ("Approved".equalsIgnoreCase(purchaseRequisition.getStatus())) {
            JOptionPane.showMessageDialog(null,
                    "Approved Purchase Requisitions cannot be deleted.",
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            // Load the latest data from file
            loadPRData();
            boolean deleted = false;

            // Find and remove the PR from the in-memory list
            if (prList.removeIf(pr -> pr.getPRID().equalsIgnoreCase(purchaseRequisition.getPRID()))) {
                deleted = true;
            }

            if (deleted) {
                // Save the updated list back to the file, overwriting its contents
                List<String> dataToSave = new ArrayList<>();
                dataToSave.add(PR_HEADER); // Add header
                for (PurchaseRequisition pr : prList) {
                    dataToSave.add(pr.toFileString());
                }
                boolean success = FileIO.writeToFile(rootFilePath + filePath, dataToSave, false); // Overwrite
                if (!success) {
                    System.err.println("ERROR: Failed to rewrite file after deletion for PR: " + purchaseRequisition.getPRID());
                }
                return success;
            }
            return false;
        }
    }

    public PurchaseRequisition getPRById(String prID) {
        loadPRData(); // Refresh data to ensure accuracy
        PurchaseRequisition foundPr = prList.stream()
                .filter(pr -> pr.getPRID().equalsIgnoreCase(prID))
                .findFirst()
                .orElse(null);
        return foundPr;
    }

    public List<PurchaseRequisition> loadApprovedPRData() {
        List<String> data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
        List<PurchaseRequisition> approvedPrs = new ArrayList<>(); // Use a local list for approved PRs

        int startIndex = 0;
        if (data.size() > 0 && data.get(0).trim().equalsIgnoreCase(PR_HEADER.trim())) {
            startIndex = 1;
        }
        for (int i = startIndex; i < data.size(); i++) {
            PurchaseRequisition pr = PurchaseRequisition.fromFileString(data.get(i));
            if (pr != null && "Approved".equalsIgnoreCase(pr.getStatus())) {
                approvedPrs.add(pr);
            }
        }
        return approvedPrs; // Return the list of approved PRs
    }
}
