package Management.Controller;

import Management.Model.Category;
import Management.FileIO.FileIO;
import java.util.ArrayList;
import java.util.List;

public class CategoryController {
    private final String rootFilePath = "src/main/resources/";
    private final String categoryFilePath = "category.txt";
    private List<Category> categoryList = new ArrayList<>();

    // Load categories from file
    public void loadCategories() {
        categoryList.clear();
        List<String> lines = FileIO.readFromFile(rootFilePath + categoryFilePath, Integer.MAX_VALUE);

        // Skip header
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            String[] parts = line.split(","); // Split by comma
            if (parts.length == 2) {
                String categoryID = parts[0];
                String categoryName = parts[1];
                Category category = new Category(categoryID, categoryName);
                categoryList.add(category);
            } else {
                System.err.println("Skipping invalid line: " + line); //error message
            }
        }
    }

    // Return list of categories
    public List<Category> getCategoryList() {
        return categoryList;
    }

    // Get a category by ID
    public Category getCategoryByID(String categoryID) {
        for (Category c : categoryList) {
            if (c.CategoryID().equalsIgnoreCase(categoryID)) {
                return c;
            }
        }
        return null;
    }

    // Add a new category (and append to file)
    public void addCategory(Category category) {
        categoryList.add(category);
        FileIO.writeToFile(rootFilePath + categoryFilePath, List.of(category.toFileString()), true);
    }
}

