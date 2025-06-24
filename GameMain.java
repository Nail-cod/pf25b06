import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG_STATUS = new Color(247, 255, 0);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    public enum GameMode {
        HUMAN_VS_HUMAN,
        HUMAN_VS_AI
    }

    private GameMode gameMode = GameMode.HUMAN_VS_AI;

    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;
    private Image backgroundImage;
    private AIPlayer.Difficulty aiDifficulty = AIPlayer.Difficulty.EASY;


    public interface GameChangeListener {
        void onRequestChangeMode();
    }

    private GameChangeListener changeListener;

    public void setGameChangeListener(GameChangeListener listener) {
        this.changeListener = listener;
    }

    public GameMain() {
        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        }

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = e.getY() / Cell.SIZE;
                int col = e.getX() / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        currentState = board.stepGame(currentPlayer, row, col);
                        SoundEffect.EAT_FOOD.play();
                        repaint();

                        if (gameMode == GameMode.HUMAN_VS_AI && currentState == State.PLAYING) {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;

                            new Timer(600, evt -> {
                                AIPlayer ai = new AIPlayer(board, currentPlayer, aiDifficulty);
                                Point move = ai.getAIMove();
                                currentState = board.stepGame(currentPlayer, move.x, move.y);
                                SoundEffect.DIE.play();

                                if (currentState == State.PLAYING) {
                                    currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                                }
                                repaint();
                                ((Timer) evt.getSource()).stop();
                            }).start();
                        } else {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        }
                    }
                } else {
                    SoundEffect.EXPLODE.play();
                    int choice = JOptionPane.showOptionDialog(
                            GameMain.this,
                            getResultMessage(),
                            "Game Over",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.QUESTION_MESSAGE,
                            null,
                            new String[]{"Reset Game", "Change Mode"},
                            "Reset Game"
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        resetGameOnly();
                    } else if (choice == JOptionPane.NO_OPTION && changeListener != null) {
                        changeListener.onRequestChangeMode();
                    }
                }
                repaint();
            }
        });

        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        setLayout(new BorderLayout());
        add(statusBar, BorderLayout.PAGE_END);
        setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2));

        initGame();
        newGame();
    }

    public void initGame() {
        board = new Board();
    }

    public void newGame() {
        // Pilih mode permainan
        Object[] modeOptions = {"Player vs Player", "Player vs AI"};
        int modeChoice = JOptionPane.showOptionDialog(
                this,
                "Select Game Mode:",
                "Game Mode",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                modeOptions,
                modeOptions[0]
        );
        gameMode = (modeChoice == JOptionPane.YES_OPTION) ? GameMode.HUMAN_VS_HUMAN : GameMode.HUMAN_VS_AI;
        // Jika AI dipilih, tampilkan pilihan tingkat kesulitan
        if (gameMode == GameMode.HUMAN_VS_AI) {
            Object[] difficultyOptions = {"Easy", "Medium", "Hard"};
            int diffChoice = JOptionPane.showOptionDialog(
                    this,
                    "Select AI Difficulty:",
                    "AI Difficulty",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    difficultyOptions,
                    difficultyOptions[0]
            );
            aiDifficulty = switch (diffChoice) {
                case 0 -> AIPlayer.Difficulty.EASY;
                case 1 -> AIPlayer.Difficulty.MEDIUM;
                case 2 -> AIPlayer.Difficulty.HARD;
                default -> AIPlayer.Difficulty.EASY; // default fallback
            };
        }
        // Pilih simbol di awal
        Object[] options = {"Spongebob (X)", "Patrick (O)"};
        int choice = JOptionPane.showOptionDialog(
                this,
                "Choose your character:",
                "Player Selection",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]
        );
        if (choice == JOptionPane.YES_OPTION) {
            currentPlayer = Seed.CROSS;
        } else {
            currentPlayer = Seed.NOUGHT;
        }
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED;
            }
        }
        currentState = State.PLAYING;
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
        board.paint(g);
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

    private String getResultMessage() {
        return switch (currentState) {
            case CROSS_WON -> "'Spongebob' Won! What do you want to do next?";
            case NOUGHT_WON -> "'Patrick' Won! What do you want to do next?";
            case DRAW -> "It's a Draw! What do you want to do next?";
            default -> "Game Over!";
        };
    }

    public void resetGameOnly() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED;
            }
        }
        currentState = State.PLAYING;
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);

            // Buat array satu elemen untuk menampung listener (supaya bisa diakses dari lambda)
            final GameChangeListener[] listenerRef = new GameChangeListener[1];

            listenerRef[0] = () -> {
                frame.getContentPane().removeAll();
                GameMain newGame = new GameMain();
                newGame.setGameChangeListener(listenerRef[0]); // gunakan listener yang sama
                frame.setContentPane(newGame);
                frame.revalidate();
                frame.repaint();
            };

            GameMain gamePanel = new GameMain();
            gamePanel.setGameChangeListener(listenerRef[0]);

            frame.setContentPane(gamePanel);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    SoundEffect.BACKGROUND.clip.stop();
                }
            });
        });
    }

}
