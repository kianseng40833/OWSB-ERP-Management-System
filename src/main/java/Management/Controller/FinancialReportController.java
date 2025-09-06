package Management.Controller;

import Management.FileIO.FileIO;
import Management.Model.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional; // Added for Optional
import java.util.function.Function; // Added for Function
import java.util.stream.Collectors;
import java.util.Collections; // Added for Collections
import java.util.Objects; // Added for Objects

public class FinancialReportController {

    private final String rootFilePath = "src/main/resources/";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd"); // Added for date parsing

    // Controllers to access data
    private SalesController salesController;
    private PaymentController paymentController;
    private POController poController;
    private PRController prController;
    private ItemController itemController;
    private SupplierController supplierController;

    public FinancialReportController() {
        salesController = new SalesController();
        paymentController = new PaymentController();
        poController = new POController();
        prController = new PRController();
        itemController = new ItemController();
        supplierController = new SupplierController(); // Initialize SupplierController
    }

    public List<FinancialReportData> getDailyReport(LocalDate date) {
        return getReportForPeriod(date, date);
    }

    public List<FinancialReportData> getWeeklyReport(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        return getReportForPeriod(startDate, endDate);
    }

    public List<FinancialReportData> getMonthlyReport(LocalDate dateInMonth) {
        YearMonth yearMonth = YearMonth.from(dateInMonth);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();
        return getReportForPeriod(startDate, endDate);
    }

    public List<FinancialReportData> getYearlyReport(int year) {
        LocalDate startDate = LocalDate.of(year, 1, 1);
        LocalDate endDate = LocalDate.of(year, 12, 31);
        return getReportForPeriod(startDate, endDate);
    }

    private List<FinancialReportData> getReportForPeriod(LocalDate startDate, LocalDate endDate) {
        List<FinancialReportData> reportData = new ArrayList<>();

        // 1. Get Sales Data
        List<Sales> allSales = salesController.getSalesList();
        for (Sales sale : allSales) {
            try {
                LocalDate saleDate = LocalDate.parse(sale.getDate());
                if (!saleDate.isBefore(startDate) && !saleDate.isAfter(endDate)) {
                    // Generate FinancialReportData from Sales using the new method
                    Optional<FinancialReportData> salesReport = generateFinancialReportFromSale(sale);
                    salesReport.ifPresent(reportData::add);
                }
            } catch (DateTimeParseException e) {
                System.err.println("Error parsing sale date: " + sale.getDate() + " - " + e.getMessage());
            }
        }

        // 2. Get Completed Purchase Order Data (from Payments)
        List<Payment> allPayments = paymentController.getPaymentList();
        for (Payment payment : allPayments) {
            if ("Completed".equalsIgnoreCase(payment.getStatus())) {
                try {
                    LocalDate paymentDate = LocalDate.parse(payment.getDate());
                    if (!paymentDate.isBefore(startDate) && !paymentDate.isAfter(endDate)) {
                        PurchaseOrder po = poController.getPOById(payment.getPo_id());
                        if (po != null) {
                            PurchaseRequisition pr = prController.getPRById(po.getPr_id());
                            if (pr != null) {
                                Item purchasedItem = itemController.getItemById(pr.getItem_id());
                                Supplierlist supplyPurchasedItem = supplierController.getSupplierByItemId(pr.getItem_id());
                                if (purchasedItem != null) {
                                    // Retrieve supplier unit price from Item model's costPrice
                                    double supplierUnitPrice = supplyPurchasedItem.getCost();

                                    int poQuantity = 0;
                                    try {
                                        poQuantity = Integer.parseInt(po.getQuantity());
                                    } catch (NumberFormatException e) {
                                        System.err.println("Error parsing PO quantity for PO " + po.getPo_id() + ": " + po.getQuantity() + " - " + e.getMessage());
                                        continue;
                                    }

                                    double totalPurchaseAmount = 0.0;
                                    try {
                                        totalPurchaseAmount = Double.parseDouble(payment.getTotalAmount());
                                    } catch (NumberFormatException e) {
                                        System.err.println("Error parsing total amount for payment " + payment.getPaymentId() + ": " + payment.getTotalAmount() + " - " + e.getMessage());
                                        continue;
                                    }

                                    reportData.add(new FinancialReportData(
                                            payment.getPaymentId(),
                                            po.getPo_id(),
                                            pr.getPr_id(),
                                            paymentDate,
                                            purchasedItem.getItemID(),
                                            purchasedItem.getItemName(),
                                            poQuantity,
                                            supplierUnitPrice,
                                            totalPurchaseAmount
                                    ));
                                } else {
                                    System.err.println("Warning: Item not found for PR ID: " + pr.getItem_id() + " in completed payment: " + payment.getPaymentId());
                                }
                            } else {
                                System.err.println("Warning: Purchase Requisition not found for PO ID: " + po.getPo_id() + " in completed payment: " + payment.getPaymentId());
                            }
                        } else {
                            System.err.println("Warning: Purchase Order not found for Payment ID: " + payment.getPaymentId());
                        }
                    }
                } catch (DateTimeParseException e) {
                    System.err.println("Error parsing payment date: " + payment.getDate() + " - " + e.getMessage());
                }
            }
        }

        return reportData;
    }

