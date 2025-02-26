import java.awt.Color;
import java.awt.Graphics;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

class AndreyLuzin {
 
    public static void main(String[] args) {
        // TODO code application logic here
//        NewClass ne = new NewClass();
//        ne.go();
        SwingUtilities.invokeLater(() -> new NewClass().go());
    }
 
}
 
class NewClass extends JPanel
    implements Runnable {
 
    int x = 0;
    int y = 0;
     
    int test1Count, test2Count;
 
    public void go() {
 
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        frame.getContentPane().add(this);
        frame.setSize(300, 300);
        frame.setVisible(true);
        Thread thr = new Thread(this);
        thr.start();
    }
     
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawString("Im paintComponenet; is this the EDT? : " + SwingUtilities.isEventDispatchThread(), 5, 100);
        g.setColor(Color.green);
        g.fillOval(x, y, 40, 40);
        g.setColor(Color.RED);
        g.drawString("" + x, 5, 130);
    }
 
    public void redraw() {
        x++;
        y++;
        test1Count++;
        System.out.println("test1 " + test1Count + " is this on the EDT? :" + SwingUtilities.isEventDispatchThread());
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        }
        catch (InterruptedException e) {
            System.out.println("Interrupted");
        }
        repaint();
    }
 
    @Override
    public void run() {
        while (x != 120) {
            test2Count++;
            System.out.println("test2 " + test2Count);
//            repaint();
            redraw();
        }
    }
}
