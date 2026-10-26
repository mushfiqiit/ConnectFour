package Game;

public class MiniMaxAI extends AI {
    private static final int SEARCH_DEPTH = 10;

    public static int nodeCount = 0;

    @Override
    int play(int[][] boardState) {
        nodeCount = 0;
        int[][] searchState = Game.getInstance().clone(boardState);
        Node root = new Node(searchState, NodeType.MAX, SEARCH_DEPTH);
        Node bestChild = findBestChild(root);
        int column = bestChild.getMoveColumn();

        if (column < 0 || column >= boardState.length
                || boardState[column][boardState[column].length - 1] != 0) {
            throw new IllegalStateException("AI selected an illegal column: " + column);
        }

        System.out.println(nodeCount + " nodes created");
        return column;
    }

    private Node findBestChild(Node root) {
        for (Node child : root.children) {
            // Preserve the first matching move when several children share the best score.
            if (child.eval == root.eval) {
                return child;
            }
        }
        throw new IllegalStateException("No move matches the root's evaluation; the position may be terminal.");
    }
}
