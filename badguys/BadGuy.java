import java.awt.*;

public abstract class BadGuy {
    private Rectangle rect;

    public BadGuy(int x, int y, int w, int h) {
        rect = new Rectangle(x, y, w, h);
    }

    public Rectangle getRectangle() {
        return rect;
    }

    public void setRectangle(Rectangle rect) {
        this.rect = rect;
    }

    public boolean intersects(Rectangle p) {
        return rect.intersects(p);
    }

    public void draw(Graphics g) {
        g.setColor(getColor());
        g.fillRect(rect.x, rect.y, rect.width, rect.height);
    }

    public abstract Color getColor();

    public abstract void move();
}
