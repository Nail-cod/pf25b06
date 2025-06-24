import javax.swing.*;
import java.awt.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import java.net.URL;

import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;

public class JPanelSettings extends JPanel {
    private final MainFrame mainFrame;
    private final JComboBox<String> modeCombo;
    private final JComboBox<String> symbolCombo;
    private final JComboBox<String> levelCombo;
    private final JSlider volumeSlider;
    private Image backgroundImage;

    public JPanelSettings(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());

        // Load background
        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }

        // Komponen UI
        JLabel title = new JLabel("Game Settings");
        title.setFont(new Font("Arial", Font.BOLD, 24));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        modeCombo = new JComboBox<>(new String[]{"Human vs Human", "Human vs AI"});
        symbolCombo = new JComboBox<>(new String[]{"Spongebob (X)", "Patrick (O)"});
        levelCombo = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});

        JButton startButton = new JButton("Start Game");
        startButton.addActionListener(e -> {
            GameMain.GameMode mode = (modeCombo.getSelectedIndex() == 0) ?
                    GameMain.GameMode.HUMAN_VS_HUMAN : GameMain.GameMode.HUMAN_VS_AI;

            Seed player = (symbolCombo.getSelectedIndex() == 0) ? Seed.CROSS : Seed.NOUGHT;

            AIPlayer.Difficulty difficulty = AIPlayer.Difficulty.EASY;
            switch (levelCombo.getSelectedIndex()) {
                case 1 -> difficulty = AIPlayer.Difficulty.MEDIUM;
                case 2 -> difficulty = AIPlayer.Difficulty.HARD;
            }

            mainFrame.setGameSettings(mode, difficulty, player);
            mainFrame.startGame();
        });

        // Label Volume
        JLabel volumeLabel = new JLabel("Backsound Volume:");
        volumeLabel.setForeground(Color.WHITE);
        volumeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        volumeLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Volume Slider
        volumeSlider = new JSlider(0, 100, 70);
        volumeSlider.setMajorTickSpacing(25);
        volumeSlider.setPaintTicks(true);
        volumeSlider.setPaintLabels(true);
        volumeSlider.setOpaque(false);
        volumeSlider.addChangeListener(e -> {
            int value = volumeSlider.getValue(); // 0 - 100
            if (SoundEffect.BACKGROUND.clip != null && SoundEffect.BACKGROUND.clip.isOpen()) {
                FloatControl control = (FloatControl) SoundEffect.BACKGROUND.clip.getControl(FloatControl.Type.MASTER_GAIN);

                float min = control.getMinimum(); // biasanya -80.0 dB
                float gain;

                if (value == 0) {
                    gain = min; // mute
                } else {
                    // Konversi ke gain logaritmis agar terasa natural
                    float volumeLinear = value / 100f;
                    gain = (float) (20f * Math.log10(volumeLinear)); // konversi ke dB
                    gain = Math.max(gain, min); // pastikan tidak melebihi batas minimal
                }

                control.setValue(gain);
            }
        });



        // Layout
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 20, 10, 20);
        gbc.gridx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridy = 0;
        add(title, gbc);

        gbc.gridy++;
        add(new JLabel("Select Mode:"), gbc);
        gbc.gridy++;
        add(modeCombo, gbc);

        gbc.gridy++;
        add(new JLabel("Select Character:"), gbc);
        gbc.gridy++;
        add(symbolCombo, gbc);

        gbc.gridy++;
        add(new JLabel("AI Difficulty:"), gbc);
        gbc.gridy++;
        add(levelCombo, gbc);

        gbc.gridy++;
        add(volumeLabel, gbc);
        gbc.gridy++;
        add(volumeSlider, gbc);

        gbc.gridy++;
        add(startButton, gbc);
    }

    private void setVolume(Clip clip, float volume) {
        if (clip != null && clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float min = gainControl.getMinimum();
            float max = gainControl.getMaximum();
            float gain = min + (max - min) * volume;
            gainControl.setValue(gain);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}
