package GUI;

import javax.swing.*;
import java.awt.*;

public class Faq extends JPanel{
    public Faq() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // padding

        JLabel title = new JLabel(" Frequently Asked Questions (FAQ)");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        String aboutText = """
                1. Who can use the OWSB Purchase Order Management System?
                    Only registered users with valid login credentials can access the system.

                2. I forgot my password. What should I do?
                     Contact the system administrator via hotline or email.
                     
                3. How do I create a Purchase Requisition (PR)?
                    Sales Managers can create a PR in the “Create PR” section.
                                
                4. Who is allowed to generate Purchase Orders (POs)?
                    Only Purchase Managers based on approved PRs.
                                
                5. Can I edit or delete item or supplier information?
                    Yes. Sales Managers and Admins can do so with caution.
                                
                6. How is stock updated?
                    Inventory Manager updates stock once items are received.
                                
                7. How do I register a new user?
                    Only Admins can register new users.
                                
                8. What file formats are used?
                    Data is stored in .txt format.
                                
                9. How can I view reports?
                    Sales and Inventory Managers can view respective reports.
                                
                10. Who do I contact for support?
                    📞 ‪+60 102108236‬‬ | 📧 support@owsb.com.my
                """;

        JScrollPane scrollPane = getjScrollPane(aboutText);

        add(title, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JScrollPane getjScrollPane(String aboutText) {
        JTextArea textArea = new JTextArea(aboutText);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Serif", Font.PLAIN, 14));
        textArea.setBackground(getBackground());

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        return scrollPane;
    }
}
