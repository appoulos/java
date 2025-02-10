import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class SpriteAnimation extends JPanel implements ActionListener {

    private BufferedImage[] frames;
    private int currentFrame;
    private Timer timer;
    private int x, y;

    public SpriteAnimation() {
        loadFrames("path/to/your/sprite_sheet.png", 4, 1); // Assuming 4 frames in a row
        currentFrame = 0;
        timer = new Timer(100, this); // 100 ms delay between frames
        timer.start();
        x = 50;
        y = 50;
    }

    private void loadFrames(String filePath, int numFrames, int numRows) {
      try {
        BufferedImage spriteSheet = ImageIO.read(new File(filePath));
        int frameWidth = spriteSheet.getWidth() / numFrames;
        int frameHeight = spriteSheet.getHeight() / numRows;
        frames = new BufferedImage[numFrames];
        for (int i = 0; i < numFrames; i++) {
            frames[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        currentFrame = (currentFrame + 1) % frames.length;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(frames[currentFrame], x, y, null);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Sprite Animation");
        SpriteAnimation animation = new SpriteAnimation();
        frame.add(animation);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
