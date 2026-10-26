package Game;

import java.util.ArrayList;
import java.util.Arrays;

public class Node {
    private static final int CONNECT_LENGTH = 4;
    private static final int THREE_PIECE_SCORE = 10000;
    private static final int TWO_PIECE_SCORE = 100;
    private static final int FALLING_DIAGONAL_THREE_PIECE_SCORE = 100;
    private static final int FALLING_DIAGONAL_TWO_PIECE_SCORE = 10;

    Node parent;
    ArrayList<Node> children = new ArrayList<>();
    int[][] state;
    boolean terminalNode = true;
    int eval;
    NodeType type;
    int alphaBeta;
    int tempParentAlphaBeta;
    int remainingDepth;

    public Node(
            int[][] inputState, NodeType nodeType,
            Node parent, int remainingDepth) {
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
            eval = evaluateBasedOnChild();
            tempParentAlphaBeta = eval;
            setParent();
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
        if (pushUpwards()) {
            setParent();
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
            Node child = new Node(childState, childType, this, remainingDepth - 1);
            children.add(child);

            if (parent != null && parent.parent != null && !pushUpwards()) {
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

    private int evaluateBasedOnChild(){
        if(type == NodeType.MAX)
            return max();
        else return min();
    }

    private int max(){
        int childEval= Integer.MIN_VALUE;
        for(Node n: children){
            childEval = Math.max(n.eval, childEval);
        }
        return childEval;
    }

    private int min(){
        int childEval= Integer.MAX_VALUE;
        for(Node n: children){
            childEval = Math.min(n.eval,childEval);
        }
        return childEval;
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

    public int evaluationFunction2() {
        final int[][] evaluationTable = {
                {3, 4,  5,  5,  4,  3},
                {4, 6,  8,  8,  6,  4},
                {5, 8,  11, 11, 8,  5},
                {7, 10, 13, 13, 10, 7},
                {5, 8,  11, 11, 8,  5},
                {4, 6,  8,  8,  6,  4},
                {3, 4,  5,  5,  4,  3}
        };

        int center = 138;
        int sum = 0;
        for (int i = 0; i < state.length; i++)
            for (int j = 0; j <state[0].length; j++)
                if (state[i][j] == 1)
                    sum += evaluationTable[i][j];
                else if (state[i][j] == -1)
                    sum -= evaluationTable[i][j];
        return sum;
    }
//
//    private int evaluationFunction3() {
//        int i,j;
//        for (i=0; i<7; i++){
//            for(j=0; j<6; j++){
//                if(state[i][j]==0) break;
//            }
//
//
//        }
//        return 0;
//    }




    public int getDifference(Node child) throws Exception {
        for(int i=0; i<7; i++){
            for (int j=0; j<6; j++){
                if (child.state[i][j] != state[i][j]) {
                    if(child.state[i][j]==1 && state[i][j]==0)
                        return i;
                    else if(child.state[i][j]==-1 && state[i][j]==0)
                        throw new Exception("ai -1 daan dise kemne jani");
                    else if(state[i][j]!=0)
                        throw new Exception("ai ager daan nai koira dese kemne?");
                    else throw new Exception("ki hoiche jani na. but check koira dekh");
                }
            }
        }
        throw new Exception("akam chodaiso");
    }

    private void print(int[][] arr){
        for (int[] a:arr) {
            System.out.println(Arrays.toString(a));
        }
        System.out.println("------------------------------------------------");
    }

    public String toString(){
        String ret="";
        String tabs = "";
        for (int i=7-remainingDepth;i>=0;i--) tabs+="\t";
        ret+=tabs+"{\n";
        for (int[] a:state) {
            ret+=tabs+"    "+Arrays.toString(a)+"\n";
        }
        ret+=tabs+"    e:" +eval+"\n";
        for (Node c:children)
            ret+=c;

        ret+=tabs+"}"+"\n";
//        System.out.println(ret);
        return ret;
    }



    /***************************ab******************************/
    private boolean pushUpwards(){
        if(type == NodeType.MAX){
            if(alphaBeta<= tempParentAlphaBeta){
                tempParentAlphaBeta = alphaBeta;
                return true;
            }
            else return false;
        }
        else {
            if(alphaBeta>=tempParentAlphaBeta){
                tempParentAlphaBeta = alphaBeta;
                return true;
            }
            else return false;
        }
    }

    private void setParent(){
        if(parent != null){
            if(type == NodeType.MAX){
                if(tempParentAlphaBeta<=parent.alphaBeta){
                    parent.alphaBeta = tempParentAlphaBeta;
                }
            } else {
                if(tempParentAlphaBeta >= parent.alphaBeta){
                    parent.alphaBeta = tempParentAlphaBeta;
                }
            }
        }
    }
    /*************************************************************/
}
