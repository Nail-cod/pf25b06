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
                int mouseX = e.getX();
                int mouseY = e.getY();

                // Hitung offset board yang berada di tengah
                int boardWidth = Board.CANVAS_WIDTH;
                int boardHeight = Board.CANVAS_HEIGHT;
                int offsetX = (getWidth() - boardWidth) / 2;
                int offsetY = (getHeight() - boardHeight - statusBar.getHeight()) / 2;

                int adjustedX = mouseX - offsetX;
                int adjustedY = mouseY - offsetY;

                // Pastikan klik terjadi di dalam area board
                if (adjustedX >= 0 && adjustedY >= 0 &&
                        adjustedX < boardWidth && adjustedY < boardHeight) {

                    int row = adjustedY / Cell.SIZE;
                    int col = adjustedX / Cell.SIZE;

                    if (currentState == State.PLAYING) {
                        if (board.cells[row][col].content == Seed.NO_SEED) {
                            currentState = board.stepGame(currentPlayer, row, col);
                            SoundEffect.EAT_FOOD.play();
                            repaint();

                            if (gameMode == GameMode.HUMAN_VS_AI && currentState == State.PLAYING) {
                                currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;

                                // Delay sebelum AI bergerak
                                Timer timer = new Timer(600, evt -> {
                                    AIPlayer ai = new AIPlayer(board, currentPlayer, AIPlayer.Difficulty.HARD);
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
                            } else if (gameMode == GameMode.HUMAN_VS_HUMAN && currentState == State.PLAYING) {
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

        // Hitung posisi untuk meletakkan board di tengah
        int boardWidth = Board.CANVAS_WIDTH;
        int boardHeight = Board.CANVAS_HEIGHT;
        int x = (getWidth() - boardWidth) / 2;
        int y = (getHeight() - boardHeight - statusBar.getHeight()) / 2;

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.translate(x, y);  // geser canvas menggambar ke titik (x,y)
        board.paint(g2d);     // gambar board dengan offset
        g2d.dispose();

        // Status bar tetap
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
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(600, 400);
            frame.setLocationRelativeTo(null);

            // [1] Inisialisasi playListener dan settingsListener
            ActionListener[] playListenerHolder = new ActionListener[1];  // temp holder karena playListener butuh akses ke dirinya sendiri

            ActionListener settingsListener = e -> {
                JOptionPane.showMessageDialog(frame, "Settings page coming soon!");
            };

            ActionListener playListener = e -> {
                GameMain gamePanel = new GameMain();
                gamePanel.setGameChangeListener(() -> {
                    // kembali ke menu
                    MainMenuPanel menuPanel = new MainMenuPanel(playListenerHolder[0], settingsListener);
                    frame.setContentPane(menuPanel);
                    frame.revalidate();
                    frame.repaint();
                });
                frame.setContentPane(gamePanel);
                frame.revalidate();
            };
            playListenerHolder[0] = playListener; // isi holder setelah selesai

            // [2] Buat panel menu utama
            MainMenuPanel menuPanel = new MainMenuPanel(playListener, settingsListener);
            frame.setContentPane(menuPanel);
            frame.setVisible(true);
        });
    }
}