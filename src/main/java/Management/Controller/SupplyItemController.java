package Management.Controller;

import Management.FileIO.FileIO;
import Management.Model.*; // Import all models
import javax.swing.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections; // For Collections.max

public class SupplyItemController {
    private final String rootFilePath = "src/main/resources/";
    private final String filePath = "supplyItem.txt";
    private static final String SUPPLY_ITEM_HEADER = "SupplyItem_ID,Payment_ID,Item_ID,Item_Name,quantityOrdered,quantityReceived,suppliedDate,Status";
    private List<SupplyItem> supplyItemList = new ArrayList<>();

    // Dependencies for other controllers
    private ItemController itemController;

    public SupplyItemController() {
        loadSupplyItemData();
    }

    // Lazy initialization for ItemController
    private ItemController getItemController() {
        if (itemController == null) {
            itemController = new ItemController();
        }
        return itemController;
    }

    // Load all supply items from file
    public List<SupplyItem> loadSupplyItemData() {
        List<String> data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
        supplyItemList.clear();

        // Assuming the first line is a header, skip it.
        int startIndex = 1;
//        if (!data.isEmpty() && data.get(0).trim().equalsIgnoreCase(SUPPLY_ITEM_HEADER.trim())) {
//            startIndex = 1;
//        }

        for (int i = startIndex; i < data.size(); i++) {
            SupplyItem supplyItem = SupplyItem.fromFileString(data.get(i));
            if (supplyItem != null) {
                supplyItemList.add(supplyItem);
            }
        }
        return supplyItemList;
    }

    public List<SupplyItem> getSupplyItemList() {
        return new ArrayList<>(supplyItemList); // Return a copy to prevent external modification
    }

    // Add a new supply item
    public boolean addSupplyItem(SupplyItem supplyItem) {
        if (supplyItem == null) {
            System.err.println("Error: SupplyItem object is null when trying to add.");
            return false;
        }

        supplyItemList.add(supplyItem); // Add to in-memory list
        // Always append the new item. This method assumes the header is already present in the file
        // or will be handled by saveAllSupplyItems when called for updates/deletions.
        // For initial adds, we'll rely on the file being pre-created with a header,
        // or the saveAllSupplyItems() (if called later) to manage it.
        return saveAllSupplyItems(); // Call saveAllSupplyItems to ensure consistency immediately
    }

    // Update a supply item by its ID
    public boolean updateSupplyItemInFile(SupplyItem supplyItem) {
        loadSupplyItemData(); // Ensure the in-memory list is fresh

        boolean updated = false;
        for (int i = 0; i < supplyItemList.size(); i++) {
            SupplyItem existingSupplyItem = supplyItemList.get(i);
            if (existingSupplyItem.getSupplyItem_ID().equals(supplyItem.getSupplyItem_ID())) {
                // Check if status is being changed to "Completed"
                boolean isBeingCompleted = "Completed".equalsIgnoreCase(supplyItem.getStatus()) &&
                        !"Completed".equalsIgnoreCase(existingSupplyItem.getStatus());

                // Update the in-memory list
                supplyItemList.set(i, supplyItem);
                updated = true;

                // If supply item is being completed, update the item stock in inventory
                if (isBeingCompleted) {
                    updateItemStock(supplyItem);
                }
                break;
            }
        }

        if (updated) {
            return saveAllSupplyItems(); // Save the entire updated list, ensuring header is pre-existing
        }
        return false;
    }

