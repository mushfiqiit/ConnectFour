package Game;

import java.util.ArrayList;
import java.util.Arrays;

public class Node {
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
            eval = evaluationFunction() * (remainingDepth + 1) / 2;
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




    private int evaluationFunction(){
        int total =0;
        for(int i=0;i<7;i++){
            for (int j=0;j<6;j++){
                //check horizontal
                if(i<4){
                    boolean possible = true;
                    int count =0;
                    int standard = state[i][j];
                    for(int k=i; k<i+4; k++){
                        if(state[k][j]==standard){
                            count++;
                        }
                        else {
                            if (standard != 0) {
                                if (state[k][j] == standard * -1) {
                                    count = 0;
                                    possible = false;
                                    break;
                                }
                            }
                            //interruption
                        }

                    }
                    if(possible){
                        if(count ==3)
                            total+= standard*10000;
                        else if(count ==2)
                            total+=standard*100;
                        else
                            total += standard*count;
                    }
                }

                //check vertical
                if(j<3){
                    boolean possible = true;
                    int count =0;
                    int standard = state[i][j];
                    for(int k=j; k<j+4; k++){
                        if(state[i][k]==standard){
                            count++;
                        }
                        //interruption
                        else if(state[i][k]==standard*-1){
                            count = 0;
                            possible = false;
                            break;
                        }
                    }
                    if(possible){
                        if(count ==3)
                            total+= standard*10000;
                        else if(count ==2)
                            total+=standard*100;
                        else
                            total += standard*count;
                    }
                }

                //check diagonal /
                if(i<4 &&j<3){
                    boolean possible = true;
                    int count =0;
                    int standard = state[i][j];
                    for(int k=0; k<4; k++){
                        if(state[i+k][j+k]==standard){
                            count++;
                        }
                        //interruption
                        else if(state[i+k][j+k]==standard*-1){
                            count = 0;
                            possible = false;
                            break;
                        }
                    }
                    if(possible){
                        if(count ==3)
                            total+= standard*10000;
                        else if(count ==2)
                            total+=standard*100;
                        else
                            total += standard*count;
                    }
                }

                //check diagonal \
                if(i>2 &&j<3){
                    boolean possible = true;
                    int count =0;
                    int standard = state[i][j];
                    for(int k=0; k<4; k++){
                        if(state[i-k][j+k]==standard){
                            count++;
                        }
                        //interruption
                        else if(state[i-k][j+k]==standard*-1){
                            count = 0;
                            possible = false;
                            break;
                        }
                    }
                    if(possible){
                        if(count ==3)
                            total+= standard*100;
                        else if(count ==2)
                            total+=standard*10;
                        else
                            total += standard*count;
                    }
                }

            }
        }

        return total;
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