    /**
     * Generates a single FinancialReportData object from a Sales object.
     * This method attempts to find related item cost from suppliers,
     * but will still return a report even if those linked details are missing.
     * @param sale The Sales object to convert.
     * @return An Optional containing the FinancialReportData, or empty if critical data (like date) is missing.
     */
    private Optional<FinancialReportData> generateFinancialReportFromSale(Sales sale) {
        FinancialReportData report = new FinancialReportData(); // Use no-arg constructor

        // Set basic sales details from the Sales object
        try {
            report.setTransactionDate(LocalDate.parse(sale.getDate(), dateFormatter)); // Use setTransactionDate
        } catch (Exception e) {
            System.err.println("Error parsing sale date for report: " + sale.getDate());
            return Optional.empty(); // Cannot create report without a valid date
        }

        report.setReceiptNo(sale.getReceiptNo());
        report.setItemId(sale.getItemId());
        report.setItemName(sale.getItemName());
        report.setQuantity(sale.getQuantitySold());
        report.setUnitPrice(sale.getUnitPrice()); // This is the selling price per unit
        report.setRevenue(sale.getTotalPrice());

        // --- Determine the cost of the item from supplier data ---
        double supplierCostPerUnit = 0.0; // Default cost if not found

        // Find all supplier entries that supply this specific item
        List<Supplierlist> relevantSuppliers = supplierController.getSupplierList().stream()
                .filter(s -> s.getItemID().equalsIgnoreCase(sale.getItemId()))
                .collect(Collectors.toList());

        if (!relevantSuppliers.isEmpty()) {
            // IMPORTANT ASSUMPTION: If multiple suppliers provide the same item,
            // we are currently taking the cost from the first supplier found.
            supplierCostPerUnit = relevantSuppliers.get(0).getCost();
        } else {
            System.err.println("No supplier found for item ID: " + sale.getItemId() + ". Cost will be 0.0 for this sale.");
        }

        report.setCostOfGoodsSold(supplierCostPerUnit * sale.getQuantitySold()); // Set cost of goods sold
        // Profit calculation is done in FinanceReport.java's display method or can be added here
        // report.setProfit(report.getRevenue() - report.getCostOfGoodsSold());

        // PO ID and PR ID will remain null for sales entries
        report.setPoId(null);
        report.setPrId(null);
        report.setPaymentId(null); // No payment ID directly for sales
        report.setSupplierUnitPrice(0.0); // Not applicable for sales
        report.setPurchaseCost(0.0); // Not applicable for sales

        return Optional.of(report);
    }
}
