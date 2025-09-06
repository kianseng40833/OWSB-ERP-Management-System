package GUI;

import Management.Controller.DashboardController;
import javax.swing.*;
import java.awt.*;

public class DashboardPanel extends JPanel {
    private final DashboardController controller;
    private final JLabel poCountLabel;
    private final JLabel prCountLabel;
    private final JLabel salesCountLabel;
    private final JLabel paymentCountLabel;

    public DashboardPanel() {
        // Initialize labels first
        poCountLabel = new JLabel();
        prCountLabel = new JLabel();
        salesCountLabel = new JLabel();
        paymentCountLabel = new JLabel();

        controller = new DashboardController();
        initializeUI();
        loadData();
    }

    private void initializeUI() {
        // Use a more compact layout with 2x2 grid for the cards
        setLayout(new GridLayout(2, 2, 15, 15));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        setBackground(new Color(240, 240, 240)); // Light gray background

        // Initialize and add cards for each metric
        initializeMetricCard(poCountLabel, "Purchase Orders", new Color(65, 105, 225)); // Royal Blue
        initializeMetricCard(prCountLabel, "Purchase Requisitions", new Color(46, 139, 87)); // Sea Green
        initializeMetricCard(salesCountLabel, "Sales", new Color(220, 20, 60)); // Crimson Red
        initializeMetricCard(paymentCountLabel, "Payments", new Color(255, 140, 0)); // Dark Orange
    }

    private void initializeMetricCard(JLabel countLabel, String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 3, 0, accentColor), // Bottom border only
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        card.setBackground(Color.WHITE);
        card.setPreferredSize(new Dimension(200, 120));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(80, 80, 80));

        countLabel.setText("0");
        countLabel.setHorizontalAlignment(SwingConstants.CENTER);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countLabel.setForeground(accentColor);

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(countLabel, BorderLayout.CENTER);

        add(card);
    }

    private void loadData() {
        DashboardController.DashboardSummary summary = controller.loadCountedData();

        // Update labels with the counts
        poCountLabel.setText(String.valueOf(summary.purchaseOrderCount()));
        prCountLabel.setText(String.valueOf(summary.purchaseRequisitionCount()));
        salesCountLabel.setText(String.valueOf(summary.salesCount()));
        paymentCountLabel.setText(String.valueOf(summary.paymentCount()));
    }
}