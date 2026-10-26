package Game;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class Game {

    private AI ai;

    private static final int COLUMN_COUNT = 7;
    private static final int ROW_COUNT = 6;
    private static final int CONNECT_LENGTH = 4;

    // The board is indexed by column, then by row from the bottom.
    private final int[][] board = new int[COLUMN_COUNT][ROW_COUNT];
    private int currentPlayer = -1;
    private boolean acceptingHumanInput = true;

    public static Game instance;
    public static void startGame(AI ai) throws Exception {
        if (instance == null) {
            instance = new Game(ai);
        }
    }
    public static void startGame(){
        if (instance==null){
            instance = new Game();
        }
    }
    public static Game getInstance(){
        if(instance==null){
            instance = new Game();
        }
        return instance;
    }

    private Game(AI ai) throws Exception {
        this.ai =ai;
    }

    private Game(){

    }

    public void drawBoard() {
        for (int i=0; i<COLUMN_COUNT; i++){
            FrameGenerator.columns()[i].drawColumn(board[i]);
        }
        if (checkWinner() != 0) {
            currentPlayer = 0;

//            System.out.println(ai.toString());

//            if(ai != null){
//                if (checkWinner() == 1) {
//                    JOptionPane.showMessageDialog(null, "AI Won");
//                } else {
//                    JOptionPane.showMessageDialog(null, "Human Won");
//                }
//            }
//            else {
                if (checkWinner() == 1) {
                    JOptionPane.showMessageDialog(null, "Red Won");
                } else {
                    JOptionPane.showMessageDialog(null, "Yellow Won");
                }
//            }





//            System.out.println((
//                checkWinner() ==-1 ?
//                    "Human Won":
//                    (
//                        checkWinner() ==1?
//                            "AI Won":
//                            "Match Drawn"
//                    )
//            ));
            currentPlayer = 0;
        }
    }

    public int checkWinner() {
        for (int column = 0; column < COLUMN_COUNT; column++) {
            for (int row = 0; row < ROW_COUNT; row++) {
                int player = board[column][row];
                if (player == 0) {
                    continue;
                }

                if (hasWinningLine(column, row, 1, 0)       // Horizontal
                        || hasWinningLine(column, row, 0, 1)  // Vertical
                        || hasWinningLine(column, row, 1, 1)  // Rising diagonal
                        || hasWinningLine(column, row, 1, -1)) { // Falling diagonal
                    return player;
                }
            }
        }
        return 0;
    }

    private boolean hasWinningLine(
            int column, int row, int columnStep, int rowStep) {
        int endColumn = column + (CONNECT_LENGTH - 1) * columnStep;
        int endRow = row + (CONNECT_LENGTH - 1) * rowStep;

        if (endColumn < 0 || endColumn >= COLUMN_COUNT
                || endRow < 0 || endRow >= ROW_COUNT) {
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

    public void updateBoard(int columnIndex){
        if (!playable() || !isLegalColumn(columnIndex)) {
            return;
        }
        for (int i = 0; i < ROW_COUNT; i++) {
            if (board[columnIndex][i] == 0) {
                board[columnIndex][i] = currentPlayer;
                FrameGenerator.columns()[columnIndex].incrementSize();
                currentPlayer *= -1;
                drawBoard();
                if (playable() && !hasLegalMove()) {
                    currentPlayer = 0;
                    JOptionPane.showMessageDialog(FrameGenerator.getFrame(), "Match Drawn");
                }


//                FrameGenerator.columns()[columnIndex].incrementSize();
                break;
            }
        }

//        print(board);
//        if(FrameGenerator.hasAI())
//            flipClick();
    }

    private void print(int[][] arr){
        for (int[] a:arr) {
            System.out.println(Arrays.toString(a));
        }
        System.out.println("------------------------------------------------");
    }

    public boolean playable(){
        return currentPlayer!=0;
    }

    public boolean clickable(){
        return acceptingHumanInput;
    }

    public void flipClick(){
        acceptingHumanInput = !acceptingHumanInput;
    }

    public int[][] clone(int[][] input){
        int[][] clone= new int[input.length][input[0].length];
        for (int i=0;i<input.length;i++){
            clone[i] = input[i].clone();
        }
        return clone;
    }

    public void playHumanMove(int columnIndex) {
        if (!playable() || !acceptingHumanInput || !isLegalColumn(columnIndex)) {
            return;
        }

        // Lock before drawing: a winner dialog runs a nested Swing event loop.
        acceptingHumanInput = false;
        updateBoard(columnIndex);
        if (ai != null && playable()) {
            requestAiMove();
        } else {
            acceptingHumanInput = true;
        }
    }

    private void requestAiMove() {
        int[][] snapshot = clone(board);
        new SwingWorker<Integer, Void>() {
            @Override
            protected Integer doInBackground() throws Exception {
                return ai.play(snapshot);
            }

            @Override
            protected void done() {
                try {
                    int column = get();
                    if (!isLegalColumn(column)) {
                        throw new IllegalStateException("AI selected an illegal column: " + column);
                    }
                    updateBoard(column);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    currentPlayer = 0;
                } catch (java.util.concurrent.ExecutionException | RuntimeException e) {
                    currentPlayer = 0;
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(FrameGenerator.getFrame(),
                            "The AI could not choose a move. Please restart the game.");
                } finally {
                    acceptingHumanInput = true;
                }
            }
        }.execute();
    }

    private boolean isLegalColumn(int columnIndex) {
        return columnIndex >= 0 && columnIndex < board.length && !isColumnFull(columnIndex);
    }

    private boolean isColumnFull(int columnIndex) {
        return board[columnIndex][board[columnIndex].length - 1] != 0;
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
