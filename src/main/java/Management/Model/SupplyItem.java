package Management.Model;

public class SupplyItem {
    private String SupplyItem_ID;
    private String Payment_ID;
    private String Item_ID;
    private String Item_Name;
    private String quantityOrdered; // Renamed from 'Stock' for clarity: This is the quantity ordered in the PO
    private String quantityReceived; // Renamed from 'storageStock' for clarity: This is the quantity actually received
    private String suppliedDate;
    private String Status;

    public SupplyItem(String SupplyItem_ID, String Payment_ID, String Item_ID, String Item_Name, String quantityOrdered, String quantityReceived, String suppliedDate, String Status) {
        this.SupplyItem_ID = SupplyItem_ID;
        this.Payment_ID = Payment_ID;
        this.Item_ID = Item_ID;
        this.Item_Name = Item_Name;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived;
        this.suppliedDate = suppliedDate;
        this.Status = Status;
    }

    // Getters
    public String getSupplyItem_ID() { return SupplyItem_ID; }
    public String getPayment_ID() { return Payment_ID; }
    public String getItem_ID() { return Item_ID; }
    public String getItem_Name() { return Item_Name; }
    public String getQuantityOrdered() { return quantityOrdered; } // Getter for new name
    public String getQuantityReceived() { return quantityReceived; } // Getter for new name
    public String getSuppliedDate() { return suppliedDate; }
    public String getStatus() { return Status; }

    // Setters
    public void setStatus(String status) {
        this.Status = status;
    }

    public void setPayment_ID(String payment_ID) { // Added setter for Payment_ID
        this.Payment_ID = payment_ID;
    }

    public void setQuantityOrdered(String quantityOrdered) { // Added setter for quantityOrdered
        this.quantityOrdered = quantityOrdered;
    }

    public void setQuantityReceived(String quantityReceived) { // Added setter for quantityReceived
        this.quantityReceived = quantityReceived;
    }

    public void setSuppliedDate(String suppliedDate) { // Added setter for suppliedDate
        this.suppliedDate = suppliedDate;
    }

    // Convert object to CSV-style string for saving
    public String toFileString() {
        return String.join(",", SupplyItem_ID, Payment_ID, Item_ID, Item_Name, quantityOrdered, quantityReceived, suppliedDate, Status);
    }

    // Parse string from file to SupplyItem object
    public static SupplyItem fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 8) {
            System.err.println("Invalid data format for SupplyItem: " + line);
            return null;  // Return null if the data is invalid
        }
        return new SupplyItem(
                parts[0].trim(), // SupplyItem_ID
                parts[1].trim(), // Payment_ID
                parts[2].trim(), // Item_ID
                parts[3].trim(), // Item_Name
                parts[4].trim(), // quantityOrdered (formerly Stock)
                parts[5].trim(), // quantityReceived (formerly storageStock)
                parts[6].trim(), // suppliedDate
                parts[7].trim()  // Status
        );
    }
}
