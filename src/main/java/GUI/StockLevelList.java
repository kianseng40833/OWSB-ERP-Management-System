package GUI;

import Management.Controller.ItemController;
import Management.Model.Item;
import GUI.Component.Dialog.StockAdjustmentDialog; // Import the new dialog

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.util.List;

// Imports for printing functionality
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.Paper;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;


public class StockLevelList extends JPanel {
    private final ItemController itemController;
    private DefaultTableModel tableModel;
    private JTable table;
    private static final int LOW_STOCK_THRESHOLD = 300; // Define the low stock threshold
    private JButton printStockReportButton;
    private JButton adjustStockButton; // New button for adjusting stock

    public StockLevelList() {
        this.itemController = new ItemController();
        initializeUI();
        refreshTable();
        updateButtonStates(); // Set initial button states
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setBackground(UIManager.getColor("Panel.background"));

        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BorderLayout());
        topPanel.setBackground(UIManager.getColor("Panel.background"));

        JLabel title = new JLabel("Stock Level Report");
        title.setFont(UIManager.getFont("Label.font").deriveFont(20f).deriveFont(Font.BOLD));
        topPanel.add(title, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(UIManager.getColor("Panel.background"));

        printStockReportButton = new JButton("Print Stock Report");
        printStockReportButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
        printStockReportButton.addActionListener(e -> showStockLevelPrintPreview());
        buttonPanel.add(printStockReportButton);

        // New Adjust Stock Button
        adjustStockButton = new JButton("Adjust Stock");
        adjustStockButton.setFont(UIManager.getFont("Button.font").deriveFont(14f));
        adjustStockButton.addActionListener(e -> adjustStock());
        adjustStockButton.setEnabled(false); // Initially disabled
        buttonPanel.add(adjustStockButton);

        topPanel.add(buttonPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BorderLayout());
        centerPanel.setBackground(UIManager.getColor("Panel.background"));

        String[] columnNames = {"No.", "Item ID", "Item Name", "Current Stock", "Storage Stock", "Current Stock Status"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        // Add ListSelectionListener to enable/disable buttons
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) { // Ensure event is not a partial adjustment
                    updateButtonStates();
                }
            }
        });

        table.getColumnModel().getColumn(0).setPreferredWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(150);
        table.getColumnModel().getColumn(3).setPreferredWidth(120);
        table.getColumnModel().getColumn(4).setPreferredWidth(150);
        table.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(table);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);
    }

    /**
     * Updates the enabled state of action buttons based on table row selection.
     */
    private void updateButtonStates() {
        boolean rowSelected = table.getSelectedRow() != -1;
        adjustStockButton.setEnabled(rowSelected);
        // If you had other buttons like "Edit" for items, you'd enable/disable them here too.
    }

    /**
     * Handles the logic for adjusting stock of a selected item.
     */
    private void adjustStock() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to adjust stock.", "No Item Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String itemID = (String) tableModel.getValueAt(selectedRow, 1); // Item ID is at column 1
        Item itemToAdjust = itemController.getItemById(itemID); // Assuming getItemById exists in ItemController

        if (itemToAdjust != null) {
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            StockAdjustmentDialog dialog = new StockAdjustmentDialog(parentFrame, itemToAdjust);

            if (dialog.showDialog()) { // If user clicked OK in the dialog
                // 🔁 Update item with new stock values from dialog
                itemToAdjust.setQuantity(dialog.getUpdatedStock());
                itemToAdjust.setStock(String.valueOf(dialog.getUpdatedStorageStock()));

                if (itemController.updateItem(itemToAdjust)) {
                    JOptionPane.showMessageDialog(this,
                            "Stock for " + itemToAdjust.getItemName() + " updated successfully!",
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    refreshTable(); // Refresh table to show updated stock
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Failed to update stock for " + itemToAdjust.getItemName(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }

        } else {
            JOptionPane.showMessageDialog(this, "Selected item not found in database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refreshes the table with the latest item stock data and calculates their status.
     */
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Item> items = itemController.getItemList();

        if (items != null) {
            int rowNum = 1;
            for (Item item : items) {
                String itemID = item.getItemID();
                String itemName = item.getItemName();
                int currentStock = item.getQuantity();
                String storageStock = item.getStock(); // Get storage stock from Item model
                String stockStatus;

                // Calculate status based on current stock (or combine both if needed)
                if (currentStock < LOW_STOCK_THRESHOLD) {
                    stockStatus = "Low Stock";
                } else {
                    stockStatus = "Normal";
                }

                tableModel.addRow(new Object[]{
                        rowNum++,
                        itemID,
                        itemName,
                        currentStock,
                        storageStock, // Add storage stock to the table
                        stockStatus
                });
            }
        }
    }

    /**
     * Generates a JTextArea containing details of all items for the stock level report.
     * @return A JTextArea with formatted stock level details.
     */
    private JTextArea generateStockLevelReportTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setEditable(false);
        // Use a smaller font for the report text area to better fit print preview
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 10)); // Adjusted font size

        StringBuilder reportDetails = new StringBuilder();
        reportDetails.append("--- Stock Level Report ---\n\n");
        // Adjusted header for printing to include Storage Stock and better alignment for A4
        reportDetails.append(String.format("%-5s %-12s %-35s %-15s %-15s %-15s\n", "No.", "Item ID", "Item Name", "Current Stock", "Storage Stock", "Status"));
        reportDetails.append("----------------------------------------------------------------------------------------------------\n"); // 100 characters

        List<Item> items = itemController.getItemList();
        if (items.isEmpty()) {
            reportDetails.append("No items to display.\n");
        } else {
            int reportRowNum = 1;
            for (Item item : items) {
                String itemID = item.getItemID();
                String itemName = item.getItemName();
                int stockValue = item.getQuantity();
                String storageStock =  item.getStock();
                String stockStatus;

                if (stockValue < LOW_STOCK_THRESHOLD) {
                    stockStatus = "Low Stock";
                } else {
                    stockStatus = "Normal";
                }
                // Adjusted format for printing to include Storage Stock and better alignment for A4
                reportDetails.append(String.format("%-5d %-12s %-35s %-15d %-15s %-15s\n", reportRowNum++, itemID, itemName, stockValue, storageStock, stockStatus));
            }
        }
        textArea.setText(reportDetails.toString());
        textArea.setCaretPosition(0);
        return textArea;
    }

    /**
     * Initiates the printing process for the given JTextArea content.
     * This method is now called from within the PrintPreviewDialog.
     * @param textArea The JTextArea containing the report to be printed.
     * @param jobName The name to be assigned to the print job.
     */
    private void executePrintJob(JTextArea textArea, String jobName) {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName(jobName.replace("_", " "));

            Printable printable = new StockLevelPrintable(textArea);

            PageFormat pageFormat = printerJob.defaultPage();
            // --- Explicitly set A4 paper size and margins ---
            Paper paper = new Paper();
            // A4 dimensions in points (approx 8.27 x 11.69 inches)
            paper.setSize(595.27, 841.89);
            double margin = 36; // 0.5 inch margin in points
            paper.setImageableArea(margin, margin, paper.getWidth() - 2 * margin, paper.getHeight() - 2 * margin);
            pageFormat.setPaper(paper);
            // --- End A4 setup ---

            printerJob.setPrintable(printable, pageFormat);

            if (printerJob.printDialog()) {
                printerJob.print();
                JOptionPane.showMessageDialog(this, "Printing Complete.", "Print Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Printing Cancelled.", "Print Result", JOptionPane.WARNING_MESSAGE);
            }
        } catch (PrinterException pe) {
            JOptionPane.showMessageDialog(this, "Error during printing: " + pe.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            pe.printStackTrace();
        }
    }

    /**
     * Saves the content of the given JTextArea to a text file.
     * @param textArea The JTextArea containing the report to be saved.
     * @param defaultFileName The default file name to suggest for saving.
     */
    private void saveStockReportToFile(JTextArea textArea, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Stock Level Report As");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".txt");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                textArea.write(writer);
                JOptionPane.showMessageDialog(this, "Stock Level Report saved successfully to:\n" + fileToSave.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving report: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    /**
     * Displays a print preview dialog for the generated stock level report.
     * This is the entry point for the "Print Stock Report" button.
     */
    private void showStockLevelPrintPreview() {
        JTextArea stockReportTextArea = generateStockLevelReportTextArea();
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName("Stock Level Report");

            Printable printable = new StockLevelPrintable(stockReportTextArea);

            PageFormat pageFormat = printerJob.defaultPage();
            // --- Explicitly set A4 paper size and margins for preview ---
            Paper paper = new Paper();
            paper.setSize(595.27, 841.89); // A4 dimensions
            double margin = 36; // 0.5 inch margin in points
            paper.setImageableArea(margin, margin, paper.getWidth() - 2 * margin, paper.getHeight() - 2 * margin);
            pageFormat.setPaper(paper);
            // --- End A4 setup for preview ---

            StockLevelPrintPreviewDialog previewDialog = new StockLevelPrintPreviewDialog(
                    (JFrame) SwingUtilities.getWindowAncestor(this),
                    stockReportTextArea, // Pass the JTextArea to the dialog
                    printable,
                    pageFormat,
                    "Stock Level Report"
            );
            previewDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error showing print preview: " + ex.getMessage(), "Preview Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * A custom Printable implementation to render JTextArea content for printing.
     * Adapted for Stock Level Report.
     */
    private class StockLevelPrintable implements Printable {
        private JTextArea textArea;
        private int linesPerPage;
        private Font fontForPrinting; // Declare a dedicated font for printing
        private FontMetrics fontMetrics;
        private int lineHeight;

        public StockLevelPrintable(JTextArea textArea) {
            this.textArea = textArea;
            // Use a smaller font for printing to ensure it fits A4 width
            // 9pt is a common readable size that often fits more content
            fontForPrinting = new Font("Monospaced", Font.PLAIN, 9); // Adjusted font size for printing
            fontMetrics = textArea.getFontMetrics(fontForPrinting); // Get metrics for the print font
            lineHeight = fontMetrics.getHeight();
        }

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
            Graphics2D g2d = (Graphics2D) graphics;
            g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int printableHeight = (int) pageFormat.getImageableHeight();
            linesPerPage = printableHeight / lineHeight;

            String[] lines = textArea.getText().split("\n");
            int totalLines = lines.length;
            int totalPages = (int) Math.ceil((double) totalLines / linesPerPage);

            if (pageIndex >= totalPages) {
                return NO_SUCH_PAGE;
            }

            int startLine = pageIndex * linesPerPage;
            int endLine = Math.min(startLine + linesPerPage, totalLines);

            g2d.setFont(fontForPrinting); // Set the dedicated print font
            g2d.setColor(Color.BLACK);

            int y = 0;

            for (int i = startLine; i < endLine; i++) {
                String line = lines[i];
                g2d.drawString(line, 0, y + fontMetrics.getAscent());
                y += lineHeight;
            }

            return PAGE_EXISTS;
        }
    }

    /**
     * A simple dialog to display a print preview of a Printable object.
     * Adapted for Stock Level Report.
     */
    private class StockLevelPrintPreviewDialog extends JDialog {
        private Printable printable;
        private PageFormat pageFormat;
        private JTextArea sourceTextArea; // Store the source JTextArea
        private String printJobName;

        public StockLevelPrintPreviewDialog(JFrame owner, JTextArea sourceTextArea, Printable printable, PageFormat pageFormat, String printJobName) {
            super(owner, "Print Preview - " + printJobName.replace("_", " "), true);
            this.sourceTextArea = sourceTextArea;
            this.printable = printable;
            this.pageFormat = pageFormat;
            this.printJobName = printJobName;

            initComponents();
            setSize(1000, 800); // Keep the large size
            setLocationRelativeTo(owner);
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel previewPanel = new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    // Set the preferred size of the panel to the actual page dimensions
                    return new Dimension((int) pageFormat.getWidth(), (int) pageFormat.getHeight());
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;

                    try {
                        printable.print(g2d, pageFormat, 0); // Always print page 0 for simplified preview
                    } catch (PrinterException e) {
                        e.printStackTrace();
                        g2d.setColor(Color.RED);
                        g2d.drawString("Error rendering page: " + e.getMessage(), 10, 20);
                    }
                }
            };
            previewPanel.setBackground(Color.LIGHT_GRAY);

            JScrollPane scrollPane = new JScrollPane(previewPanel);
            add(scrollPane, BorderLayout.CENTER);

            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            JButton printButton = new JButton("Print");
            printButton.addActionListener(e -> {
                dispose(); // Close preview dialog
                executePrintJob(sourceTextArea, printJobName); // Call executePrintJob
            });
            JButton saveButton = new JButton("Save as Text File"); // Added Save button
            saveButton.addActionListener(e -> saveStockReportToFile(sourceTextArea, printJobName));
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());

            controlPanel.add(printButton);
            controlPanel.add(saveButton);
            controlPanel.add(closeButton);
            add(controlPanel, BorderLayout.SOUTH);
        }
    }
}
