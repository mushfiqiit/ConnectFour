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
    int level;

    public Node(int[][] inputState, NodeType nt, Node parent, int level) {
        this.state = inputState;
        this.type = nt;
        MiniMaxAI.nodeCount++;
        this.parent = parent;
        this.level= level;

        /*********************ab************************/
        if(type.equals(NodeType.MAX))
            alphaBeta = Integer.MIN_VALUE;
        else alphaBeta = Integer.MAX_VALUE;

        if(parent!=null)
            tempParentAlphaBeta = this.parent.alphaBeta;
        else tempParentAlphaBeta = Integer.MAX_VALUE;
        /***************************************************/

        for(int i=0; i<7;i++){
            for(int j=0; j<6; j++){
                if(state[i][j]==0){
                    terminalNode=false;
                    break;
                }
            }
            if(!terminalNode)
                break;
        }
        if(terminalNode) {
            eval = checkWinner()*20000*(level+1)/2;
        }
        else{
            if(checkWinner()!=0){
                terminalNode = true;
                eval = checkWinner()*20000*(level+1)/2;
            }
            else if(level==0){
                terminalNode = true;
                eval = evaluationFunction()*(level+1)/2;
//                eval = evaluationFunction2();
//                eval = evaluationFunction3();
            }
            else {
                for(int i=0; i<7; i++){
                    boolean canBeFilled = false;
                    for (int j=0;j<6;j++){
                        if(state[i][j]==0) {
                            canBeFilled = true;
                            break;
                        }
                    }
                    if(canBeFilled){
                        int[][] temp = Game.getInstance().clone(state);
                        int replace= type.equals(NodeType.MAX) ? 1 : -1;
                        for (int j = 0; j < 6; j++) {
                            if (temp[i][j] == 0) {
                                temp[i][j] = replace;
                                break;
                            }
                        }

                        NodeType newType = type.equals(NodeType.MAX)? NodeType.MIN:NodeType.MAX;
                        Node n = new Node(temp,newType,this, level-1);
                        children.add(n);

                        /**************ab****************/
                        if(parent!=null)
                            if(parent.parent!=null)
                                if (!pushUpwards())
                                    break;

                        /***************end******************/
                    }
                }
                eval= evaluateBasedOnChild();
                /******************ab*****************/
                tempParentAlphaBeta = eval;
                setParent();
                /*********************************/
            }
        }

        /*******************ab*******************/
        if(terminalNode){
            alphaBeta = eval;
            if(pushUpwards()){
                setParent();
            }
        }
        /***********************************/
    }




    private int checkWinner() {
        return BoardRules.checkWinner(state);
    }

    private int evaluateBasedOnChild(){
        if(type.equals(NodeType.MAX))
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
        for (int i=7-level;i>=0;i--) tabs+="\t";
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
        if(type.equals(NodeType.MAX)){
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
            if(type.equals(NodeType.MAX)){
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
