package Management.Controller;

import Management.Model.Sales;
import Management.FileIO.FileIO; // Assuming you have a FileIO utility class
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class SalesController {
    private final String rootFilePath = "src/main/resources/";
    private final String filePath = "sales.txt"; // File to store sales data
    // FIX: Updated SALES_HEADER to correctly include customerName
    private static final String SALES_HEADER = "receiptNo,date,itemId,itemName,quantitySold,unitPrice,totalPrice,salesPIC,customerName";

    private List<Sales> salesList;

    public SalesController() {
        this.salesList = new ArrayList<>();
        loadSalesData(); // Load data on initialization
    }

    /**
     * Loads all sales records from the sales.txt file into the in-memory list.
     * Clears the existing list before loading to ensure a fresh state.
     * Handles cases where the header might be missing or outdated.
     * @return The loaded list of Sales objects.
     */
    public List<Sales> loadSalesData() {
        List<String> data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
        salesList.clear();

        boolean headerMatches = false;
        if (!data.isEmpty()) {
            // Check if the first line is the header, case-insensitive and trim-safe
            if (data.get(0).trim().equalsIgnoreCase(SALES_HEADER.trim())) {
                headerMatches = true;
            } else {
                // If header doesn't match, log a warning and assume it's an old/incorrect header
                System.err.println("WARNING: sales.txt header does not match expected format. Attempting to re-write header.");
            }
        }

        int startIndex = headerMatches ? 1 : 0; // Skip header if it matches, otherwise process all lines (or if file is empty)

        // If the file is empty or header doesn't match, ensure the correct header is written
        // This is crucial to prevent parsing errors from old/missing headers
        if (data.isEmpty() || !headerMatches) {
            // This will ensure the file starts with the correct header
            // If the file was empty, it will just write the header.
            // If it had an old header, it will overwrite it with the new correct one.
            saveAllSales();
            // After saving, reload to ensure the new header is properly skipped on next load
            data = FileIO.readFromFile(rootFilePath + filePath, Integer.MAX_VALUE);
            startIndex = 1; // Now we know the header is there and should be skipped
        }


        for (int i = startIndex; i < data.size(); i++) {
            String currentLine = data.get(i);
            if (currentLine.trim().isEmpty()) continue; // Skip empty lines

            Sales sales = Sales.fromFileString(currentLine);
            if (sales != null) {
                salesList.add(sales);
            } else {
                System.err.println("ERROR: Failed to parse line into Sales: '" + currentLine + "'. This line was NOT added to salesList.");
            }
        }
        return salesList;
    }

    /**
     * Returns the current in-memory list of sales.
     * @return A new ArrayList containing all Sales objects.
     */
    public List<Sales> getSalesList() {
        // Always return a copy to prevent external modification of the internal list
        return new ArrayList<>(salesList);
    }

    /**
     * Adds a new sales record to the system.
     * Generates a new receipt number if not already set.
     * @param sales The Sales object to add.
     * @return true if the sales record was successfully added, false otherwise.
     */
    public boolean addSales(Sales sales) {
        if (sales == null) return false;

        // Ensure sales list is up-to-date before checking for duplicates or generating ID
        loadSalesData();

        String newReceiptNo = sales.getReceiptNo();
        if (newReceiptNo == null || newReceiptNo.isEmpty()) {
            newReceiptNo = generateNextReceiptNo();
        } else {
            // Check for uniqueness if an ID is provided
            if (getSalesByReceiptNo(newReceiptNo) != null) {
                System.err.println("ERROR: Sales with Receipt No " + newReceiptNo + " already exists. Cannot add duplicate.");
                return false;
            }
        }
        
        // Create a new Sales object with the potentially newly generated receipt number,
        // existing salesPIC, AND customerName, using the 9-parameter constructor.
        Sales salesToAdd = new Sales(
            newReceiptNo,
            sales.getDate(),
            sales.getItemId(),
            sales.getItemName(),
            sales.getQuantitySold(),
            sales.getUnitPrice(),
            sales.getTotalPrice(),
            sales.getSalesPIC(), // Include salesPIC
            sales.getCustomerName() // Include customerName
        );

        salesList.add(salesToAdd); // Add to in-memory list
        return saveAllSales(); // Persist all sales to file
    }

    /**
     * Updates an existing sales record in the system.
     * @param updatedSales The Sales object with updated information.
     * @return true if the sales record was successfully updated, false otherwise.
     */
    public boolean updateSales(Sales updatedSales) {
        if (updatedSales == null) return false;

        loadSalesData(); // Ensure list is up-to-date
        boolean updated = false;
        for (int i = 0; i < salesList.size(); i++) {
            if (salesList.get(i).getReceiptNo().equalsIgnoreCase(updatedSales.getReceiptNo())) {
                salesList.set(i, updatedSales); // Replace the old object with the updated one
                updated = true;
                break;
            }
        }
        if (updated) {
            return saveAllSales(); // Persist all sales to file
        }
        return false;
    }

    /**
     * Deletes a sales record from the system by its receipt number.
     * @param receiptNo The receipt number of the sales record to delete.
     * @return true if the sales record was successfully deleted, false otherwise.
     */
    public boolean deleteSales(String receiptNo) {
        if (receiptNo == null || receiptNo.isEmpty()) return false;

        loadSalesData(); // Ensure list is up-to-date
        boolean deleted = salesList.removeIf(s -> s.getReceiptNo().equalsIgnoreCase(receiptNo));
        if (deleted) {
            return saveAllSales(); // Persist all sales to file
        }
        return false;
    }

    /**
     * Retrieves a sales record by its receipt number.
     * @param receiptNo The receipt number of the sales record to retrieve.
     * @return The Sales object if found, null otherwise.
     */
    public Sales getSalesByReceiptNo(String receiptNo) {
        if (receiptNo == null) return null;

        loadSalesData(); // Refresh data to ensure accuracy
        return salesList.stream()
                .filter(s -> s.getReceiptNo().equalsIgnoreCase(receiptNo))
                .findFirst()
                .orElse(null);
    }

    /**
     * Generates the next available receipt number in the format R000001, R000002, etc.
     * It finds the highest existing numeric ID (e.g., from R000123, it takes 123)
     * and increments it to produce the next sequential ID.
     * @return The next unique receipt number string.
     */
    public String generateNextReceiptNo() {
        loadSalesData(); // Ensure latest data for ID generation
        int maxNumber = 0;
        // Pattern to match "R" followed by digits
        Pattern pattern = Pattern.compile("^R(\\d+)$");

        for (Sales sales : salesList) {
            Matcher matcher = pattern.matcher(sales.getReceiptNo());
            if (matcher.matches()) {
                try {
                    int number = Integer.parseInt(matcher.group(1));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing Receipt No number: " + sales.getReceiptNo());
                }
            }
        }
        return String.format("R%06d", maxNumber + 1); // Format: R000001
    }

    /**
     * Saves the entire list of Sales objects to the file, overwriting its contents.
     * This method is a helper to ensure data consistency after additions, updates, or deletions.
     * @return true if the save operation was successful, false otherwise.
     */
    private boolean saveAllSales() {
        List<String> linesToSave = new ArrayList<>();
        linesToSave.add(SALES_HEADER); // Add the header line
        for (Sales sales : salesList) {
            linesToSave.add(sales.toFileString()); // This will now include salesPIC and customerName
        }
        return FileIO.writeToFile(rootFilePath + filePath, linesToSave, false); // Overwrite the file
    }

    /**
     * Calculates the total sales amount from all sales records.
     */
    public double getTotalSales() {
        loadSalesData();
        return salesList.stream()
                .mapToDouble(Sales::getTotalPrice)
                .sum();
    }

    /**
     * Calculates the daily sales amount for a specific date.
     */
    public double getDailySales(LocalDate date) {
        loadSalesData();
        String dateString = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return salesList.stream()
                .filter(sales -> sales.getDate().equals(dateString))
                .mapToDouble(Sales::getTotalPrice)
                .sum();
    }

    /**
     * Calculates the monthly sales amount for a specific year and month.
     */
    public double getMonthlySales(int year, int month) {
        loadSalesData();
        return salesList.stream()
                .filter(s -> {
                    try {
                        LocalDate saleDate = LocalDate.parse(s.getDate());
                        return saleDate.getYear() == year && saleDate.getMonthValue() == month;
                    } catch (Exception e) {
                        return false; // Ignore if date parsing fails
                    }
                })
                .mapToDouble(Sales::getTotalPrice)
                .sum();
    }

    /**
     * Calculates the yearly sales amount for a specific year.
     */
    public double getYearlySales(int year) {
        loadSalesData();
        return salesList.stream()
                .filter(s -> {
                    try {
                        LocalDate saleDate = LocalDate.parse(s.getDate());
                        return saleDate.getYear() == year;
                    } catch (Exception e) {
                        return false; // Ignore if date parsing fails
                    }
                })
                .mapToDouble(Sales::getTotalPrice)
                .sum();
    }
}
