package Management.Model;

// Represents a single sales transaction.
public class Sales {
    private String receiptNo;
    private String date; // Format: YYYY-MM-DD
    private String itemId; // ID of the item sold
    private String itemName; // Name of the item sold (for display convenience)
    private int quantitySold;
    private double unitPrice; // Price of the item at the time of sale
    private double totalPrice; // Calculated total price for this sale line item
    private String salesPIC; // Person-In-Charge for the sale (optional)
    private String customerName; // Customer's name (optional)

    // Constructor for existing data that might not have salesPIC or customerName
    public Sales(String receiptNo, String date, String itemId, String itemName, int quantitySold, double unitPrice, double totalPrice) {
        this(receiptNo, date, itemId, itemName, quantitySold, unitPrice, totalPrice, "", ""); // Call full constructor with empty defaults
    }

    // Constructor including salesPIC (for older data before customerName was added)
    public Sales(String receiptNo, String date, String itemId, String itemName, int quantitySold, double unitPrice, double totalPrice, String salesPIC) {
        this(receiptNo, date, itemId, itemName, quantitySold, unitPrice, totalPrice, salesPIC, ""); // Call full constructor with empty customerName
    }

    // Full constructor including salesPIC and customerName
    public Sales(String receiptNo, String date, String itemId, String itemName, int quantitySold, double unitPrice, double totalPrice, String salesPIC, String customerName) {
        this.receiptNo = receiptNo;
        this.date = date;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantitySold = quantitySold;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.salesPIC = salesPIC;
        this.customerName = customerName;
    }

    // --- Getters ---
    public String getReceiptNo() { return receiptNo; }
    public String getDate() { return date; }
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantitySold() { return quantitySold; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalPrice() { return totalPrice; }
    public String getSalesPIC() { return salesPIC; }
    public String getCustomerName() { return customerName; }

    // --- Setters (if needed for updates, though sales are often immutable) ---
    public void setQuantitySold(int quantitySold) { this.quantitySold = quantitySold; }
    public void setTotalPrice(double totalPrice) { this.totalPrice = totalPrice; }
    public void setSalesPIC(String salesPIC) { this.salesPIC = salesPIC; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    // --- File Conversion ---
    // Converts the Sales object to a CSV-formatted string for file storage.
    // Includes all fields: receiptNo,date,itemId,itemName,quantitySold,unitPrice,totalPrice,salesPIC,customerName
    public String toFileString() {
        return String.join(",",
                receiptNo,
                date,
                itemId,
                itemName,
                String.valueOf(quantitySold),
                String.format("%.2f", unitPrice), // Format price with 2 decimal places
                String.format("%.2f", totalPrice),  // Format total price with 2 decimal places
                salesPIC, // Include salesPIC
                customerName // Include customerName
        );
    }

    // Parses a CSV-formatted string from a file into a Sales object.
    // Handles different line formats (7, 8, or 9 fields) for backward compatibility.
    public static Sales fromFileString(String line) {
        String[] parts = line.split(",", -1); // Use -1 to keep trailing empty strings
        
        // Minimum fields required for a basic sale (7 fields)
        // Max fields with salesPIC and customerName (9 fields)
        if (parts.length < 7 || parts.length > 9) {
            System.err.println("Invalid Sales data format (incorrect number of fields): " + line + " (Expected 7-9 fields, got " + parts.length + ")");
            return null;
        }

        try {
            String parsedReceiptNo = parts[0].trim();
            String parsedDate = parts[1].trim();
            String parsedItemId = parts[2].trim();
            String parsedItemName = parts[3].trim();
            int parsedQuantitySold = Integer.parseInt(parts[4].trim());
            double parsedUnitPrice = Double.parseDouble(parts[5].trim());
            double parsedTotalPrice = Double.parseDouble(parts[6].trim());
            
            // Handle optional fields based on parts.length
            String parsedSalesPIC = (parts.length >= 8) ? parts[7].trim() : "";
            String parsedCustomerName = (parts.length == 9) ? parts[8].trim() : "";

            return new Sales(
                    parsedReceiptNo,
                    parsedDate,
                    parsedItemId,
                    parsedItemName,
                    parsedQuantitySold,
                    parsedUnitPrice,
                    parsedTotalPrice,
                    parsedSalesPIC,
                    parsedCustomerName
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing numeric data in Sales line: " + line + " - " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Unexpected error parsing Sales line: " + line + " - " + e.getMessage());
            return null;
        }
    }
}
