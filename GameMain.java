import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    // Constants
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG_STATUS = new Color(247, 255, 0);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    public enum GameMode {
        HUMAN_VS_HUMAN,
        HUMAN_VS_AI
    }

    private GameMode gameMode = GameMode.HUMAN_VS_AI;// Enums & Modes

    // Game state
    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;

    private Image backgroundImage;

    public interface GameChangeListener {
        void onRequestChangeMode();
    }

    private GameChangeListener changeListener;

    public void setGameChangeListener(GameChangeListener listener) {
        this.changeListener = listener;
    }

    public GameMain() {
        // Load background image
        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        } else {
            System.err.println("Couldn't find background!");
        }

        // Mouse listener
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
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
                            Timer aiTimer = new Timer(1000, new ActionListener() {
                                @Override
                                public void actionPerformed(ActionEvent evt) {
                                    makeAIMove();
                                    if (currentState == State.PLAYING) {
                                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                                    }
                                    ((Timer) evt.getSource()).stop(); // Hentikan timer setelah sekali jalan
                                    repaint();
                                }
                            });
                            aiTimer.setRepeats(false); // Hanya jalan sekali
                            aiTimer.start();

                        } else {
                            currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                        }
                    }
                } else {
                    SoundEffect.EXPLODE.play();  // Suara restart

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
                        resetGameOnly();  // hanya reset board
                    } else if (choice == JOptionPane.NO_OPTION) {
                        if (changeListener != null) {
                            changeListener.onRequestChangeMode(); // Ini memicu recreate dari luar
                        }
                    }

                }
                repaint();
            }
        });

        // Status bar
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        super.setLayout(new BorderLayout());
        super.add(statusBar, BorderLayout.PAGE_END);
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2));

        initGame();
        newGame();
    }

    private void makeAIMove() {
        java.util.List<Point> emptyCells = new java.util.ArrayList<>();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    emptyCells.add(new Point(row, col));
                }
            }
        }

        if (!emptyCells.isEmpty()) {
            Point move = emptyCells.get((int) (Math.random() * emptyCells.size()));
            currentState = board.stepGame(currentPlayer, move.x, move.y);
            SoundEffect.DIE.play(); // misalnya suara AI berbeda
            repaint();
        }
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

    public static void recreateMainPanel(JFrame frame) {
        frame.getContentPane().removeAll(); // Hapus semua panel lama
        GameMain newPanel = new GameMain(); // Buat panel baru
        frame.setContentPane(newPanel);     // Ganti content pane
        frame.revalidate();                 // Refresh frame
        frame.repaint();                    // Gambar ulang
    }

    private static JFrame mainFrame;

    public static void setMainFrame(JFrame frame) {
        mainFrame = frame;
    }


    private String getResultMessage() {
        switch (currentState) {
            case CROSS_WON: return "'Spongebob' Won! What do you want to do next?";
            case NOUGHT_WON: return "'Patrick' Won! What do you want to do next?";
            case DRAW: return "It's a Draw! What do you want to do next?";
            default: return "Game Over!";
        }
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

            // Buat method pembuat GameMain
            GameChangeListener listener = new GameChangeListener() {
                @Override
                public void onRequestChangeMode() {
                    frame.getContentPane().removeAll();
                    GameMain newGame = new GameMain();
                    newGame.setGameChangeListener(this);
                    frame.setContentPane(newGame);
                    frame.revalidate();
                    frame.repaint();
                }
            };

            GameMain gamePanel = new GameMain();
            gamePanel.setGameChangeListener(listener);

            frame.setContentPane(gamePanel);
            setMainFrame(frame);
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