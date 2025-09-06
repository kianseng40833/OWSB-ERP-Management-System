package GUI;

import Management.Controller.ItemController;
import Management.Model.Item;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.List;

public class Showitem extends JPanel {
    private JTable itemTable;
    private DefaultTableModel tableModel;
    private final String[] columnNames = {"Item Name", "Price (RM)", "In Stock"};

    public Showitem() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(15, 25, 40));
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Table model
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        // Setup UI
        setupUI();
        loadItemData();
    }

    private void setupUI() {
        JLabel titleLabel = new JLabel("<html><u>Item List</u></html>");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(titleLabel, BorderLayout.WEST);

        itemTable = new JTable(tableModel);
        styleTable();

        JScrollPane scrollPane = new JScrollPane(itemTable);
        styleScrollPane(scrollPane);

        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void styleTable() {
        itemTable.setFillsViewportHeight(true);
        itemTable.setRowHeight(30);
        itemTable.setFont(new Font("SansSerif", Font.PLAIN, 12));
        itemTable.setForeground(Color.WHITE);
        itemTable.setBackground(new Color(30, 40, 60));
        itemTable.setGridColor(new Color(60, 70, 90));
        itemTable.setSelectionBackground(new Color(70, 130, 180));
        itemTable.setSelectionForeground(Color.WHITE);

        JTableHeader header = itemTable.getTableHeader();
        header.setBackground(new Color(40, 55, 75));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 13));
        header.setPreferredSize(new Dimension(100, 35));
        header.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 90)));

        TableColumnModel colModel = itemTable.getColumnModel();
        if (colModel.getColumnCount() == columnNames.length) {
            colModel.getColumn(0).setPreferredWidth(200);  // Item Name
            colModel.getColumn(1).setPreferredWidth(100);  // Price
            colModel.getColumn(2).setPreferredWidth(80);   // In Stock
        }
    }

    private void styleScrollPane(JScrollPane scrollPane) {
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 70, 90), 1));
        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                this.thumbColor = new Color(70, 85, 105);
                this.trackColor = new Color(30, 40, 60);
            }
        });
    }

    private void loadItemData() {
        ItemController controller = new ItemController();
        controller.loadItems();
        List<Item> itemList = controller.getItemList();

        tableModel.setRowCount(0);
        for (Item item : itemList) {
            // Solution 1: If getPrice() returns int but you want decimal places
            tableModel.addRow(new Object[]{
                    item.getItemName(),
                    String.format("%.2f", (double)item.getPrice()/100), // Convert to RM and sen
                    item.getQuantity()
            });
            
            // OR Solution 2: If you want whole RM amounts
            // tableModel.addRow(new Object[]{
            //        item.getItemName(),
            //        "RM " + item.getPrice(),
            //        item.getQuantity()
            // });
        }
    }
}