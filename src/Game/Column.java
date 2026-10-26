package Game;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Column {
    private static final int ROW_COUNT = 6;
    private static final int CELL_SIZE = 100;

    private static final ImageIcon RED_ICON = new ImageIcon("red.png");
    private static final ImageIcon YELLOW_ICON = new ImageIcon("yellow.png");
    private static final ImageIcon EMPTY_ICON = new ImageIcon("empty.png");

    private final int columnIndex;
    private final JLabel[] labels = new JLabel[ROW_COUNT];
    private int size = 0;

    public Column(int columnIndex) {
        this.columnIndex = columnIndex;

        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                requestHumanMove();
            }
        };

        for (int row = 0; row < ROW_COUNT; row++) {
            JLabel label = new JLabel(EMPTY_ICON);
            // Board rows start at the bottom; screen coordinates start at the top.
            int screenY = (ROW_COUNT - 1 - row) * CELL_SIZE;
            label.setBounds(0, screenY, CELL_SIZE, CELL_SIZE);
            label.addMouseListener(clickListener);
            labels[row] = label;
        }
    }

    public JLabel[] getLabels() {
        return labels;
    }

    private void requestHumanMove() {
        Game.getInstance().playHumanMove(columnIndex);
    }

    public void drawColumn(int[] columnState) {
        for (int row = 0; row < columnState.length; row++) {
            int player = columnState[row];
            if (player == 0) {
                labels[row].setIcon(EMPTY_ICON);
            } else if (player == 1) {
                labels[row].setIcon(RED_ICON);
            } else if (player == -1) {
                labels[row].setIcon(YELLOW_ICON);
            }
        }
    }

    public int size() {
        return size;
    }

    public void incrementSize() {
        size++;
    }
}
