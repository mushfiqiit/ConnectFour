package Game;

import java.util.ArrayList;
import java.util.Arrays;

public class Node {
    private static final int[][] CELL_WEIGHTS = {
            {3, 4, 5, 5, 4, 3},
            {4, 6, 8, 8, 6, 4},
            {5, 8, 11, 11, 8, 5},
            {7, 10, 13, 13, 10, 7},
            {5, 8, 11, 11, 8, 5},
            {4, 6, 8, 8, 6, 4},
            {3, 4, 5, 5, 4, 3}
    };

    private static final int CONNECT_LENGTH = 4;
    private static final int THREE_PIECE_SCORE = 10000;
    private static final int TWO_PIECE_SCORE = 100;
    private static final int FALLING_DIAGONAL_THREE_PIECE_SCORE = 100;
    private static final int FALLING_DIAGONAL_TWO_PIECE_SCORE = 10;

    private final int moveColumn;

    Node parent;
    ArrayList<Node> children = new ArrayList<>();
    int[][] state;
    boolean terminalNode = true;
    int eval;
    NodeType type;
    int alphaBeta;
    int tempParentAlphaBeta;
    int remainingDepth;

    public Node(int[][] inputState, NodeType nodeType, int remainingDepth) {
        this(inputState, nodeType, null, remainingDepth, -1);
    }

    private Node(
            int[][] inputState, NodeType nodeType,
            Node parent, int remainingDepth, int moveColumn) {
        this.moveColumn = moveColumn;
        this.state = inputState;
        this.type = nodeType;
        this.parent = parent;
        this.remainingDepth = remainingDepth;
        MiniMaxAI.nodeCount++;

        initializeSearchBounds();

        int winner = checkWinner();
        boolean boardFull = isBoardFull();
        terminalNode = winner != 0 || boardFull || remainingDepth == 0;

        if (terminalNode) {
            evaluateTerminalPosition(winner, boardFull);
            propagateTerminalEvaluation();
        } else {
            expandChildren();
            eval = evaluateChildren();
            tempParentAlphaBeta = eval;
            updateParentBound();
        }
    }

    private void initializeSearchBounds() {
        alphaBeta = type == NodeType.MAX
                ? Integer.MIN_VALUE
                : Integer.MAX_VALUE;
        tempParentAlphaBeta = parent != null
                ? parent.alphaBeta
                : Integer.MAX_VALUE;
    }

