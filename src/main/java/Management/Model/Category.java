package Management.Model;

public class Category {
    private String category_ID;
    private String category_name;

    public Category(String category_ID, String category_name) {
        this.category_ID = category_ID;
        this.category_name = category_name;
    }

    public String CategoryID() {
        return category_ID;
    }

    public String CategoryName() {
        return category_name;
    }

    @Override
    public String toString() { return category_name; }

    public String toFileString() {
        return String.join(",", category_ID, category_name);
    }

    public static Category fromFileString(String line) {
        String[] parts = line.split(",");
        if (parts.length != 2) {
            System.err.println("Invalid data format: " + line);
            return null;  // Return null if the data is invalid
        }
        return new Category(
                parts[0].trim(),
                parts[1].trim()
        );
    }
}
