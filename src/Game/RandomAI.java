package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAI extends AI {
    private static final int EMPTY = 0;

    @Override
    int play(int[][] boardState) {
        List<Integer> legalColumns = findLegalColumns(boardState);
        if (legalColumns.isEmpty()) {
            throw new IllegalStateException("No legal moves remain");
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(legalColumns.size());
        return legalColumns.get(randomIndex);
    }

    private List<Integer> findLegalColumns(int[][] boardState) {
        List<Integer> legalColumns = new ArrayList<>();
        for (int column = 0; column < boardState.length; column++) {
            int topRow = boardState[column].length - 1;
            if (boardState[column][topRow] == EMPTY) {
                legalColumns.add(column);
            }
        }
        return legalColumns;
    }
}
