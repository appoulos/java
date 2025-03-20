import java.awt.*;

public class StalkerEnemy extends BadGuy {
    
    private Rectangle rectPlayer;
    
    public StalkerEnemy(int x, int y, int w, int h, Rectangle p) {
        super(x, y, w, h);
        
        rectPlayer = p;
    }
    
    public void move() {
        
        Rectangle rect = getRectangle();
        
        if (rectPlayer.y > rect.y) {
            rect.y++;
        }
        if (rectPlayer.x > rect.x) {
            rect.x++;
        }
        if (rectPlayer.x < rect.x) {
            rect.x--;
        }
        if (rectPlayer.y < rect.y) {
            rect.y--;
        }
        
    }
    
    public Color getColor() {
        return Color.YELLOW;
    }
}


