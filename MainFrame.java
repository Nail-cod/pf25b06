import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final CardLayout cardLayout;
    private final JPanel cards;

    private GameMain.GameMode gameMode;
    private AIPlayer.Difficulty difficulty;
    private Seed currentPlayer;

    public MainFrame() {
        setTitle("Tic Tac Toe");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(600, 500);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        JPanel welcome = new JPanelWelcome(this);
        JPanel settings = new JPanelSettings(this);

        cards.add(welcome, "mainMenu");
        cards.add(settings, "settings");
        add(cards);

        showPage("welcome");
    }

    public void showPage(String name) {
        cardLayout.show(cards, name);
    }

    public void setGameSettings(GameMain.GameMode mode, AIPlayer.Difficulty difficulty, Seed player) {
        this.gameMode = mode;
        this.difficulty = difficulty;
        this.currentPlayer = player;
    }

    public void startGame() {
        GameMain gamePanel = new GameMain(this, gameMode, difficulty, currentPlayer);
        cards.add(gamePanel, "game");
        showPage("game");
    }

    // 🔽 Tambahkan method main ini agar bisa dijalankan
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}

