package Management.Model;

public class PurchaseOrder {
    private String po_id;
    private String pr_id;
    private String quantity;
    private String date;
    private String status;
    private String totalAmount;

    public PurchaseOrder(String po_id, String pr_id, String quantity, String date, String status, String totalAmount) {
        this.po_id = po_id;
        this.pr_id = pr_id;
        this.quantity = quantity;
        this.date = date;
        this.status = status;
        this.totalAmount = totalAmount;
    }

    public String getPo_id() { return po_id; }
    public String getPr_id() { return pr_id; }
    public String getQuantity() { return quantity; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public String getTotalAmount() { return totalAmount; }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setPo_id(String po_id) {
        this.po_id = po_id;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String toFileString() {
        return String.join(",", po_id, pr_id, quantity, date, status, totalAmount);
    }

    public static PurchaseOrder fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 6) {
            System.err.println("Invalid data format: " + line);
            return null;
        }
        return new PurchaseOrder(
                parts[0].trim(),
                parts[1].trim(),
                parts[2].trim(),
                parts[3].trim(),
                parts[4].trim(),
                parts[5].trim()
        );
    }
}