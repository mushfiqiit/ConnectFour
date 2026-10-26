package Game;

/** Rules shared by the live game and the AI's hypothetical boards. */
public final class BoardRules {
    private static final int CONNECT_LENGTH = 4;

    private BoardRules() {
        // Utility class; no instances needed.
    }

    /**
     * Finds a winner without modifying the supplied board.
     *
     * @param board a non-null rectangular array indexed by column, then row;
     *              cells contain 0 (empty), 1 (red), or -1 (yellow)
     * @return the winning player's value, or 0 if there is no winner
     */
    public static int checkWinner(int[][] board) {
        for (int column = 0; column < board.length; column++) {
            for (int row = 0; row < board[0].length; row++) {
                int player = board[column][row];
                if (player == 0) {
                    continue;
                }

                if (hasWinningLine(board, column, row, 1, 0)       // Horizontal
                        || hasWinningLine(board, column, row, 0, 1)  // Vertical
                        || hasWinningLine(board, column, row, 1, 1)  // Rising diagonal
                        || hasWinningLine(board, column, row, 1, -1)) { // Falling diagonal
                    return player;
                }
            }
        }
        return 0;
    }

    private static boolean hasWinningLine(
            int[][] board, int column, int row, int columnStep, int rowStep) {
        int endColumn = column + (CONNECT_LENGTH - 1) * columnStep;
        int endRow = row + (CONNECT_LENGTH - 1) * rowStep;

        if (endColumn < 0 || endColumn >= board.length
                || endRow < 0 || endRow >= board[0].length) {
            return false;
        }

        int player = board[column][row];
        for (int offset = 1; offset < CONNECT_LENGTH; offset++) {
            int nextColumn = column + offset * columnStep;
            int nextRow = row + offset * rowStep;
            if (board[nextColumn][nextRow] != player) {
                return false;
            }
        }
        return true;
    }

}
