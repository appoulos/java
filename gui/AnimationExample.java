import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AnimationExample extends JFrame implements ActionListener {
    private int x = 0;
    private final int y = 50;
    private final int radius = 20;
    private final Timer timer;
    private final int delay = 9; // milliseconds

    public AnimationExample() {
        setTitle("Simple Animation");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        timer = new Timer(delay, this);
				// timer.isRunning();
        timer.start();

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.setColor(Color.RED);
                g.fillOval(x, y, radius * 2, radius * 2);
            }
        };
        add(panel);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        x += 2;
        if (x > getWidth()) {
            x = -radius * 2;
        }
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(AnimationExample::new);
    }
}
