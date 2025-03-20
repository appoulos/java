import java.awt.*;

public class DiagonalEnemy extends VerticalEnemy {

    private int screenWidth;
    private int xSpeed;
    
    public DiagonalEnemy(int x, int y, int w, int h, int sH, int yS, int sW, int xS) {
        super(x, y, h, h, sH, yS);
        
        screenWidth = sW;
        xSpeed = xS;
    }
    
    public void move() {
        
        Rectangle rect = getRectangle();
        rect.x -= xSpeed;
        super.move();
        if (rect.x + rect.width >= screenWidth || rect.x <= 0) {
            xSpeed *= -1;
        }
    }
    
    public Color getColor() {
        return Color.ORANGE;
    }
}

