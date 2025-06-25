import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class MainMenuPanel extends JPanel {
    public MainMenuPanel(ActionListener playListener, ActionListener settingsListener) {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(300, 400));

        // Background image
        setOpaque(false);

        JLabel title = new JLabel("Tic Tac Toe");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.BLACK);
        title.setBorder(BorderFactory.createEmptyBorder(80, 0, 30, 0));

        JButton playButton = createStyledButton("Play");
        playButton.addActionListener(playListener);

        JButton settingsButton = createStyledButton("Settings");
        settingsButton.addActionListener(settingsListener);

        add(title);
        add(playButton);
        add(Box.createRigidArea(new Dimension(0, 15)));
        add(settingsButton);
    }

    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setBackground(new Color(255, 255, 255, 160)); // semi-transparent white
        button.setForeground(new Color(50, 200, 50));
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setContentAreaFilled(true);
        button.setOpaque(true);
        return button;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Draw background image
        ImageIcon bg = new ImageIcon(getClass().getClassLoader().getResource("images/jellyfish.jpeg"));
        g.drawImage(bg.getImage(), 0, 0, getWidth(), getHeight(), this);
    }
}

