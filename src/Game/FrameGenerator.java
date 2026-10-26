package Game;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.GridLayout;

public class FrameGenerator {
    private static final int COLUMN_COUNT = 7;
    private static final int ROW_COUNT = 6;
    private static final int CELL_SIZE = 100;
    private static final int BOARD_WIDTH = COLUMN_COUNT * CELL_SIZE;
    private static final int BOARD_HEIGHT = ROW_COUNT * CELL_SIZE;

    private static final Column[] columns = new Column[COLUMN_COUNT];
    private static AI ai;
    private static JFrame frame;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(FrameGenerator::start);
    }

    private static void start() {
        initializeColumns();
        ai = selectOpponent();
        startGame();
        showFrame();
    }

    private static void initializeColumns() {
        for (int column = 0; column < columns.length; column++) {
            columns[column] = new Column(column);
        }
    }

    private static AI selectOpponent() {
        int selectedMode = JOptionPane.showOptionDialog(
                null,
                "Game Mode",
                "Connect Four",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Single Player", "Multi Player"},
                "Multi Player");

        // Preserve multiplayer as the fallback when the mode dialog is closed.
        if (selectedMode != 0) {
            return null;
        }
        return selectDifficulty();
    }

    private static AI selectDifficulty() {
        int selectedDifficulty = JOptionPane.showOptionDialog(
                null,
                "Difficulty",
                "Connect Four",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                new Object[]{"Hard", "Easy"},
                "Easy");

        // Preserve hard difficulty as the fallback when this dialog is closed.
        return selectedDifficulty == 1 ? new RandomAI() : new MiniMaxAI();
    }

    private static void startGame() {
        if (ai == null) {
            Game.startGame();
            return;
        }

        try {
            Game.startGame(ai);
        } catch (Exception e) {
            throw new IllegalStateException("Could not start game", e);
        }
    }

    private static JPanel createBoardPanel() {
        JPanel boardPanel = new JPanel(new GridLayout(1, COLUMN_COUNT));
        boardPanel.setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        for (Column column : columns) {
            boardPanel.add(createColumnPanel(column));
        }
        return boardPanel;
    }

    private static JPanel createColumnPanel(Column column) {
        // Column creates its labels with positions measured from the board's bottom.
        JPanel columnPanel = new JPanel(null);
        for (JLabel label : column.getLabels()) {
            columnPanel.add(label);
        }
        return columnPanel;
    }

    private static void showFrame() {
        frame = new JFrame("Connect Four");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(createBoardPanel());
        frame.pack();
        frame.setLocation(300, 50);
        frame.setVisible(true);
    }

    public static Column[] columns() {
        return columns;
    }

    public static boolean hasAI() {
        return ai != null;
    }

    public static JFrame getFrame() {
        return frame;
    }
}
