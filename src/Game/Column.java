package Game;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class Column {
    private static final ImageIcon red = new ImageIcon("red.png");
    private static final ImageIcon yellow = new ImageIcon("yellow.png");
    private static final ImageIcon empty = new ImageIcon("empty.png");

    final int id;
    private int size = 0;
    private final JLabel[] labels = new JLabel[6];

    public Column(int id) {
        this.id = id;
        for (int j=0;j<6;j++){
            JLabel l = new JLabel(empty);
            l.setBounds(0,500-j*100,100,100);
            l.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    clicked();
                }
            });
            labels[j] = l;
        }
    }

    public JLabel[] getLabels(){
        return labels;
    }

    public void aiClick(){
        Game.getInstance().updateBoard(id);
    }

    private void clicked(){
        Game.getInstance().playHumanMove(id);
    }

    public void drawColumn(int[] status) {
        for (int i=0; i<status.length; i++){
            if(status[i]==0) labels[i].setIcon(empty);
            else if(status[i]==1) labels[i].setIcon(red);
            else if(status[i]==-1) labels[i].setIcon(yellow);
        }
    }

    public int size(){
        return size;
    }

    public void incrementSize(){
        size++;
    }
}
