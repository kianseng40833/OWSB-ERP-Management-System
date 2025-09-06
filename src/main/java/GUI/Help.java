package GUI;

import javax.swing.*;
import java.awt.*;

public class Help extends JPanel {

    public Help() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // padding

        JLabel title = new JLabel("Help");
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
        String helpText = """
If you encounter any issues while using the system or have any questions, please do not hesitate to contact our customer service team.

📞 Customer Service Hotline: ‪+60102108236‬
📧 Email: support@owsb.com.my

Our support team is available Monday to Friday, 9:00 AM – 6:00 PM to assist you.
                        """;

        JTextArea textArea = new JTextArea(helpText);
        return textArea;
    }
}
