package Management.Model;

import java.time.LocalDate;

public class FinancialReportData {
    // Common fields
    private LocalDate transactionDate;
    private String itemId;
    private String itemName;
    private int quantity;

    // Sales-specific fields
    private String receiptNo;
    private double unitPrice; // Selling unit price
    private double revenue;
    private double costOfGoodsSold; // Cost of the item sold
    private String salesPIC;
    private String customerName;

    // Purchase Order-specific fields
    private String paymentId;
    private String poId;
    private String prId;
    private double supplierUnitPrice; // Unit cost from supplier for the PO item
    private double purchaseCost; // Total cost of the PO (from payment)

    // NEW: No-argument constructor for flexible object creation
    public FinancialReportData() {
        // Initialize all fields to default values
        this.transactionDate = null;
        this.itemId = null;
        this.itemName = null;
        this.quantity = 0;
        this.receiptNo = null;
        this.unitPrice = 0.0;
        this.revenue = 0.0;
        this.costOfGoodsSold = 0.0;
        this.salesPIC = null;
        this.customerName = null;
        this.paymentId = null;
        this.poId = null;
        this.prId = null;
        this.supplierUnitPrice = 0.0;
        this.purchaseCost = 0.0;
    }

    // Original Constructor for Sales entries (kept for compatibility)
    public FinancialReportData(String receiptNo, LocalDate transactionDate, String itemId, String itemName,
                               int quantity, double unitPrice, double revenue, double costOfGoodsSold,
                               String salesPIC, String customerName) {
        this.receiptNo = receiptNo;
        this.transactionDate = transactionDate;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.revenue = revenue;
        this.costOfGoodsSold = costOfGoodsSold;
        this.salesPIC = salesPIC;
        this.customerName = customerName;

        this.paymentId = null;
        this.poId = null;
        this.prId = null;
        this.supplierUnitPrice = 0.0;
        this.purchaseCost = 0.0;
    }

    // Original Constructor for Purchase Order entries (kept for compatibility)
    public FinancialReportData(String paymentId, String poId, String prId, LocalDate transactionDate,
                               String itemId, String itemName, int quantity, double supplierUnitPrice, double purchaseCost) {
        this.paymentId = paymentId;
        this.poId = poId;
        this.prId = prId;
        this.transactionDate = transactionDate;
        this.itemId = itemId;
        this.itemName = itemName;
        this.quantity = quantity;
        this.supplierUnitPrice = supplierUnitPrice;
        this.purchaseCost = purchaseCost;

        this.receiptNo = null;
        this.unitPrice = 0.0;
        this.revenue = 0.0;
        this.costOfGoodsSold = 0.0;
        this.salesPIC = null;
        this.customerName = null;
    }

    // Getters for common fields
    public LocalDate getTransactionDate() { return transactionDate; }
    public String getItemId() { return itemId; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }

    // Getters for Sales-specific fields
    public String getReceiptNo() { return receiptNo; }
    public double getUnitPrice() { return unitPrice; }
    public double getRevenue() { return revenue; }
    public double getCostOfGoodsSold() { return costOfGoodsSold; }
    public String getSalesPIC() { return salesPIC; }
    public String getCustomerName() { return customerName; }

    // Getters for Purchase Order-specific fields
    public String getPaymentId() { return paymentId; }
    public String getPoId() { return poId; }
    public String getPrId() { return prId; }
    public double getSupplierUnitPrice() { return supplierUnitPrice; }
    public double getPurchaseCost() { return purchaseCost; }

    // NEW: Setters for all fields
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
    public void setItemId(String itemId) { this.itemId = itemId; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void setReceiptNo(String receiptNo) { this.receiptNo = receiptNo; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
    public void setRevenue(double revenue) { this.revenue = revenue; }
    public void setCostOfGoodsSold(double costOfGoodsSold) { this.costOfGoodsSold = costOfGoodsSold; }
    public void setSalesPIC(String salesPIC) { this.salesPIC = salesPIC; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
    public void setPoId(String poId) { this.poId = poId; }
    public void setPrId(String prId) { this.prId = prId; }
    public void setSupplierUnitPrice(double supplierUnitPrice) { this.supplierUnitPrice = supplierUnitPrice; }
    public void setPurchaseCost(double purchaseCost) { this.purchaseCost = purchaseCost; }
}
