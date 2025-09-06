package GUI;

import javax.swing.*;
import java.awt.*;

public class About extends JPanel {

    public About() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // padding

        JLabel title = new JLabel("About Us – Omega Wholesale Sdn Bhd (OWSB)");
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        JTextArea textArea = getjTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setEditable(false);
        textArea.setFont(new Font("Serif", Font.PLAIN, 14));
        textArea.setBackground(getBackground());

        add(title, BorderLayout.NORTH);
        add(textArea, BorderLayout.CENTER);
    }

    private static JTextArea getjTextArea() {
        String aboutText = """
                OWSB is a growing wholesaler based in Kuala Lumpur, Malaysia.We distribute groceries, produce, and essential goods nationwide.

                We are digitizing our Purchase Order system to improve efficiency,reduce errors, and enhance supplier communications.

                Core values: Integrity, Innovation, Customer-Centricity.
                """;

        JTextArea textArea = new JTextArea(aboutText);
        return textArea;
    }
}
