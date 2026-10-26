package Game;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import java.util.concurrent.ExecutionException;

public class Game {
    private static final int COLUMN_COUNT = 7;
    private static final int ROW_COUNT = 6;
    private static final int EMPTY = 0;
    private static final int RED_PLAYER = 1;
    private static final int YELLOW_PLAYER = -1;
    private static final int GAME_OVER = 0;

    private static Game instance;

    private final AI ai;
    // The board is indexed by column, then by row from the bottom.
    private final int[][] board = new int[COLUMN_COUNT][ROW_COUNT];
    private int currentPlayer = YELLOW_PLAYER;
    private boolean acceptingHumanInput = true;

    public static void startGame(AI ai) {
        if (instance == null) {
            instance = new Game(ai);
        }
    }

    public static void startGame() {
        startGame(null);
    }

    public static Game getInstance() {
        startGame();
        return instance;
    }

    private Game(AI ai) {
        this.ai = ai;
    }

    private void drawBoard() {
        for (int column = 0; column < board.length; column++) {
            FrameGenerator.columns()[column].drawColumn(board[column]);
        }
    }

    private int checkWinner() {
        return BoardRules.checkWinner(board);
    }

    private void updateBoard(int columnIndex) {
        if (!isGameActive() || !isLegalColumn(columnIndex)) {
            return;
        }

        int row = findFirstEmptyRow(columnIndex);
        board[columnIndex][row] = currentPlayer;
        FrameGenerator.columns()[columnIndex].incrementSize();
        currentPlayer *= -1;
        drawBoard();
        checkGameResult();
    }

    private int findFirstEmptyRow(int columnIndex) {
        for (int row = 0; row < board[columnIndex].length; row++) {
            if (board[columnIndex][row] == EMPTY) {
                return row;
            }
        }
        throw new IllegalStateException("Column is full: " + columnIndex);
    }

    private void checkGameResult() {
        int winner = checkWinner();
        if (winner != EMPTY) {
            finishGame(winner == RED_PLAYER ? "Red Won" : "Yellow Won");
        } else if (!hasLegalMove()) {
            finishGame("Match Drawn");
        }
    }

    private void finishGame(String message) {
        currentPlayer = GAME_OVER;
        JOptionPane.showMessageDialog(FrameGenerator.getFrame(), message);
    }

    private boolean isGameActive() {
        return currentPlayer != GAME_OVER;
    }

    static int[][] copyBoard(int[][] source) {
        int[][] copy = new int[source.length][];
        for (int column = 0; column < source.length; column++) {
            copy[column] = source[column].clone();
        }
        return copy;
    }

    public void playHumanMove(int columnIndex) {
        if (!isGameActive() || !acceptingHumanInput || !isLegalColumn(columnIndex)) {
            return;
        }

        // Lock before drawing: a winner dialog runs a nested Swing event loop.
        acceptingHumanInput = false;
        updateBoard(columnIndex);
        if (ai != null && isGameActive()) {
            requestAiMove();
        } else {
            acceptingHumanInput = true;
        }
    }

    private void requestAiMove() {
        int[][] snapshot = copyBoard(board);
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ai.play(snapshot);
            }

            @Override
            protected void done() {
                try {
                    applyAiMove(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    currentPlayer = GAME_OVER;
                } catch (ExecutionException e) {
                    handleAiFailure(e.getCause());
                } catch (RuntimeException e) {
                    handleAiFailure(e);
                } finally {
                    acceptingHumanInput = true;
                }
            }
        }.execute();
    }

    private void applyAiMove(int columnIndex) {
        if (!isLegalColumn(columnIndex)) {
            throw new IllegalStateException("AI selected an illegal column: " + columnIndex);
        }
        updateBoard(columnIndex);
    }

    private void handleAiFailure(Throwable failure) {
        currentPlayer = GAME_OVER;
        failure.printStackTrace();
        JOptionPane.showMessageDialog(FrameGenerator.getFrame(),
                "The AI could not choose a move. Please restart the game.");
    }

    private boolean isLegalColumn(int columnIndex) {
        return columnIndex >= 0
                && columnIndex < board.length
                && !isColumnFull(columnIndex);
    }

    private boolean isColumnFull(int columnIndex) {
        return board[columnIndex][board[columnIndex].length - 1] != EMPTY;
    }

    private boolean hasLegalMove() {
        for (int column = 0; column < board.length; column++) {
            if (!isColumnFull(column)) {
                return true;
            }
        }
        return false;
    }
}
