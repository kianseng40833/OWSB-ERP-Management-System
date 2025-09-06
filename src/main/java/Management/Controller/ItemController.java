package Management.Controller;

import Management.Model.Item;
import Management.FileIO.FileIO;
import Management.Model.PurchaseRequisition; // This import might not be directly used in ItemController but is kept for context
import Management.Model.Supplierlist;       // This import might not be directly used in ItemController but is kept for context

import javax.imageio.ImageIO; // This import might not be directly used in ItemController but is kept for context
import javax.swing.*;         // Used for JOptionPane in saveItem/updateItem error messages
import java.awt.image.BufferedImage; // This import might not be directly used in ItemController but is kept for context
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher; // Required for generateNextItemId
import java.util.regex.Pattern; // Required for generateNextItemId

public class ItemController {
    private final String rootFilePath = "src/main/resources/";
    private final String itemFilePath = "item.txt";
    private final String pictureFilePath = "Itemimage/";
    private final List<Item> itemList = new ArrayList<>();

    public ItemController() {
        createImageDirectory();
        loadItems();
    }

    /**
     * Creates the directory for storing item images if it doesn't already exist.
     * Prints an error message to System.err if directory creation fails.
     */
    private void createImageDirectory() {
        File directory = new File(rootFilePath + pictureFilePath);
        if (!directory.exists() && !directory.mkdirs()) {
            System.err.println("Failed to create image directory: " + directory.getAbsolutePath());
        }
    }

    /**
     * Loads item data from the item.txt file into the in-memory itemList.
     * Clears the existing list before loading to ensure a fresh state.
     * Ensures image paths are relative to the 'Itemimage/' folder.
     *
     * @return The loaded list of items.
     */
    public List<Item> loadItems() {
        String filePath = rootFilePath + itemFilePath;
        List<String> lines = FileIO.readFromFile(filePath, Integer.MAX_VALUE);
        itemList.clear();

        for (String line : lines) {
            // Skip empty lines and the header line
            if (line.trim().isEmpty() || line.trim().startsWith("ItemID")) continue;

            Item item = Item.fromFileString(line);
            if (item != null) {
                // Ensure image path is relative to Itemimage folder for internal consistency
                // If the path from file doesn't start with 'Itemimage/', prepend it.
                if (item.getImagePath() != null && !item.getImagePath().startsWith(pictureFilePath)) {
                    item.setImagePath(pictureFilePath + item.getImagePath());
                }
                itemList.add(item);
            }
        }
        return itemList;
    }

    /**
     * Returns the current in-memory list of items.
     *
     * @return A list of Item objects.
     */
    public List<Item> getItemList() {
        return itemList;
    }

    /**
     * Generates the next available item ID in the format I0001, I0002, etc.
     * It finds the highest existing numeric ID (e.g., from I0012, it takes 12)
     * and increments it to produce the next sequential ID.
     *
     * @return The next unique item ID string.
     */
    public String generateNextItemId() {
        int maxIdNum = 0;
        // Pattern to match "I" followed by exactly four digits (e.g., I0001, I1234)
        Pattern pattern = Pattern.compile("^I(\\d{4})$");

        for (Item item : itemList) {
            Matcher matcher = pattern.matcher(item.getItemID());
            if (matcher.matches()) {
                try {
                    // Extract the numeric part (group 1) and parse it
                    int idNum = Integer.parseInt(matcher.group(1));
                    if (idNum > maxIdNum) {
                        maxIdNum = idNum;
                    }
                } catch (NumberFormatException e) {
                    // Log an error if an item ID matches the pattern but its numeric part is unparseable
                    System.err.println("Error parsing item ID number: " + item.getItemID() + " - " + e.getMessage());
                }
            }
        }
        // Increment the maximum found number and format it back to "IXXXX" with leading zeros
        return String.format("I%04d", maxIdNum + 1);
    }

