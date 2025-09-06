package Management.Model;

public class Item {
    private String itemID;
    private String itemName;
    private double price; // Selling price
    private int quantity; // Current stock quantity
    private String stock;
    private String category;
    private String brand;
    private String imagePath;

    // Original 8-argument constructor (preserved)
    public Item(String itemID, String itemName, double price, int quantity, String stock, String category, String brand, String imagePath) {
        this.itemID = itemID;
        this.itemName = itemName;
        this.price = price;
        this.quantity = quantity;
        this.stock = stock;
        this.category = category;
        this.brand = brand;
        this.imagePath = imagePath;
    }


    // --- Getters ---
    public String getItemID() {
        return itemID;
    }

    public String getItemName() {
        return itemName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStock() { return stock; }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public String getImagePath() {
        return imagePath;
    }

    // --- Setters ---
    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setStock(String stock) {
        this.stock = stock;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }


    /**
     * Overrides the toString method to display both item ID and item name,
     * which is used by JComboBox to render items in the dropdown.
     * @return A formatted string combining item ID and name.
     */
    @Override
    public String toString() {
        return itemID + " - " + itemName;
    }

    // Add this method for debugging
    public String toUpdateItemString() {
        return String.join(",",
                itemID,
                itemName,
                String.format("%.2f", price),
                String.valueOf(quantity),
                stock,
                category != null ? category : "",
                brand != null ? brand : "",
                imagePath != null ? imagePath.replace("Itemimage/", "") : ""
        );
    }

    // --- File Conversion ---
    public String toFileString() {
        // Now writes 9 fields, including costPrice
        return String.join(",", itemID, itemName, String.format("%.2f", price), String.valueOf(quantity),
                stock, safe(category), safe(brand), safe(imagePath));
    }

    public static Item fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length == 8) { // Old format (no costPrice)
            try {
                return new Item(
                        parts[0].trim(), // itemID
                        parts[1].trim(), // itemName
                        Double.parseDouble(parts[2].trim()), // price
                        Integer.parseInt(parts[3].trim()), // quantity
                        parts[4].trim(), // stock
                        parts[5].trim(), // category
                        parts[6].trim(), // brand
                        parts[7].trim()  // imagePath
                );
            } catch (NumberFormatException e) {
                System.err.println("Error parsing 8-field Item data: " + line + " - " + e.getMessage());
            }
        }
        return null;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}
