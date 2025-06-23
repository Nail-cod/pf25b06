import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import javax.swing.*;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;
    /**
     * Tic-Tac-Toe: Two-player Graphic version with better OO design.
     * The Board and Cell classes are separated in their own classes.
     */
    // Define named constants for the drawing graphics
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG_STATUS = new Color(247, 255, 0);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);

    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;

    private Image backgroundImage;

    public GameMain() {
        // Load background image
        URL bgURL = getClass().getClassLoader().getResource("images/jellyfish.jpeg");
        if (bgURL != null) {
            backgroundImage = new ImageIcon(bgURL).getImage();
        } else {
            System.err.println("Couldn't find background image!");
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

                        if (currentPlayer == Seed.CROSS) {
                            SoundEffect.EAT_FOOD.play();  // Suara X
                        } else {
                            SoundEffect.DIE.play();       // Suara O
                        }

                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    }
                } else {
                    SoundEffect.EXPLODE.play();  // Suara restart
                    newGame();
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

        SoundEffect.initGame();
        initGame();
        newGame();
    }

    public void initGame() {
        board = new Board();
    }

    public void newGame() {
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
                board.cells[row][col].content = Seed.NO_SEED; // all cells empty
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

        // Draw background image
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        board.paint(g);

        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText((currentPlayer == Seed.CROSS) ? "Spongebob Turn" : "Patrick's Turn");
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            frame.setContentPane(new GameMain());
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    SoundEffect.BACKGROUND.clip.stop();  // Stop backsound saat aplikasi ditutup
                }
            });
        });
    }
}