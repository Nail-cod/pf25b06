package process_5;

public State stepGame(Seed player, int selectedRow, int selectedCol) {
    // Update game board
    board[selectedRow][selectedCol] = player;

    // Compute and return the new game state
    if (board[selectedRow][0] == player  // 3-in-the-row
            && board[selectedRow][1] == player
            && board[selectedRow][2] == player
            || board[0][selectedCol] == player // 3-in-the-column
            && board[1][selectedCol] == player
            && board[2][selectedCol] == player
            || selectedRow == selectedCol  // 3-in-the-diagonal
            && board[0][0] == player
            && board[1][1] == player
            && board[2][2] == player
            || selectedRow + selectedCol == 2 // 3-in-the-opposite-diagonal
            && board[0][2] == player
            && board[1][1] == player
            && board[2][0] == player) {
        return (player == Seed.CROSS) ? State.CROSS_WON : State.NOUGHT_WON;
    } else {
        // Nobody win. Check for DRAW (all cells occupied) or PLAYING.
        for (int row = 0; row < ROWS; ++row) {
            for (int col = 0; col < COLS; ++col) {
                if (board[row][col] == Seed.NO_SEED) {
                    return State.PLAYING; // still have empty cells
                }
            }
        }
        return State.DRAW; // no empty cell, it's a draw
    }
}

// Play appropriate sound clip
if (currentState == State.PLAYING) {
        SoundEffect.EAT_FOOD.play();
} else {
        SoundEffect.DIE.play();
}
