import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    public enum GameMode {
        HUMAN_VS_HUMAN,
        HUMAN_VS_AI
    }

    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;
    private Image backgroundImage;

    private GameMode gameMode;
    private AIPlayer.Difficulty difficulty;
    private MainFrame mainFrame;

    public GameMain(MainFrame mainFrame, GameMode gameMode, AIPlayer.Difficulty difficulty, Seed firstPlayer) {
        this.mainFrame = mainFrame;
        this.gameMode = gameMode;
        this.difficulty = difficulty;
        this.currentPlayer = firstPlayer;

        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }

        initGame();
        setupUI();
    }

    private String getResultMessage() {
        return switch (currentState) {
            case CROSS_WON -> "'Spongebob' Won! What do you want to do?";
            case NOUGHT_WON -> "'Patrick' Won! What do you want to do?";
            case DRAW -> "It's a Draw! What do you want to do?";
            default -> "Game Over!";
        };
    }

    private void setupUI() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX() - (getWidth() - Board.CANVAS_WIDTH) / 2;
                int mouseY = e.getY();
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        currentState = board.stepGame(currentPlayer, row, col);
                        SoundEffect.EAT_FOOD.play();
                        repaint();

                        if (gameMode == GameMode.HUMAN_VS_AI && currentState == State.PLAYING) {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;

                            Timer timer = new Timer(600, evt -> {
                                AIPlayer ai = new AIPlayer(board, currentPlayer, difficulty);
                                Point move = ai.getAIMove();
                                currentState = board.stepGame(currentPlayer, move.x, move.y);
                                SoundEffect.DIE.play();
                                if (currentState == State.PLAYING) {
                                    currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                                }
                                repaint();
                            });
                            timer.setRepeats(false);
                            timer.start();
                        } else {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        }
                    }
                } else {
                    SoundEffect.EXPLODE.play();

                    int option = JOptionPane.showOptionDialog(
                            GameMain.this,
                            getResultMessage(),
                            "Game Over",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            new Object[]{"Play Again", "Main Menu"},
                            "Play Again"
                    );

                    if (option == JOptionPane.YES_OPTION) {
                        initGame(); // Reset ulang game
                        repaint();
                    } else if (option == JOptionPane.NO_OPTION) {
                        if (mainFrame != null) {
                            mainFrame.showPage("mainMenu");
                        }
                    }
                }


                repaint();
            }
        });

        statusBar = new JLabel();
        statusBar.setFont(new Font("OCR A Extended", Font.PLAIN, 14));
        statusBar.setBackground(new Color(247, 255, 0));
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        setLayout(new BorderLayout());
        add(statusBar, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        setBorder(BorderFactory.createLineBorder(new Color(247, 255, 0), 2));
    }

    public void initGame() {
        board = new Board();
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED;
            }
        }
        currentState = State.PLAYING;

        SoundEffect.initGame(); // Tambahkan ini agar semua clip reload
        if (!SoundEffect.BACKGROUND.clip.isRunning()) {
            SoundEffect.BACKGROUND.play();
        }
    }


    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        Graphics2D g2d = (Graphics2D) g.create();
        int xOffset = (getWidth() - Board.CANVAS_WIDTH) / 2;
        g2d.translate(xOffset, 0);
        board.paint(g2d);
        g2d.dispose();

        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText((currentPlayer == Seed.CROSS) ? "Spongebob's Turn" : "Patrick's Turn");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again.");
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'Spongebob' Won! Click to play again.");
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'Patrick' Won! Click to play again.");
        }
    }

    // Optional - Getter dan Setter
    public void setCurrentPlayer(Seed player) {
        this.currentPlayer = player;
    }

    public void setGameMode(GameMode mode) {
        this.gameMode = mode;
    }

    public void setDifficulty(AIPlayer.Difficulty diff) {
        this.difficulty = diff;
    }
}
