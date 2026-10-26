package Game;

public class MiniMaxAI extends AI{
    public static int nodeCount =0;

    @Override
    int play(int[][] boardState) throws Exception {
        nodeCount = 0;
        Node root = new Node(Game.getInstance().clone(boardState),NodeType.MAX, 10);

        Node bestChild = null;
        for (Node c: root.children){
            if(root.eval== c.eval){
                bestChild=c;
                break;
            }
        }
        int way;
        if(bestChild==null)
            throw new Exception("child pay na");
        else way = bestChild.getMoveColumn();


        System.out.println(nodeCount+" node created\n");

        if(way<7 && way>=0){
//            System.out.println(way);
//            FileWriter wr = new FileWriter(new File("output.txt"));
//            wr.write(root.toString());
//            wr.close();
//            System.out.println(root);


//            Thread.sleep(1000);
            return way;
        }
        else throw new Exception("AI baire dite chay");
    }


}
