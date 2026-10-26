package Game;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomAI extends AI {
    @Override
    int play(int[][] boardStatus) {
        List<Integer> legalColumns = new ArrayList<>();
        for (int column = 0; column < boardStatus.length; column++) {
            if (boardStatus[column][boardStatus[column].length - 1] == 0) {
                legalColumns.add(column);
            }
        }
        if (legalColumns.isEmpty()) {
            throw new IllegalStateException("No legal moves remain");
        }
        return legalColumns.get(ThreadLocalRandom.current().nextInt(legalColumns.size()));
    }
}
