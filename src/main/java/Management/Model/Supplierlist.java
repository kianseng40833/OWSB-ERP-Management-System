package Management.Model;

public class Supplierlist {
    private String SupplierID;
    private String SupplierCompanyName;
    private String Contact;
    private String Email;
    private String PIC;
    private String ItemID;
    private String ItemName;
    private double Cost; // Changed from String to double
    private String Status;

    public Supplierlist(String SupplierID, String SupplierCompanyName, String Contact, String Email, String PIC, String ItemID, String ItemName, double Cost, String Status) { // Changed Cost parameter type
        this.SupplierID = SupplierID;
        this.SupplierCompanyName = SupplierCompanyName;
        this.Contact = Contact;
        this.Email = Email;
        this.PIC = PIC;
        this.ItemID = ItemID;
        this.ItemName = ItemName;
        this.Cost = Cost;
        this.Status = Status;
    }

    public String getSupplierID() { return SupplierID; }
    public String getSupplierCompanyName() { return SupplierCompanyName; }
    public String getContact() { return Contact; }
    public String getEmail() { return Email; }
    public String getPIC() { return PIC; }
    public String getItemID() { return ItemID; }
    public String getItemName() { return ItemName; }
    public double getCost() { return Cost; } // Changed return type to double
    public String getStatus() { return Status; }

    public void setStatus(String status) {
        this.Status = status;
    }

    public void updateItemInfo(String newItemName) {
        this.ItemName = newItemName;
    }

    @Override
    public String toString() {
        return SupplierCompanyName;
    }

    public String toFileString(String header) {
        // Convert double Cost to String for file writing
        return String.join(",", SupplierID, SupplierCompanyName, Contact, Email, PIC, ItemID, ItemName, String.valueOf(Cost), Status);
    }

    // Parse string from file to Supplierlist object
    public static Supplierlist fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 9) {
            System.err.println("Invalid data format for Supplierlist: " + line);
            return null;
        }
        try {
            // Parse Cost from String to double
            double cost = Double.parseDouble(parts[7].trim());
            return new Supplierlist(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim(),
                parts[5].trim(),
                parts[6].trim(),
                cost, // Pass the parsed double
                parts[8].trim()
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing cost for Supplierlist: " + parts[7].trim() + " in line: " + line);
            return null;
        }
    }
}
