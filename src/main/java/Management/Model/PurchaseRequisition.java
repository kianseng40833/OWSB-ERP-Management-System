package Management.Model;

public class PurchaseRequisition {
    private String pr_id;
    private String item_id;
    private String stock;
    private String supplier_id;
    private String date;
    private String pic;
    private String status;
    private String remarks;

    public PurchaseRequisition(String pr_id, String item_id, String stock, String supplier_id, String date, String pic, String status, String remarks) {
        this.pr_id = pr_id;
        this.item_id = item_id;
        this.stock = stock;
        this.supplier_id = supplier_id;
        this.date = date;
        this.pic = pic;
        this.status = status;
        this.remarks = remarks;
    }

    public String getPRID() { return pr_id; }
    public String getItem_id() { return item_id; }
    public String getDate() { return date; }
    public String getPic() { return pic; }
    public String getStock() { return stock; } // This is the requested quantity from PR
    public String getSupplier_id() { return supplier_id; }
    public String getStatus() { return status; }
    public String getRemarks() { return remarks; }

    public String getItemId() {
        return this.item_id; // Make sure you have an itemId field
    }

    public String getPr_id() {
        return this.pr_id; // Make sure you have a pr_id field
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Setter for pr_id, needed for generateNewPrId in PRController
    public void setPr_id(String pr_id) {
        this.pr_id = pr_id;
    }

    // Convert object to CSV-style string for saving
    public String toFileString() {
        // Ensure remarks is never null when writing to file
        String safeRemarks = (this.remarks != null) ? this.remarks : "";
        return String.join(",", pr_id, item_id, stock, supplier_id, date, pic, status, safeRemarks);
    }

    // Parse string from file to PurchaseRequisition object
    public static PurchaseRequisition fromFileString(String line) {
        // Use -1 limit to ensure trailing empty strings are not discarded
        String[] parts = line.split(",", -1);

        // We expect 8 parts. If there are fewer than 7 (mandatory fields), it's invalid.
        // If there are exactly 7, remarks is implicitly empty.
        if (parts.length < 7) {
            System.err.println("Invalid data format (too few parts): " + line); // Keep error logging for invalid format
            return null;
        }

        // Get remarks if available, else default to an empty string
        String remarks = (parts.length > 7 && parts[7] != null) ? parts[7].trim() : "";

        return new PurchaseRequisition(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim(),
                parts[5].trim(),
                parts[6].trim(),
                remarks // Use the correctly determined remarks
        );
    }
}
