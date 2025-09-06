package Management.Controller;

import Management.Model.Item;
import Management.Model.Supplierlist;
import Management.FileIO.FileIO;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SupplierController {
    private final String rootFilePath = "src/main/resources/";
    private final String supplierFilePath = "supplier.txt";
    private List<Supplierlist> supplierList = new ArrayList<>();

    public SupplierController() {
        loadSuppliers();
    }

    // Load all suppliers from file
    public void loadSuppliers() {
        supplierList.clear();
        List<String> lines = FileIO.readFromFile(rootFilePath + supplierFilePath, Integer.MAX_VALUE);
        if (lines.isEmpty()) return;

        for (int i = 1; i < lines.size(); i++) {
            Supplierlist supplier = Supplierlist.fromFileString(lines.get(i));
            if (supplier != null) {
                supplierList.add(supplier);
            }
        }
    }

    public List<Supplierlist> getSupplierList() {
        return new ArrayList<>(supplierList);
    }

    public Supplierlist getSupplier(String supplierID) {
        return supplierList.stream()
                .filter(s -> s.getSupplierID().equalsIgnoreCase(supplierID))
                .findFirst()
                .orElse(null);
    }

    public Supplierlist getSupplierByItemId(String itemId) {
        return supplierList.stream()
                .filter(supplier -> itemId.equalsIgnoreCase(supplier.getItemID()))
                .findFirst()
                .orElse(null);
    }

    public boolean addSupplier(Supplierlist supplier) {
        if (supplier == null || isSupplierIdExists(supplier.getSupplierID())) {
            return false;
        }
        supplierList.add(supplier);
        return saveSuppliersToFile();
    }

    public boolean updateSupplier(Supplierlist updatedSupplier) {
        if (updatedSupplier == null) return false;

        for (int i = 0; i < supplierList.size(); i++) {
            if (supplierList.get(i).getSupplierID().equalsIgnoreCase(updatedSupplier.getSupplierID())) {
                supplierList.set(i, updatedSupplier);
                return saveSuppliersToFile();
            }
        }
        return false;
    }

    public boolean deleteSupplier(String supplierID) {
        boolean removed = supplierList.removeIf(s -> s.getSupplierID().equalsIgnoreCase(supplierID));
        if (removed) {
            return saveSuppliersToFile();
        }
        return false;
    }

    public String generateSupplierId() {
        int maxId = 0;
        Pattern pattern = Pattern.compile("^S(\\d{3})$");

        for (Supplierlist supplier : supplierList) {
            Matcher matcher = pattern.matcher(supplier.getSupplierID());
            if (matcher.matches()) {
                try {
                    int idNum = Integer.parseInt(matcher.group(1));
                    maxId = Math.max(maxId, idNum);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid supplier ID format: " + supplier.getSupplierID());
                }
            }
        }
        return String.format("S%03d", maxId + 1);
    }

    private boolean isSupplierIdExists(String id) {
        return supplierList.stream()
                .anyMatch(s -> s.getSupplierID().equalsIgnoreCase(id));
    }

    // This is the method that ItemController calls to update suppliers when an item changes
    public boolean updateSuppliersForItem(String itemId, String newItemName) {
        boolean updated = false;
        for (Supplierlist supplier : supplierList) {
            if (itemId.equalsIgnoreCase(supplier.getItemID())) {
                supplier.updateItemInfo(newItemName);
                updated = true;
            }
        }
        if (updated) {
            return saveSuppliersToFile();
        }
        return false;
    }

    private boolean saveSuppliersToFile() {
        List<String> lines = new ArrayList<>();
        lines.add("SupplierID,SupplierCompanyName,Contact,Email,PIC,ItemID,ItemSupplied,Cost,Stock,Status");

        supplierList.forEach(supplier ->
                lines.add(supplier.toFileString("SupplierID,SupplierCompanyName,Contact,Email,PIC,ItemID,ItemSupplied,Cost,Stock,Status"))
        );

        return FileIO.writeToFile(rootFilePath + supplierFilePath, lines, false);
    }

    public List<Supplierlist> getSuppliersForItem(Item item) {
        if (item == null) return new ArrayList<>();
        return supplierList.stream()
                .filter(s -> item.getItemID().equalsIgnoreCase(s.getItemID()))
                .toList();
    }
}