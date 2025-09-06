package Management.Controller;

import Management.FileIO.FileIO;
import Management.Model.Payment;
import Management.Model.PurchaseOrder;
import Management.Model.PurchaseRequisition;
import Management.Model.Sales;
import java.util.List;
import java.util.ArrayList;

public class DashboardController {
    private static final String oriFilePath = "src/main/resources/";
    private static final String PO_FILE = "purchaseOrder.txt";
    private static final String PR_FILE = "purchaseRequisition.txt";
    private static final String SALES_FILE = "sales.txt";
    private static final String PAYMENTS_FILE = "payment.txt";

    private List<Payment> paymentList = new ArrayList<>();
    private List<PurchaseOrder> purchaseOrderList = new ArrayList<>();
    private List<PurchaseRequisition> prList = new ArrayList<>();
    private List<Sales> salesList = new ArrayList<>();

    public DashboardSummary loadCountedData() {
        loadPaymentData();
        loadPurchaseOrderData();
        loadPurchaseRequisitionData();
        loadSalesData();

        return new DashboardSummary(
                purchaseOrderList.size(),
                prList.size(),
                salesList.size(),
//                calculateTotalSales(),
                paymentList.size()
//                calculateTotalPayments()
        );
    }

    public List<Payment> loadPaymentData() {
        List<String> data = FileIO.readFromFile(oriFilePath + PAYMENTS_FILE, Integer.MAX_VALUE);
        paymentList.clear();

        // Skip header row
        for (int i = 1; i < data.size(); i++) {
            Payment payment = Payment.fromFileString(data.get(i));
            if (payment != null) {
                paymentList.add(payment);
            }
        }
        return paymentList;
    }

    public List<PurchaseOrder> loadPurchaseOrderData() {
        List<String> data = FileIO.readFromFile(oriFilePath + PO_FILE, Integer.MAX_VALUE);
        purchaseOrderList.clear();

        // Skip header row
        for (int i = 1; i < data.size(); i++) {
            PurchaseOrder po = PurchaseOrder.fromFileString(data.get(i));
            if (po != null) {
                purchaseOrderList.add(po);
            }
        }
        return purchaseOrderList;
    }

    public List<PurchaseRequisition> loadPurchaseRequisitionData() {
        List<String> data = FileIO.readFromFile(oriFilePath + PR_FILE, Integer.MAX_VALUE);
        prList.clear();

        // Skip header row
        for (int i = 1; i < data.size(); i++) {
            PurchaseRequisition pr = PurchaseRequisition.fromFileString(data.get(i));
            if (pr != null) {
                prList.add(pr);
            }
        }
        return prList;
    }

    public List<Sales> loadSalesData() {
        List<String> data = FileIO.readFromFile(oriFilePath + SALES_FILE, Integer.MAX_VALUE);
        salesList.clear();

        // Skip header row
        for (int i = 1; i < data.size(); i++) {
            Sales sale = Sales.fromFileString(data.get(i));
            if (sale != null) {
                salesList.add(sale);
            }
        }
        return salesList;
    }

//    private double calculateTotalSales() {
//        double total = 0;
//        for (Sales sale : salesList) {
//            total += sale.getTotalPrice();
//        }
//        return total;
//    }
//
//    private double calculateTotalPayments() {
//        double total = 0;
//        for (Payment payment : paymentList) {
//            total += Double.parseDouble(payment.getTotalAmount());
//        }
//        return total;
//    }
//
    public record DashboardSummary(
            int purchaseOrderCount,
            int purchaseRequisitionCount,
            int salesCount,
//            double totalSalesAmount,
            int paymentCount
//            double totalPaymentAmount
    ) {}
}