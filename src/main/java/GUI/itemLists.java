package GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.border.EmptyBorder;

import GUI.Component.Dialog.ItemDialogs;
import Management.Controller.ItemController;
import Management.Model.Item;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.util.UUID;
import java.awt.image.BufferedImage;
import Management.Controller.CategoryController;
import Management.Model.Category;
import Management.Model.LoggedInUser;
import Management.Model.User;

import java.util.stream.Collectors; // Import for stream operations
import javax.swing.event.DocumentEvent; // Import for DocumentEvent
import javax.swing.event.DocumentListener; // Import for DocumentListener


public class itemLists extends JPanel {
    private static final Color CARD_BACKGROUND = new Color(5, 25, 55);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Dimension CARD_SIZE = new Dimension(240, 220);
    private static final Dimension IMAGE_SIZE = new Dimension(180, 100);
    private static final int GRID_COLUMNS = 2;
    private static final int GRID_GAP = 20;
    private static final int BORDER_PADDING = 10;

    private JPanel itemPanel;
    private JScrollPane scrollPane;
    private ItemController itemController;
    private Item selectedItem = null;
    private JPanel selectedCard = null;
    private JButton editButtonTop;
    private JButton deleteButtonTop;
    private JTextField searchField; // New search field
    User currentUser = LoggedInUser.getCurrentUser();
    public itemLists() {
        this.itemController = new ItemController();
        initializeUI();
        loadItems(null); // Load all items initially
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING));
        setBackground(UIManager.getColor("Panel.background")); // Ensure background is set

        // Top panel with title and buttons
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(getBackground());

        JLabel titleLabel = new JLabel("Item List");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(getBackground());

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id2")) {
            JButton addButton = new JButton("Add Item");
            addButton.addActionListener(e -> showAddItemDialog());

            editButtonTop = new JButton("Edit Selected");
            editButtonTop.setEnabled(false);
            editButtonTop.addActionListener(e -> {
                if (selectedItem != null) {
                    showEditItemDialog(selectedItem);
                }
            });

            deleteButtonTop = new JButton("Delete Selected");
            deleteButtonTop.setEnabled(false);
            deleteButtonTop.addActionListener(e -> {
                if (selectedItem != null) {
                    deleteItem(selectedItem);
                }
            });
            buttonPanel.add(addButton);
            buttonPanel.add(editButtonTop);
            buttonPanel.add(deleteButtonTop);
            topPanel.add(buttonPanel, BorderLayout.EAST);
        }

        // New: Search bar panel
        JPanel searchBarPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        searchBarPanel.setBackground(getBackground());
        searchField = new JTextField(30); // Increased width for better visibility
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add DocumentListener for auto-search as user types
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                performSearch();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                // This method is for attribute changes, not text content changes in plain text fields.
                performSearch();
            }
        });

        searchBarPanel.add(new JLabel("Search:"));
        searchBarPanel.add(searchField);

        // Combine title/buttons and search bar into a main header panel
        JPanel headerContainer = new JPanel();
        headerContainer.setLayout(new BoxLayout(headerContainer, BoxLayout.Y_AXIS));
        headerContainer.setBackground(getBackground());
        headerContainer.add(topPanel);
        headerContainer.add(Box.createRigidArea(new Dimension(0, 10))); // Vertical space
        headerContainer.add(searchBarPanel);
        headerContainer.setBorder(new EmptyBorder(0, 0, 15, 0)); // Add bottom padding

        add(headerContainer, BorderLayout.NORTH); // Add the combined header to NORTH

        // Main items panel
        itemPanel = new JPanel();
        itemPanel.setLayout(new GridLayout(0, GRID_COLUMNS, GRID_GAP, GRID_GAP));
        itemPanel.setBackground(getBackground());

        scrollPane = new JScrollPane(itemPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);
    }

    /**
     * Triggers the search based on the text in the search field.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        loadItems(query); // Call loadItems with the search query
    }

    public void loadItems() {
        loadItems(null); // Overload for initial call without search query
    }

    public void loadItems(String searchQuery) { // Added searchQuery parameter
        SwingUtilities.invokeLater(() -> {
            itemPanel.removeAll();
            List<Item> items = itemController.getItemList();

            // Filter items if a search query is provided
            if (searchQuery != null && !searchQuery.isEmpty()) {
                String lowerCaseQuery = searchQuery.toLowerCase();
                items = items.stream()
                        .filter(item -> item.getItemID().toLowerCase().contains(lowerCaseQuery) ||
                                         item.getItemName().toLowerCase().contains(lowerCaseQuery) ||
                                         String.valueOf(item.getPrice()).toLowerCase().contains(lowerCaseQuery) ||
                                         String.valueOf(item.getQuantity()).toLowerCase().contains(lowerCaseQuery) ||
                                         item.getCategory().toLowerCase().contains(lowerCaseQuery))
                        .collect(Collectors.toList());
            }

            if (items.isEmpty()) {
                itemPanel.add(createEmptyStatePanel());
            } else {
                for (Item item : items) {
                    itemPanel.add(createItemCard(item));
                }
            }

            itemPanel.revalidate();
            itemPanel.repaint();
            clearSelection();
        });
    }

    private void showAddItemDialog() {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);
        ItemDialogs currentDialog = new ItemDialogs(parent, "Add New Item", null);
        if (currentDialog.showDialog()) {
            Item item = currentDialog.getItemData();
            System.out.println(item + ", asdada");
                if (itemController.saveItem(item)) {
                    loadItems(searchField.getText().trim()); // Refresh with current search query
                    JOptionPane.showMessageDialog(itemLists.this, "Item saved successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(itemLists.this, "Failed to save item", "Error", JOptionPane.ERROR_MESSAGE);
                }
        }
    }

    // Update the showEditItemDialog method
   private void showEditItemDialog(Item item) {
        JFrame parent = (JFrame) SwingUtilities.getWindowAncestor(this);

        ItemDialogs currentDialog = new ItemDialogs(parent, "Edit Item", item);
        if (currentDialog.showDialog()) {
            Item updatedItem = currentDialog.getItemData();
            if (updatedItem != null) {
                // If you need to convert to string and back for your controller
                String itemString = updatedItem.toUpdateItemString();
                Item itemToUpdate = Item.fromFileString(itemString);

                if (itemToUpdate != null && itemController.updateItem(itemToUpdate)) {
                    loadItems(searchField.getText().trim()); // Refresh with current search query
                    JOptionPane.showMessageDialog(itemLists.this,
                            "Item updated successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(itemLists.this,
                            "Failed to update item",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(itemLists.this,
                        "Invalid item data",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private JPanel createEmptyStatePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel("No items available", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(label, BorderLayout.CENTER);
        return panel;
    }

    private JPanel createItemCard(Item item) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY),
                new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING)
        ));
        card.setBackground(CARD_BACKGROUND);
        card.setMaximumSize(CARD_SIZE);
        card.setPreferredSize(CARD_SIZE);

        JLabel imageLabel = createImageLabel(item);
        card.add(imageLabel);
        card.add(Box.createVerticalStrut(BORDER_PADDING));

        card.add(createLabel(item.getItemName(), Font.BOLD));
        card.add(createLabel(String.format("RM %.2f", item.getPrice()), Font.PLAIN));
        card.add(createLabel("In Stock: " + item.getQuantity(), Font.PLAIN));

        if (currentUser.getUserRoleId().equals("ur_id1") || currentUser.getUserRoleId().equals("ur_id5")) {
            card.add(createLabel("In Storage Stock: " + item.getStock(), Font.PLAIN));
        }

        card.add(Box.createVerticalGlue());
        card.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectCard(card, item);
            }
        });
        return card;
    }

    private JLabel createImageLabel(Item item) {
        JLabel label = new JLabel();
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setPreferredSize(IMAGE_SIZE);

        ImageIcon icon = loadImage(item.getImagePath());
        if (icon != null) {
            Image scaledImage = icon.getImage().getScaledInstance(
                    IMAGE_SIZE.width, IMAGE_SIZE.height, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaledImage));
        } else {
            label.setText("No Image");
            label.setHorizontalAlignment(SwingConstants.CENTER);
        }
        return label;
    }

    private ImageIcon loadImage(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return null;
        }

        try {
            File imageFile = new File(imagePath);
            if (!imageFile.exists()) {
                // Try resources directory if not found
                String resourcePath = "src/main/resources/" + imagePath;
                imageFile = new File(resourcePath);
                if (!imageFile.exists()) {
                    return null;
                }
            }
            BufferedImage bufferedImage = ImageIO.read(imageFile);
            if (bufferedImage != null) {
                return new ImageIcon(bufferedImage);
            } else {
                return null;
            }
        } catch (IOException e) {
            System.err.println("Error loading image: " + e.getMessage());
            return null;
        } catch (Exception e) {
            System.err.println("Error loading image: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private JLabel createLabel(String text, int fontStyle) {
        JLabel label = new JLabel(text);
        label.setForeground(TEXT_COLOR);
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        label.setFont(label.getFont().deriveFont(fontStyle));
        return label;
    }

    private void deleteItem(Item item) {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete \"" + item.getItemName() + "\"?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            if (itemController.deleteItem(item.getItemID())) {
                JOptionPane.showMessageDialog(this, "Item deleted successfully");
                loadItems(searchField.getText().trim()); // Refresh with current search query
            } else {
                JOptionPane.showMessageDialog(this, "Failed to delete item", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void selectCard(JPanel card, Item item) {
        if (selectedCard != null) {
            selectedCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING)
            ));
        }

        selectedCard = card;
        selectedItem = item;

        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.YELLOW, 2),
                new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING)
        ));

        if (editButtonTop != null) {
            editButtonTop.setEnabled(true);
        }
        if (deleteButtonTop != null) {
            deleteButtonTop.setEnabled(true);
        }
    }

    private void clearSelection() {
        if (selectedCard != null) {
            selectedCard.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.DARK_GRAY),
                    new EmptyBorder(BORDER_PADDING, BORDER_PADDING, BORDER_PADDING, BORDER_PADDING)
            ));
            selectedCard = null;
        }

        selectedItem = null;

        if (editButtonTop != null) {
            editButtonTop.setEnabled(false);
        }

        if (deleteButtonTop != null) {
            deleteButtonTop.setEnabled(false);
        }
    }

}
