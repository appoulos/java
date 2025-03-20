import java.awt.*;

public class VerticalEnemy extends BadGuy {
    
    private int screenHeight;
    private int ySpeed;

    public VerticalEnemy(int x, int y, int w, int h, int sH, int yS) {
        super(x, y, w, h);
        
        screenHeight = sH;
        ySpeed = yS;
    }
    
    public void move() {
        
        Rectangle rect = getRectangle();
        rect.y -= ySpeed;
        if ((rect.y + rect.height) >= screenHeight || rect.y <= 0) {
            ySpeed *= -1;
        }
        
    } 
    
    public Color getColor() {
        return Color.RED;
    }
    
    public void changeSpeed(int speed) {
        if (ySpeed >= 0) {
            ySpeed += speed;
        } else {
            ySpeed -= speed;
        }
    }
    
    public static void delay(int m) {
        try {
            Thread.sleep(m);
        } catch (Exception e) {
        }
    }
}
