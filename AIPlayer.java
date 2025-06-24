import java.awt.*;
import java.util.*;

public class AIPlayer {
    public enum Difficulty { EASY, MEDIUM, HARD }

    private final Board board;
    private final Seed aiSeed;
    private final Seed opponentSeed;
    private final Difficulty difficulty;

    public AIPlayer(Board board, Seed aiSeed, Difficulty difficulty) {
        this.board = board;
        this.aiSeed = aiSeed;
        this.opponentSeed = (aiSeed == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
        this.difficulty = difficulty;
    }

    public Point getAIMove() {
        return switch (difficulty) {
            case EASY -> getRandomMove();
            case MEDIUM -> (Math.random() < 0.5) ? getRandomMove() : minimaxMove();
            case HARD -> minimaxMove();
        };
    }

    private Point getRandomMove() {
        java.util.List<Point> emptyCells = new java.util.ArrayList<>();
        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    emptyCells.add(new Point(row, col));
                }
            }
        }
        if (emptyCells.isEmpty()) return new Point(0, 0);  // fallback
        return emptyCells.get(new Random().nextInt(emptyCells.size()));
    }


    private Point minimaxMove() {
        int bestScore = Integer.MIN_VALUE;
        Point bestMove = new Point(-1, -1);

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = aiSeed;
                    int score = minimax(0, false);
                    board.cells[row][col].content = Seed.NO_SEED;

                    if (score > bestScore) {
                        bestScore = score;
                        bestMove = new Point(row, col);
                    }
                }
            }
        }
        return bestMove;
    }

    private int minimax(int depth, boolean isMaximizing) {
        State result = evaluateBoard();
        if (result != State.PLAYING) {
            return switch (result) {
                case CROSS_WON -> (aiSeed == Seed.CROSS) ? 10 - depth : depth - 10;
                case NOUGHT_WON -> (aiSeed == Seed.NOUGHT) ? 10 - depth : depth - 10;
                case DRAW -> 0;
                default -> 0;
            };
        }

        int bestScore = isMaximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    board.cells[row][col].content = isMaximizing ? aiSeed : opponentSeed;
                    int score = minimax(depth + 1, !isMaximizing);
                    board.cells[row][col].content = Seed.NO_SEED;

                    if (isMaximizing) {
                        bestScore = Math.max(score, bestScore);
                    } else {
                        bestScore = Math.min(score, bestScore);
                    }
                }
            }
        }
        return bestScore;
    }

    private State evaluateBoard() {
        // Reuse logic from Board.stepGame() without changing state
        for (int row = 0; row < Board.ROWS; row++) {
            if (board.cells[row][0].content != Seed.NO_SEED &&
                    board.cells[row][0].content == board.cells[row][1].content &&
                    board.cells[row][1].content == board.cells[row][2].content) {
                return board.cells[row][0].content == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON;
            }
        }

        for (int col = 0; col < Board.COLS; col++) {
            if (board.cells[0][col].content != Seed.NO_SEED &&
                    board.cells[0][col].content == board.cells[1][col].content &&
                    board.cells[1][col].content == board.cells[2][col].content) {
                return board.cells[0][col].content == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON;
            }
        }

        if (board.cells[0][0].content != Seed.NO_SEED &&
                board.cells[0][0].content == board.cells[1][1].content &&
                board.cells[1][1].content == board.cells[2][2].content) {
            return board.cells[0][0].content == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON;
        }

        if (board.cells[0][2].content != Seed.NO_SEED &&
                board.cells[0][2].content == board.cells[1][1].content &&
                board.cells[1][1].content == board.cells[2][0].content) {
            return board.cells[0][2].content == Seed.CROSS ? State.CROSS_WON : State.NOUGHT_WON;
        }

        for (int row = 0; row < Board.ROWS; row++) {
            for (int col = 0; col < Board.COLS; col++) {
                if (board.cells[row][col].content == Seed.NO_SEED) {
                    return State.PLAYING;
                }
            }
        }

        return State.DRAW;
    }
}