    /**
     * Updates the stock of the corresponding item in the inventory.
     * This is called when a SupplyItem's status changes to "Completed",
     * meaning the goods have been received.
     * @param supplyItem The SupplyItem whose receipt triggers the stock update.
     */
    private void updateItemStock(SupplyItem supplyItem) {
        // 1. Get the item controller instance
        ItemController itemController = getItemController();

        // 2. Find the corresponding item
        Item item = itemController.getItemById(supplyItem.getItem_ID());

        if (item == null) {
            System.err.println("Item not found with ID: " + supplyItem.getItem_ID());
            JOptionPane.showMessageDialog(null,
                    "Item not found in inventory for Supply Item: " + supplyItem.getSupplyItem_ID(),
                    "Stock Update Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 3. Validate and parse current stock from the Item
            String currentStockStr = item.getStock();
            if (currentStockStr == null || currentStockStr.trim().isEmpty()) {
                currentStockStr = "0"; // Default to 0 if empty or null
            }
            int currentStock = Integer.parseInt(currentStockStr);

            // 4. Validate and parse the quantity received from the SupplyItem
            String quantityReceivedStr = supplyItem.getQuantityReceived();
            if (quantityReceivedStr == null || quantityReceivedStr.trim().isEmpty()) {
                quantityReceivedStr = "0"; // Default to 0 if empty or null
            }
            int quantityReceived = Integer.parseInt(quantityReceivedStr);

            // 5. Calculate new stock value
            int newStock = currentStock + quantityReceived;

            // 6. Update the item's stock in the Item object
            item.setStock(String.valueOf(newStock));

            // 7. Save the updated item back to its file
            if (!itemController.updateItem(item)) {
                System.err.println("Failed to update item stock in file for item ID: " + item.getItemID());
                JOptionPane.showMessageDialog(null,
                        "Failed to update item stock in inventory for Item: " + item.getItemName(),
                        "Stock Update Error",
                        JOptionPane.ERROR_MESSAGE);
            } else {
                System.out.println("Successfully updated stock for item " + item.getItemName() + " to " + newStock);
            }
        } catch (NumberFormatException e) {
            System.err.println("Invalid stock value format during update: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Invalid stock value format. Please check the stock values for item " + item.getItemName() + ".",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean deleteFile(SupplyItem supplyItem) {
        if (supplyItem == null) {
            System.err.println("Error: SupplyItem object is null when trying to delete.");
            return false;
        }
        if ("Completed".equalsIgnoreCase(supplyItem.getStatus())) {
            JOptionPane.showMessageDialog(null,
                    "Completed Supply Items cannot be deleted.",
                    "Deletion Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        } else {
            loadSupplyItemData(); // Ensure the in-memory list is fresh
            boolean deleted = supplyItemList.removeIf(si -> si.getSupplyItem_ID().equals(supplyItem.getSupplyItem_ID()));

            if (deleted) {
                return saveAllSupplyItems(); // Save the entire list after deletion, assuming header is pre-existing
            }
            return false;
        }
    }

    /**
     * Helper method to save the entire in-memory list of SupplyItems to the file.
     * This method assumes the header is already present in the file and only writes the data rows.
     * It reads the existing file content to preserve the header, then overwrites the file.
     * @return true if save is successful, false otherwise.
     */
    private boolean saveAllSupplyItems() {
        List<String> linesToWrite = new ArrayList<>();
        String headerLine = null;

        // Read the existing file to get the current header (if any)
        List<String> existingFileContent = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE); // Read all lines
        if (!existingFileContent.isEmpty()) {
            // Assume the first line is the header and preserve it.
            headerLine = existingFileContent.get(0);
        }

        // If a header line was found, add it back to the list to write
        if (headerLine != null) {
            linesToWrite.add(headerLine);
        }
        // If no header was found (e.g., brand new empty file), we don't add one here,
        // respecting the user's request that the controller doesn't "add" the header.
        // The expectation is that the file will be pre-populated with a header.

        // Add all current supply items from the in-memory list
        for (SupplyItem item : supplyItemList) {
            linesToWrite.add(item.toFileString());
        }

        // Overwrite the file with the header (if found) and the updated data
        return FileIO.writeToFile(rootFilePath + filePath, linesToWrite, false);
    }

    /**
     * Generates a unique Supply Item ID.
     * Format: SI_POXXXX_ITEMYYY_ZZZ (where ZZZ is a sequential number for that PO-Item combination)
     * @param poId The Purchase Order ID.
     * @param itemId The Item ID.
     * @return A unique Supply Item ID string.
     */
    public String generateNextSupplyItemId(String poId, String itemId) {
        loadSupplyItemData(); // Ensure the list is up-to-date

        String baseIdPrefix = "SI_" + poId + "_" + itemId;
        int maxSuffix = 0;

        for (SupplyItem si : supplyItemList) {
            if (si.getSupplyItem_ID().startsWith(baseIdPrefix)) {
                try {
                    String suffixStr = si.getSupplyItem_ID().substring(baseIdPrefix.length() + 1); // +1 for '_'
                    int currentSuffix = Integer.parseInt(suffixStr);
                    if (currentSuffix > maxSuffix) {
                        maxSuffix = currentSuffix;
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    // Ignore malformed IDs
                    System.err.println("Warning: Malformed Supply Item ID encountered: " + si.getSupplyItem_ID());
                }
            }
        }
        return baseIdPrefix + "_" + String.format("%03d", maxSuffix + 1);
    }
}