    private boolean isBoardFull() {
        for (int[] column : state) {
            for (int cell : column) {
                if (cell == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    private void evaluateTerminalPosition(int winner, boolean boardFull) {
        if (winner != 0 || boardFull) {
            eval = winner * 20000 * (remainingDepth + 1) / 2;
        } else {
            eval = evaluatePosition() * (remainingDepth + 1) / 2;
        }
    }

    private void propagateTerminalEvaluation() {
        alphaBeta = eval;
        if (updateBoundAndCheckContinuation()) {
            updateParentBound();
        }
    }

    private void expandChildren() {
        int player = type == NodeType.MAX ? 1 : -1;
        NodeType childType = type == NodeType.MAX ? NodeType.MIN : NodeType.MAX;

        for (int column = 0; column < state.length; column++) {
            int emptyRow = findFirstEmptyRow(column);
            if (emptyRow == -1) {
                continue;
            }

            int[][] childState = Game.getInstance().clone(state);
            childState[column][emptyRow] = player;
            // Constructing a child evaluates its subtree and updates search bounds.
            Node child = new Node(childState, childType, this, remainingDepth - 1, column);
            children.add(child);

            if (parent != null && parent.parent != null && !updateBoundAndCheckContinuation()) {
                break;
            }
        }
    }

    private int findFirstEmptyRow(int column) {
        for (int row = 0; row < state[column].length; row++) {
            if (state[column][row] == 0) {
                return row;
            }
        }
        return -1;
    }

    private int checkWinner() {
        return BoardRules.checkWinner(state);
    }

    private int evaluateChildren() {
        boolean maximizing = type == NodeType.MAX;
        int bestScore = maximizing ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        for (Node child : children) {
            bestScore = maximizing
                    ? Math.max(bestScore, child.eval)
                    : Math.min(bestScore, child.eval);
        }
        return bestScore;
    }

    private int evaluatePosition() {
        int totalScore = 0;
        for (int column = 0; column < state.length; column++) {
            for (int row = 0; row < state[column].length; row++) {
                totalScore += scoreWindow(column, row, 1, 0); // Horizontal
                totalScore += scoreWindow(column, row, 0, 1); // Vertical
                totalScore += scoreWindow(column, row, 1, 1); // Rising diagonal
                // Preserve the original orientation and lower weights for this diagonal.
                totalScore += scoreWindow(column, row, -1, 1,
                        FALLING_DIAGONAL_THREE_PIECE_SCORE,
                        FALLING_DIAGONAL_TWO_PIECE_SCORE);
            }
        }
        return totalScore;
    }

    private int scoreWindow(int column, int row, int columnStep, int rowStep) {
        return scoreWindow(column, row, columnStep, rowStep,
                THREE_PIECE_SCORE, TWO_PIECE_SCORE);
    }

    private int scoreWindow(
            int column, int row, int columnStep, int rowStep,
            int threePieceScore, int twoPieceScore) {
        int endColumn = column + (CONNECT_LENGTH - 1) * columnStep;
        int endRow = row + (CONNECT_LENGTH - 1) * rowStep;
        if (endColumn < 0 || endColumn >= state.length
                || endRow < 0 || endRow >= state[0].length) {
            return 0;
        }

        // The existing heuristic only scores windows whose starting cell is occupied.
        int player = state[column][row];
        if (player == 0) {
            return 0;
        }

        int matchingPieces = 0;
        for (int offset = 0; offset < CONNECT_LENGTH; offset++) {
            int nextColumn = column + offset * columnStep;
            int nextRow = row + offset * rowStep;
            int cell = state[nextColumn][nextRow];
            if (cell == -player) {
                return 0;
            }
            if (cell == player) {
                matchingPieces++;
            }
        }

        if (matchingPieces == 3) {
            return player * threePieceScore;
        }
        if (matchingPieces == 2) {
            return player * twoPieceScore;
        }
        return player * matchingPieces;
    }

    private int evaluatePositionByCellWeights() {
        int totalScore = 0;

        for (int column = 0; column < state.length; column++) {
            for (int row = 0; row < state[column].length; row++) {
                int player = state[column][row];
                int weight = CELL_WEIGHTS[column][row];
                totalScore += player * weight;
            }
        }

        return totalScore;
    }

    /** Returns the column that produced this node, or -1 for the root. */
    public int getMoveColumn() {
        return moveColumn;
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        appendTree(output, 0);
        return output.toString();
    }

    private void appendTree(StringBuilder output, int indentationLevel) {
        StringBuilder indentation = new StringBuilder();
        for (int level = 0; level < indentationLevel; level++) {
            indentation.append('\t');
        }

        output.append(indentation).append("{\n");
        for (int[] column : state) {
            output.append(indentation).append("    ")
                    .append(Arrays.toString(column)).append('\n');
        }
        output.append(indentation).append("    e:").append(eval).append('\n');
        for (Node child : children) {
            child.appendTree(output, indentationLevel + 1);
        }
        output.append(indentation).append("}\n");
    }

    /**
     * Updates the inherited bound when this node's bound permits continuation.
     * Returns false when the caller should stop expanding children.
     */
    private boolean updateBoundAndCheckContinuation() {
        boolean canContinue = type == NodeType.MAX
                ? alphaBeta <= tempParentAlphaBeta
                : alphaBeta >= tempParentAlphaBeta;
        if (canContinue) {
            tempParentAlphaBeta = alphaBeta;
        }
        return canContinue;
    }

    private void updateParentBound() {
        if (parent == null) {
            return;
        }
        if (type == NodeType.MAX) {
            parent.alphaBeta = Math.min(parent.alphaBeta, tempParentAlphaBeta);
        } else {
            parent.alphaBeta = Math.max(parent.alphaBeta, tempParentAlphaBeta);
        }
    }
}
