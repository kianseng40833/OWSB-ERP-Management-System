package GUI.Component.Dialog;

import Management.Controller.ItemController;
import Management.Model.Item;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.UUID; // You can remove this import if UUID is no longer used
import javax.imageio.ImageIO;
import Management.Controller.CategoryController;
import Management.Model.Category;
import java.util.List;

public class ItemDialogs extends JDialog {
    private JTextField idField;
    private JTextField nameField;
    private JComboBox<Category> categoryComboBox;
    private JTextField brandField;
    private JTextField stockField;
    private JTextField storageStockField;
    private JTextField priceField; // Selling price
    private JLabel imagePathLabel;
    private String imagePath;
    private String originalImagePath;
    private boolean confirmed = false;
    private final ItemController itemController;
    private final CategoryController categoryController;

    public ItemDialogs(JFrame parent, String title, Item item) {
        super(parent, title, true);
        this.itemController = new ItemController();
        this.itemController.loadItems(); // Ensure items are loaded to determine the next ID
        this.categoryController = new CategoryController();
        this.categoryController.loadCategories();
        if (item != null) {
            this.originalImagePath = item.getImagePath(); // Store original path
        }
        setSize(500, 500); // Increased height to accommodate new field
        setLayout(new BorderLayout(10, 10));
        setLocationRelativeTo(parent);

        JPanel formPanel = createFormPanel(item);
        add(formPanel, BorderLayout.CENTER);
        add(createButtonPanel(), BorderLayout.SOUTH);
    }

    private JPanel createFormPanel(Item item) {
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        initializeFields(item);
        addFormFields(formPanel);
        return formPanel;
    }

    private void initializeFields(Item item) {
        idField = new JTextField(20);
        nameField = new JTextField(20);
        categoryComboBox = new JComboBox<>();
        brandField = new JTextField(20);
        stockField = new JTextField(20);
        priceField = new JTextField(20);
        imagePathLabel = new JLabel("No file selected");
        storageStockField = new JTextField(20);
        storageStockField.setEditable(false);

        imagePath = "";
        List<Category> categories = categoryController.getCategoryList();
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }

        if (item != null) {
            populateFieldsForExistingItem(item);
        } else {
            setupFieldsForNewItem();
        }
    }

    private void populateFieldsForExistingItem(Item item) {
        idField.setText(item.getItemID());
        nameField.setText(item.getItemName());
        brandField.setText(item.getBrand());
        stockField.setText(String.valueOf(item.getQuantity()));
        stockField.setEditable(false);
        storageStockField.setText(item.getStock());
        priceField.setText(String.valueOf(item.getPrice()));
        String itemImagePath = item.getImagePath();
        this.originalImagePath = itemImagePath;
        this.imagePath = itemImagePath;

        if (itemImagePath != null && !itemImagePath.isEmpty()) {
            if (!itemImagePath.startsWith("Itemimage/") && !new File(itemImagePath).isAbsolute()) {
                itemImagePath = "src/main/resources/Itemimage/" + itemImagePath;
            }
            imagePathLabel.setText(itemImagePath);
        } else {
            imagePathLabel.setText("No image selected");
            this.imagePath = "";
        }
        idField.setEditable(false);

        String itemCategory = item.getCategory();
        if (itemCategory != null) {
            for (int i = 0; i < categoryComboBox.getItemCount(); i++) {
                Category comboBoxCategory = categoryComboBox.getItemAt(i);
                if (comboBoxCategory != null && itemCategory.equals(comboBoxCategory.CategoryName())) {
                    categoryComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void setupFieldsForNewItem() {
        idField.setText(itemController.generateNextItemId());
        idField.setEditable(false);
    }

    private void addFormFields(JPanel formPanel) {
        formPanel.add(createFieldPanel("Item ID:", idField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Item Name:", nameField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Category:", categoryComboBox));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Brand:", brandField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Stock:", stockField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createFieldPanel("Storage Stock:", storageStockField));

        formPanel.add(createFieldPanel("Price (RM):", priceField));
        formPanel.add(Box.createVerticalStrut(10));
        formPanel.add(createImageFieldPanel());
    }

    private JPanel createFieldPanel(String label, JComponent field) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JLabel jLabel = new JLabel(label);
        jLabel.setPreferredSize(new Dimension(100, jLabel.getPreferredSize().height));
        panel.add(jLabel, BorderLayout.WEST);
        panel.add(field, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createImageFieldPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        JPanel imagePanel = new JPanel(new BorderLayout());
        JButton uploadBtn = new JButton("Choose File");
        uploadBtn.addActionListener(this::chooseImage);

        imagePanel.add(uploadBtn, BorderLayout.WEST);
        imagePanel.add(imagePathLabel, BorderLayout.CENTER);

        panel.add(imagePanel, BorderLayout.CENTER);
        return panel;
    }

    private void chooseImage(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Image files", ImageIO.getReaderFileSuffixes()));

        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selected = chooser.getSelectedFile();
            if (selected.exists()) {
                imagePath = selected.getAbsolutePath();
                imagePathLabel.setText(selected.getName());
                imagePathLabel.setToolTipText(imagePath);
            }
        }
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

        JButton okButton = new JButton("OK");
        okButton.addActionListener(e -> handleOkAction());

        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        return buttonPanel;
    }

    private void handleOkAction() {
        if (validateAndSave()) {
            confirmed = true;
            dispose();
        }
    }

    private boolean validateAndSave() {
        try {
            String name = nameField.getText().trim();
            Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
            if (selectedCategory == null) {
                showValidationError("Please select a category.");
                return false;
            }
            String brand = brandField.getText().trim();
            int stock = Integer.parseInt(stockField.getText().trim());
            double price = Double.parseDouble(priceField.getText().trim());

            if (name.isEmpty() || brand.isEmpty()) {
                showValidationError("Please fill in all required fields.");
                return false;
            }

            if (stock < 0 || price < 0) { // Validate costPrice
                showValidationError("Stock, price, and cost price must be non-negative.");
                return false;
            }

            return true; // Validation passed
        } catch (NumberFormatException ex) {
            showValidationError("Please enter valid numbers for stock, price, and cost price.");
            return false;
        } catch (Exception ex) {
            showError("An unexpected error occurred: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Input Error", JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message,
                "Error", JOptionPane.ERROR_MESSAGE);
    }

    public Item getItemData() {
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();

        try {
            String finalImagePath = (imagePath != null && !imagePath.isEmpty()) ?
                    imagePath : originalImagePath;

            // Use the new 9-argument Item constructor
            return new Item(
                    idField.getText(),
                    nameField.getText(),
                    Double.parseDouble(priceField.getText()),
                    Integer.parseInt(stockField.getText()),
                    storageStockField.getText(),
                    selectedCategory != null ? selectedCategory.CategoryName() : "",
                    brandField.getText(),
                    finalImagePath != null ? finalImagePath : ""
            );
        } catch (NumberFormatException e) {
            System.err.println("Error parsing number values: " + e.getMessage());
            return null;
        }
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
}
