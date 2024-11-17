import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.WeakHashMap;

public class GameTetris extends JFrame {
    public static void main(String[] args) {
        new GameTetris().go();
    }

    final String TITLE_OF_PROGRAM="Tetris";
    final int BLOCK_SIZE=25;
    final int ARC_RADIUS=6;
    final int FIELD_WIDTH=10;
    final int FIELD_HEIGHT=18;
    final int START_LOCATION=180;
    final int FIELD_DX=7;
    final int FIELD_DY=26;
    final int LEFT=37;
    final int UP=38;
    final int RIGHT=39;
    final int DOWN=40;
    final int SHOW_DELAY=350;
    final int[][][] SHAPES = {
            {{0,0,0,0}, {1,1,1,1}, {0,0,0,0}, {0,0,0,0}, {4, 0x00f0f0}}, // I
            {{0,0,0,0}, {0,1,1,0}, {0,1,1,0}, {0,0,0,0}, {4, 0xf0f000}}, // O
            {{1,0,0,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0}, {3, 0x0000f0}}, // J
            {{0,0,1,0}, {1,1,1,0}, {0,0,0,0}, {0,0,0,0}, {3, 0xf0a000}}, // L
            {{0,1,1,0}, {1,1,0,0}, {0,0,0,0}, {0,0,0,0}, {3, 0x00f000}}, // S
            {{1,1,1,0}, {0,1,0,0}, {0,0,0,0}, {0,0,0,0}, {3, 0xa000f0}}, // T
            {{1,1,0,0}, {0,1,1,0}, {0,0,0,0}, {0,0,0,0}, {3, 0xf00000}}  // Z
    };
    final int[] SCORES = {100, 300, 700, 1500};
    int gameScores=0;
    int [][] mine=new int[FIELD_HEIGHT][FIELD_WIDTH];
    JFrame frame;
    Canvas canvasPanel=new Canvas();
    Random random=new Random();
    Figure figure=new Figure();
    boolean gameOver=false;
    final int[][] GAME_OVER_MSG = {
            {0,1,1,0,0,0,1,1,0,0,0,1,0,1,0,0,0,1,1,0},
            {1,0,0,0,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,1},
            {1,0,1,1,0,1,1,1,1,0,1,0,1,0,1,0,1,1,1,1},
            {1,0,0,1,0,1,0,0,1,0,1,0,1,0,1,0,1,0,0,0},
            {0,1,1,0,0,1,0,0,1,0,1,0,1,0,1,0,0,1,1,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
            {0,1,1,0,0,1,0,0,1,0,0,1,1,0,0,1,1,1,0,0},
            {1,0,0,1,0,1,0,0,1,0,1,0,0,1,0,1,0,0,1,0},
            {1,0,0,1,0,1,0,1,0,0,1,1,1,1,0,1,1,1,0,0},
            {1,0,0,1,0,1,1,0,0,0,1,0,0,0,0,1,0,0,1,0},
            {0,1,1,0,0,1,0,0,0,0,0,1,1,0,0,1,0,0,1,0}};

    void go()
    {
        frame=new JFrame(TITLE_OF_PROGRAM);
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        frame.setSize(FIELD_WIDTH*BLOCK_SIZE+FIELD_DX, FIELD_HEIGHT*BLOCK_SIZE+FIELD_DY);
        frame.setLocation(START_LOCATION,START_LOCATION);
        frame.setResizable(false);
        canvasPanel.setBackground(Color.BLACK);
        frame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {

            }

            @Override
            public void keyPressed(KeyEvent e) {
                if(!gameOver)
                {
                    if (e.getKeyCode()==DOWN) figure.drop();
                    if (e.getKeyCode()==UP)   figure.rotate();
                    if (e.getKeyCode()==LEFT || e.getKeyCode()==RIGHT) figure.move(e.getKeyCode());
                }
                canvasPanel.repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        frame.getContentPane().add(BorderLayout.CENTER, canvasPanel);
        frame.setVisible(true);

        Arrays.fill(mine[FIELD_HEIGHT-1],1);

        while (!gameOver)
        {
            try{
                Thread.sleep(SHOW_DELAY);
            }catch (Exception e)
            {
                e.printStackTrace();
            }
            canvasPanel.repaint();
            if(figure.isTouchGround())
            {
                figure.leaveOnTheGround();
                checkFilling();
                figure=new Figure();
                gameOver=figure.isCrossGround();
            }else {
                figure.stepDown();
            }
        }
    }

    class Block{
        private int x,y;

        public Block(int x, int y)
        {
            setX(x);
            setY(y);
        }

        private void setY(int y) {
            this.y=y;
        }

        private void setX(int x) {
            this.x=x;
        }

        int getX()
        {
            return x;
        }

        int getY()
        {
            return y;
        }

        void paint(Graphics g, int color)
        {
            g.setColor(new Color(color));
            g.drawRoundRect(x*BLOCK_SIZE+1,y*BLOCK_SIZE+1, BLOCK_SIZE-2,BLOCK_SIZE-2, ARC_RADIUS, ARC_RADIUS);
        }

    }

    void checkFilling()
    {

    }

    class Figure{

        private ArrayList<Block> figure=new ArrayList<Block>();
        private int[][] shape=new int[4][4];
        private int type, size, color;
        private int x=3, y=0;

        Figure()
        {
            type=random.nextInt(SHAPES.length);
            size=SHAPES[type][4][0];
            color=SHAPES[type][4][1];
            if (size==4){
                y-=1;
            }
            for (int i = 0; i <size ; i++) {
                System.arraycopy(SHAPES[type][i],0,shape[i], 0, SHAPES[type][i].length);
                createFromShape();
            }
        }

        void createFromShape()
        {
            for(int x=0;x<size;x++)
            {
                for (int y=0;y<size;y++)
                {
                    if (shape[x][y]==1)
                    {
                        figure.add(new Block(x+this.x,y+this.y));
                    }
                }
            }
        }

        void drop()
        {

        }

        void rotate()
        {

        }

        void move(int direction)
        {

        }

        boolean isTouchGround()
        {
            for (Block block:figure)
            {
                if (mine[block.getY()][block.getX()]>0)
                {
                    return true;
                }
            }
                return false;
        }

        void leaveOnTheGround()
        {
            for (Block block:figure)
            {
                mine[block.getY()][block.getX()]=color;
            }
        }

        boolean isCrossGround()
        {
            return false;
        }

        void stepDown()
        {
            for (Block block:figure)
            {
                block.setY(block.getY()+1);
                y++;
            }
        }

        void paint(Graphics g)
        {
            for (Block block:figure)
            {
                block.paint(g,color);
            }
        }

    }

    public class Canvas extends JPanel{
        @Override
        public void paint(Graphics g)
        {
            super.paint(g);
            for (int x=0; x<FIELD_WIDTH;x++)
            {
                for (int y = 0; y <FIELD_HEIGHT ; y++) {
                    if(mine[y][x]>0)
                    {
                        g.setColor(new Color(mine[y][x]));
                        g.fill3DRect(x*BLOCK_SIZE+1,y*BLOCK_SIZE+1,BLOCK_SIZE-1, BLOCK_SIZE-1,true);
                    }
                }
            }
            figure.paint(g);
        }
    }
}