    /**
     * Saves a new item to the system.
     * If the item's ID is not set, it generates a new sequential ID.
     * Processes the item's image path and saves the item to the file.
     *
     * @param item The Item object to save.
     * @return true if the item was successfully saved, false otherwise.
     */
    public boolean saveItem(Item item) {
        if (item == null) return false;

        // If the item ID is not already set (e.g., for a new item from the dialog), generate one.
        // This allows the ItemDialog to pre-fill the ID, but the controller ensures it's unique.
        if (item.getItemID() == null || item.getItemID().isEmpty()) {
            item.setItemID(generateNextItemId());
        } else {
            // If an ID is provided (e.g., from a form where ID might be manually entered or pre-filled),
            // check for uniqueness to prevent accidental overwrites or duplicates for new items.
            // For updates, the updateItem method should be used.
            if (getItemById(item.getItemID()) != null) {
                JOptionPane.showMessageDialog(null,
                        "Item with ID " + item.getItemID() + " already exists. Please use a different ID or update the existing item.",
                        "Duplicate Item ID",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        try {
            // Process image path using itemID as the filename, ensuring it's moved/copied correctly.
            String processedPath = ImageController.processImagePath(
                    item.getImagePath(),
                    item.getItemID()
            );
            item.setImagePath(processedPath); // Update the item object with the new processed path

            // Add the new item to the in-memory list
            itemList.add(item);
            // Save the entire list to file to ensure consistency and avoid partial writes.
            saveAllItems(itemList);
            return true;

        } catch (IOException e) {
            System.err.println("Image processing failed during save: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "Failed to save item image: " + e.getMessage(),
                    "Image Error",
                    JOptionPane.ERROR_MESSAGE);
            // If image saving fails, remove the item from the in-memory list to prevent inconsistencies.
            itemList.remove(item);
        }
        return false;
    }

    /**
     * Saves the entire list of items to the item.txt file, overwriting its contents.
     * This method is used to persist all changes (additions, deletions, updates).
     * It temporarily strips the folder prefix from image paths for file storage,
     * then restores them for the in-memory list.
     *
     * @param items The list of Item objects to save.
     */
    public void saveAllItems(List<Item> items) {
        if (items == null) return;

        List<String> data = new ArrayList<>();
        data.add("ItemID,ItemName,Price,Quantity,StorageStock,Category,Brand,imageID");

        for (Item item : items) {
            // Store original values
            String originalStorageStock = item.getStock();
            String originalPath = item.getImagePath();

            // Handle null/empty storageStock
            if (originalStorageStock == null || originalStorageStock.trim().isEmpty()) {
                item.setStock("0"); // Convert to "0"
            }

            // Handle image path formatting
            item.setImagePath(stripFolderPrefix(originalPath));

            // Add to data
            data.add(item.toFileString());

            // Restore original values to maintain object integrity
            item.setImagePath(originalPath);
        }

        FileIO.writeToFile(rootFilePath + itemFilePath, data, false);
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param itemId The ID of the item to retrieve.
     * @return The Item object if found, null otherwise.
     */
    public Item getItemById(String itemId) {
        if (itemId == null) return null;

        return itemList.stream()
                .filter(item -> itemId.equalsIgnoreCase(item.getItemID()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Deletes an item from the system by its ID.
     * Also attempts to delete the associated image file.
     *
     * @param itemId The ID of the item to delete.
     * @return true if the item was successfully deleted, false otherwise.
     */
    public boolean deleteItem(String itemId) {
        Item item = getItemById(itemId);
        if (item != null) {
            deleteImageFile(item.getImagePath()); // Delete the associated image file
            itemList.remove(item); // Remove from in-memory list
            saveAllItems(itemList); // Save changes to file
            return true;
        }
        return false;
    }

    /**
     * Updates an existing item in the system.
     * Processes the new image path, deletes the old image if it has changed,
     * updates the item in the in-memory list, and then saves all items to file.
     * Also updates related supplier information if the item name changes.
     *
     * @param updatedItem The Item object with updated information.
     * @return true if the item was successfully updated, false otherwise.
     */
    public boolean updateItem(Item updatedItem) {
        if (updatedItem == null) return false;

        for (int i = 0; i < itemList.size(); i++) {
            Item existingItem = itemList.get(i);
            if (updatedItem.getItemID().equalsIgnoreCase(existingItem.getItemID())) {
                try {
                    // Process image path with itemID as filename for the updated item
                    String processedPath = ImageController.processImagePath(
                            updatedItem.getImagePath(),
                            updatedItem.getItemID()
                    );
                    updatedItem.setImagePath(processedPath);

                    // Delete the old image file if the image path has changed
                    if (!existingItem.getImagePath().equals(processedPath)) {
                        ImageController.deleteImage(existingItem.getImagePath());
                    }

                    itemList.set(i, updatedItem); // Update the item in the in-memory list

                    // If the item name has changed, update it in related supplier lists
                    if (!existingItem.getItemName().equals(updatedItem.getItemName())) {
                        SupplierController supplierController = new SupplierController();
                        supplierController.updateSuppliersForItem(
                                updatedItem.getItemID(),
                                updatedItem.getItemName()
                        );
                    }

                    saveAllItems(itemList); // Save all changes to file
                    return true;
                } catch (IOException e) {
                    System.err.println("Image processing failed during update: " + e.getMessage());
                    JOptionPane.showMessageDialog(null,
                            "Failed to update item image: " + e.getMessage(),
                            "Image Error",
                            JOptionPane.ERROR_MESSAGE);
                    // If image update fails, revert the item in the list to its original state
                    // and save to maintain consistency.
                    itemList.set(i, existingItem);
                    saveAllItems(itemList);
                    return false; // Indicate that the update (or at least image part) failed
                }
            }
        }
        return false; // Item with the given ID was not found for update
    }

    /**
     * Deletes an image file from the file system.
     * Checks if the path is valid and if the file exists before attempting deletion.
     *
     * @param imagePath The path to the image file (relative to rootFilePath).
     */
    private void deleteImageFile(String imagePath) {
        // Only attempt to delete if the path is not null and starts with the expected picture folder prefix
        if (imagePath == null || !imagePath.startsWith(pictureFilePath)) return;

        File imageFile = new File(rootFilePath + imagePath);
        if (imageFile.exists() && !imageFile.delete()) {
            System.err.println("Failed to delete image: " + imageFile.getAbsolutePath());
        }
    }

    /**
     * Strips the 'Itemimage/' folder prefix from an image path.
     * This is used when saving paths to the text file to keep them cleaner.
     *
     * @param imagePath The full or relative image path.
     * @return The image path without the 'Itemimage/' prefix, or null if input was null.
     */
    private String stripFolderPrefix(String imagePath) {
        if (imagePath == null) return null;
        return imagePath.startsWith(pictureFilePath) ?
                imagePath.substring(pictureFilePath.length()) :
                imagePath;
    }

    public boolean transferStock(String itemId, int amountToTransfer) {
        Item item = getItemById(itemId);
        if (item == null) return false;

        int currentStorageStock = Integer.parseInt(item.getStock());
        int currentQuantity = item.getQuantity();

        // Validate transfer amount
        if (amountToTransfer <= 0 || amountToTransfer > currentStorageStock || amountToTransfer > 200) {
            return false;
        }

        // Update quantities
        item.setStock(String.valueOf(currentStorageStock - amountToTransfer));
        item.setQuantity(currentQuantity + amountToTransfer);

        return updateItem(item);
    }
}
