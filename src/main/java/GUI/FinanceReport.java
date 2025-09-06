package GUI;

import Management.Controller.FinancialReportController;
import Management.Model.FinancialReportData;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.Paper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import javax.swing.plaf.basic.BasicScrollBarUI;

public class FinanceReport extends JPanel {
    private final FinancialReportController reportController;
    private final JTabbedPane tabbedPane;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
    private final DateTimeFormatter yearFormatter = DateTimeFormatter.ofPattern("yyyy");

    // Date selection components
    private JSpinner dailyDateSpinner;
    private JSpinner weeklyStartDateSpinner;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;
    private JSpinner yearlyDateSpinner;

    public FinanceReport() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(15, 25, 40));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        reportController = new FinancialReportController();
        tabbedPane = new JTabbedPane();

        JPanel dailyReportPanel = createReportPanelWithControls("Daily");
        JPanel weeklyReportPanel = createReportPanelWithControls("Weekly");
        JPanel monthlyReportPanel = createReportPanelWithControls("Monthly");
        JPanel yearlyReportPanel = createReportPanelWithControls("Yearly");

        tabbedPane.addTab("Daily", dailyReportPanel);
        tabbedPane.addTab("Weekly", weeklyReportPanel);
        tabbedPane.addTab("Monthly", monthlyReportPanel);
        tabbedPane.addTab("Yearly", yearlyReportPanel);

        styleTabbedPane();
        add(tabbedPane, BorderLayout.CENTER);

        loadDailyReport();
        loadWeeklyReport();
        loadMonthlyReport();
        loadYearlyReport();
    }

    private JPanel createReportPanelWithControls(String reportType) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        controlPanel.setOpaque(false);

        JTextArea reportTextArea = new JTextArea();
        reportTextArea.setEditable(false);
        reportTextArea.setBackground(new Color(30, 40, 60));
        reportTextArea.setForeground(Color.WHITE);
        reportTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        styleScrollPane(scrollPane);

        JButton refreshButton = new JButton("Refresh");
        JButton printToPrinterButton = new JButton("Print Report");
        JButton saveToFileButton = new JButton("Save as Text File");

        switch (reportType) {
            case "Daily":
                dailyDateSpinner = new JSpinner(new SpinnerDateModel());
                JSpinner.DateEditor dailyEditor = new JSpinner.DateEditor(dailyDateSpinner, "yyyy-MM-dd");
                dailyDateSpinner.setEditor(dailyEditor);
                dailyDateSpinner.setValue(java.util.Calendar.getInstance().getTime());

                refreshButton.addActionListener(e -> loadDailyReport());
                printToPrinterButton.addActionListener(e -> showPrintPreview(reportTextArea, "Daily_Financial_Report"));
                saveToFileButton.addActionListener(e -> saveReportToFile(reportTextArea, "Daily_Financial_Report"));

                controlPanel.add(new JLabel("Select Date:"));
                controlPanel.add(dailyDateSpinner);
                controlPanel.add(refreshButton);
                controlPanel.add(printToPrinterButton);
                controlPanel.add(saveToFileButton);
                break;

            case "Weekly":
                weeklyStartDateSpinner = new JSpinner(new SpinnerDateModel());
                JSpinner.DateEditor weeklyEditor = new JSpinner.DateEditor(weeklyStartDateSpinner, "yyyy-MM-dd");
                weeklyStartDateSpinner.setEditor(weeklyEditor);
                weeklyStartDateSpinner.setValue(java.util.Calendar.getInstance().getTime());

                refreshButton.addActionListener(e -> loadWeeklyReport());
                printToPrinterButton.addActionListener(e -> showPrintPreview(reportTextArea, "Weekly_Financial_Report"));
                saveToFileButton.addActionListener(e -> saveReportToFile(reportTextArea, "Weekly_Financial_Report"));

                controlPanel.add(new JLabel("Select Week Starting:"));
                controlPanel.add(weeklyStartDateSpinner);
                controlPanel.add(refreshButton);
                controlPanel.add(printToPrinterButton);
                controlPanel.add(saveToFileButton);
                break;

            case "Monthly":
                monthComboBox = new JComboBox<>(new String[]{"01", "02", "03", "04", "05", "06",
                        "07", "08", "09", "10", "11", "12"});
                monthComboBox.setSelectedIndex(LocalDate.now().getMonthValue() - 1);

                yearComboBox = new JComboBox<>();
                int currentYear = LocalDate.now().getYear();
                for (int year = currentYear - 5; year <= currentYear + 5; year++) {
                    yearComboBox.addItem(year);
                }
                yearComboBox.setSelectedItem(currentYear);

                refreshButton.addActionListener(e -> loadMonthlyReport());
                printToPrinterButton.addActionListener(e -> showPrintPreview(reportTextArea, "Monthly_Financial_Report"));
                saveToFileButton.addActionListener(e -> saveReportToFile(reportTextArea, "Monthly_Financial_Report"));

                controlPanel.add(new JLabel("Select Month:"));
                controlPanel.add(monthComboBox);
                controlPanel.add(new JLabel("Year:"));
                controlPanel.add(yearComboBox);
                controlPanel.add(refreshButton);
                controlPanel.add(printToPrinterButton);
                controlPanel.add(saveToFileButton);
                break;

            case "Yearly":
                yearlyDateSpinner = new JSpinner(new SpinnerDateModel(
                        java.util.Calendar.getInstance().getTime(),
                        null, null, java.util.Calendar.YEAR
                ));
                JSpinner.DateEditor yearlyEditor = new JSpinner.DateEditor(yearlyDateSpinner, "yyyy");
                yearlyDateSpinner.setEditor(yearlyEditor);
                yearlyDateSpinner.setValue(java.util.Calendar.getInstance().getTime());

                refreshButton.addActionListener(e -> loadYearlyReport());
                printToPrinterButton.addActionListener(e -> showPrintPreview(reportTextArea, "Yearly_Financial_Report"));
                saveToFileButton.addActionListener(e -> saveReportToFile(reportTextArea, "Yearly_Financial_Report"));

                controlPanel.add(new JLabel("Select Year:"));
                controlPanel.add(yearlyDateSpinner);
                controlPanel.add(refreshButton);
                controlPanel.add(printToPrinterButton);
                controlPanel.add(saveToFileButton);
                break;
        }

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadDailyReport() {
        Date selectedDate = (Date) dailyDateSpinner.getValue();
        LocalDate reportDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        List<FinancialReportData> reports = reportController.getDailyReport(reportDate);
        displayReport(reports, 0, "Daily Report - " + reportDate.format(dateFormatter));
    }

    private void loadWeeklyReport() {
        Date selectedDate = (Date) weeklyStartDateSpinner.getValue();
        LocalDate startDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = startDate.plusDays(6);
        List<FinancialReportData> reports = reportController.getWeeklyReport(startDate);
        displayReport(reports, 1, "Weekly Report - " + startDate.format(dateFormatter) + " to " +
                endDate.format(dateFormatter));
    }

    private void loadMonthlyReport() {
        int selectedMonth = monthComboBox.getSelectedIndex() + 1;
        int selectedYear = (Integer) yearComboBox.getSelectedItem();
        LocalDate startDate = LocalDate.of(selectedYear, selectedMonth, 1);
        List<FinancialReportData> reports = reportController.getMonthlyReport(startDate);
        displayReport(reports, 2, "Monthly Report - " + startDate.format(monthFormatter));
    }

    private void loadYearlyReport() {
        Date selectedDate = (Date) yearlyDateSpinner.getValue();
        LocalDate localDate = selectedDate.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        int selectedYear = localDate.getYear();
        List<FinancialReportData> reports = reportController.getYearlyReport(selectedYear);
        displayReport(reports, 3, "Yearly Report - " + selectedYear);
    }

    /**
     * Displays the financial report data in the specified JTextArea within a tab.
     * This method is redesigned to present sales and PO/PR data in separate, summarized sections.
     * @param reports The list of FinancialReportData to display.
     * @param tabIndex The index of the tab where the report should be displayed.
     * @param title The title for the report.
     */
    private void displayReport(List<FinancialReportData> reports, int tabIndex, String title) {
        JPanel panel = (JPanel) tabbedPane.getComponentAt(tabIndex);
        JScrollPane scrollPane = (JScrollPane) panel.getComponent(1);
        JTextArea textArea = (JTextArea) scrollPane.getViewport().getView();

        StringBuilder reportText = new StringBuilder();
        reportText.append(title).append("\n\n");

        if (reports.isEmpty()) {
            reportText.append("No data found for this period.\n");
            textArea.setText(reportText.toString());
            textArea.setCaretPosition(0);
            return;
        }

        double totalSalesRevenue = 0;
        double totalSalesCostOfGoodsSold = 0;
        double totalPOExpenses = 0;

        List<FinancialReportData> salesEntries = new ArrayList<>();
        List<FinancialReportData> poEntries = new ArrayList<>();

        for (FinancialReportData report : reports) {
            // Distinguish between sales and PO entries based on presence of receiptNo or poId
            if (report.getReceiptNo() != null && !report.getReceiptNo().isEmpty()) { // This is a Sales entry
                salesEntries.add(report);
                totalSalesRevenue += report.getRevenue();
                totalSalesCostOfGoodsSold += report.getCostOfGoodsSold();
            } else if (report.getPoId() != null && !report.getPoId().trim().isEmpty()) { // This is a PO entry
                poEntries.add(report);
                totalPOExpenses += report.getPurchaseCost();
            }
        }

        // --- SALES Section ---
        reportText.append("====================================================================================================\n"); // 100 characters
        reportText.append("SALES\n");
        reportText.append("====================================================================================================\n"); // 100 characters
        // Adjusted header for Sales: Date, Item (ID), QTY, Unit Price, Total
        reportText.append(String.format("%-12s %-45s %-8s %-12s %-12s\n", "Date", "Item (ID)", "QTY", "Unit Price", "Total"));
        reportText.append("----------------------------------------------------------------------------------------------------\n"); // 100 characters

        if (salesEntries.isEmpty()) {
            reportText.append("No sales data for this period.\n");
        } else {
            for (FinancialReportData sale : salesEntries) {
                // Adjusted format for Sales: Date, Item (ID), QTY, Unit Price, Total
                reportText.append(String.format("%-12s %-45s %-8d RM%9.2f RM%9.2f\n",
                        sale.getTransactionDate().format(dateFormatter),
                        sale.getItemName() + " (" + sale.getItemId() + ")",
                        sale.getQuantity(),
                        sale.getUnitPrice(),
                        sale.getRevenue()));
            }
        }
        reportText.append("\n");

        // --- SUMMARY (Sales) ---
        double totalProfitFromSales = totalSalesRevenue - totalSalesCostOfGoodsSold;
        reportText.append("SUMMARY:\n");
        reportText.append(String.format("Total Revenue: RM%.2f\n", totalSalesRevenue));
        reportText.append(String.format("Total Cost: RM%.2f\n", totalSalesCostOfGoodsSold));
        reportText.append(String.format("Total Profit: RM%.2f\n", totalProfitFromSales));
        reportText.append("\n");

        // --- STOCK ORDERING (PO) Section ---
        reportText.append("====================================================================================================\n"); // 100 characters
        reportText.append("STOCK ORDERING (PO)\n");
        reportText.append("====================================================================================================\n"); // 100 characters
        reportText.append(String.format("%-10s %-10s %-12s %-10s %-25s %-8s %-10s %-10s\n",
                "POID", "PRID", "PAYMENTID", "ITEMID", "ITEM NAME", "QTY", "UNITPRICE", "TOTAL PRICE"));
        reportText.append("----------------------------------------------------------------------------------------------------\n"); // 100 characters

        if (poEntries.isEmpty()) {
            reportText.append("No completed purchase orders for this period.\n");
        } else {
            for (FinancialReportData po : poEntries) {
                // Calculate UNITPRICE as TOTAL PRICE / QTY
                double calculatedUnitPrice = (po.getQuantity() > 0) ? po.getPurchaseCost() / po.getQuantity() : 0.0;
                reportText.append(String.format("%-10s %-10s %-12s %-10s %-25s %-8d RM%7.2f RM%7.2f\n",
                        po.getPoId(),
                        po.getPrId(),
                        po.getPaymentId(),
                        po.getItemId(),
                        po.getItemName(),
                        po.getQuantity(),
                        calculatedUnitPrice, // Use the calculated unit price
                        po.getPurchaseCost()));
            }
        }
        reportText.append("\n");

        // --- SUMMARY (Purchase Orders) ---
        reportText.append(String.format("Total Expenses of all POs: RM%.2f\n", totalPOExpenses));
        reportText.append("\n");

        textArea.setText(reportText.toString());
        textArea.setCaretPosition(0);
    }

    private void executePrintJob(JTextArea textArea, String jobName) {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName(jobName.replace("_", " "));

            Printable printable = new ReportPrintable(textArea);

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

    private void saveReportToFile(JTextArea textArea, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Report As");
        fileChooser.setSelectedFile(new java.io.File(defaultFileName + ".txt"));

        int userSelection = fileChooser.showSaveDialog(this);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            java.io.File fileToSave = fileChooser.getSelectedFile();
            if (!fileToSave.getName().toLowerCase().endsWith(".txt")) {
                fileToSave = new java.io.File(fileToSave.getAbsolutePath() + ".txt");
            }
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                textArea.write(writer);
                JOptionPane.showMessageDialog(this, "Report saved successfully to:\n" + fileToSave.getAbsolutePath(), "Save Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving report: " + ex.getMessage(), "Save Error", JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }

    private void showPrintPreview(JTextArea textArea, String jobName) {
        try {
            PrinterJob printerJob = PrinterJob.getPrinterJob();
            printerJob.setJobName(jobName.replace("_", " "));

            Printable printable = new ReportPrintable(textArea);

            PageFormat pageFormat = printerJob.defaultPage();
            // --- Explicitly set A4 paper size and margins for preview ---
            Paper paper = new Paper();
            paper.setSize(595.27, 841.89); // A4 dimensions
            double margin = 36; // 0.5 inch margin in points
            paper.setImageableArea(margin, margin, paper.getWidth() - 2 * margin, paper.getHeight() - 2 * margin);
            pageFormat.setPaper(paper);
            // --- End A4 setup for preview ---

            PrintPreviewDialog previewDialog = new PrintPreviewDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this),
                    textArea,
                    printable,
                    pageFormat,
                    jobName
            );
            previewDialog.setVisible(true);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error showing print preview: " + ex.getMessage(), "Preview Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void styleTabbedPane() {
        tabbedPane.setOpaque(false);
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBackground(new Color(30, 40, 60));
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("TabbedPane.selected", new Color(40, 50, 70));
        UIManager.put("TabbedPane.contentBorder", BorderFactory.createEmptyBorder());
        UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
        UIManager.put("TabbedPane.background", new Color(30, 40, 60));
        UIManager.put("TabbedPane.darkShadow", new Color(15, 25, 40));
        UIManager.put("TabbedPane.light", new Color(50, 60, 80));
        UIManager.put("TabbedPane.highlight", new Color(60, 70, 90));
        UIManager.put("TabbedPane.shadow", new Color(20, 30, 50));
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(50, 60, 80), 1));
        scrollPane.getViewport().setBackground(new Color(30, 40, 60));
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(80, 90, 110);
                this.trackColor = new Color(20, 30, 50);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
        scrollPane.getHorizontalScrollBar().setUI(new BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(80, 90, 110);
                this.trackColor = new Color(20, 30, 50);
            }
            @Override
            protected JButton createDecreaseButton(int orientation) {
                return createZeroButton();
            }
            @Override
            protected JButton createIncreaseButton(int orientation) {
                return createZeroButton();
            }
            private JButton createZeroButton() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                button.setMinimumSize(new Dimension(0, 0));
                button.setMaximumSize(new Dimension(0, 0));
                return button;
            }
        });
    }

    static class BasicScrollBarUI extends javax.swing.plaf.basic.BasicScrollBarUI {
        @Override
        protected void installDefaults() {
            super.installDefaults();
        }

        @Override
        protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
            g.setColor(trackColor);
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
        }

        @Override
        protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) {
                return;
            }
            g.setColor(thumbColor);
            g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
        }
    }

    private class ReportPrintable implements Printable {
        private JTextArea textArea;
        private int linesPerPage;
        private Font fontForPrinting; // Declare a dedicated font for printing
        private FontMetrics fontMetrics;
        private int lineHeight;

        public ReportPrintable(JTextArea textArea) {
            this.textArea = textArea;
            // Use a smaller font for printing to ensure it fits A4 width
            // 9pt is a common readable size that often fits more content
            fontForPrinting = new Font("Monospaced", Font.PLAIN, 9);
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

    private class PrintPreviewDialog extends JDialog {
        private Printable printable;
        private PageFormat pageFormat;
        private JTextArea sourceTextArea;
        private String printJobName;

        public PrintPreviewDialog(Frame owner, JTextArea sourceTextArea, Printable printable, PageFormat pageFormat, String printJobName) {
            super(owner, "Print Preview - " + printJobName.replace("_", " "), true);
            this.sourceTextArea = sourceTextArea;
            this.printable = printable;
            this.pageFormat = pageFormat;
            this.printJobName = printJobName;

            initComponents();
            setSize(1000, 800);
            setLocationRelativeTo(owner);
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            JPanel previewPanel = new JPanel() {
                @Override
                public Dimension getPreferredSize() {
                    return new Dimension((int) pageFormat.getWidth(), (int) pageFormat.getHeight());
                }

                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;

                    try {
                        printable.print(g2d, pageFormat, 0);
                    } catch (PrinterException e) {
                        e.printStackTrace();
                        g2d.setColor(Color.RED);
                        // Corrected string concatenation for error message
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
                dispose();
                executePrintJob(sourceTextArea, printJobName);
            });
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> dispose());

            controlPanel.add(printButton);
            controlPanel.add(closeButton);
            add(controlPanel, BorderLayout.SOUTH);
        }
    }
}
