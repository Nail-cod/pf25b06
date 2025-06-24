import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class JPanelWelcome extends JPanel {
    private Image backgroundImage;

    public JPanelWelcome(MainFrame frame) {
        // Load background
        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }

        setLayout(new BorderLayout());

        JLabel title = new JLabel("Welcome to Tic Tac Toe", SwingConstants.CENTER);
        title.setFont(new Font("Comic Sans MS", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        JButton playButton = new JButton("Play Game");
        playButton.setFont(new Font("Arial", Font.BOLD, 16));
        playButton.addActionListener(e -> frame.showPage("settings"));

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false); // Transparent background
        buttonPanel.add(playButton);

        add(title, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
