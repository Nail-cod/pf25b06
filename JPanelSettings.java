import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class JPanelSettings extends JPanel {
    private Image backgroundImage;

    public JPanelSettings(MainFrame frame) {
        // Load background image
        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }

        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel modeLabel = new JLabel("Select Game Mode:");
        modeLabel.setForeground(Color.WHITE);
        modeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"Human vs Human", "Human vs AI"});

        JLabel symbolLabel = new JLabel("Choose Symbol:");
        symbolLabel.setForeground(Color.WHITE);
        symbolLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<String> symbolCombo = new JComboBox<>(new String[]{"Spongebob (X)", "Patrick (O)"});

        JLabel difficultyLabel = new JLabel("Select AI Difficulty:");
        difficultyLabel.setForeground(Color.WHITE);
        difficultyLabel.setFont(new Font("Arial", Font.BOLD, 16));

        JComboBox<String> levelCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        JButton startButton = new JButton("Start Game");
        startButton.setFont(new Font("Arial", Font.BOLD, 16));
        startButton.addActionListener(e -> {
            GameMain.GameMode mode = modeCombo.getSelectedIndex() == 0
                    ? GameMain.GameMode.HUMAN_VS_HUMAN : GameMain.GameMode.HUMAN_VS_AI;

            AIPlayer.Difficulty difficulty = switch (levelCombo.getSelectedIndex()) {
                case 0 -> AIPlayer.Difficulty.EASY;
                case 1 -> AIPlayer.Difficulty.MEDIUM;
                case 2 -> AIPlayer.Difficulty.HARD;
                default -> AIPlayer.Difficulty.EASY;
            };

            Seed player = symbolCombo.getSelectedIndex() == 0 ? Seed.CROSS : Seed.NOUGHT;

            frame.setGameSettings(mode, difficulty, player);
            frame.startGame();
        });

        // Layout positioning
        gbc.gridx = 0;
        gbc.gridy = 0;
        add(modeLabel, gbc);

        gbc.gridy++;
        add(modeCombo, gbc);

        gbc.gridy++;
        add(symbolLabel, gbc);

        gbc.gridy++;
        add(symbolCombo, gbc);

        gbc.gridy++;
        add(difficultyLabel, gbc);

        gbc.gridy++;
        add(levelCombo, gbc);

        gbc.gridy++;
        add(startButton, gbc);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
