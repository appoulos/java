import java.awt.*;

public class SpinningEnemy extends BadGuy {

	private int centerX, centerY;
	private int radius;
	private double angle;
	private static final int pad = 10;

	public SpinningEnemy(int cX, int cY, int w, int h, int r) {
		super(cX + r - w / 2, cY - h / 2, w, h);
		int y = (int) (Math.random() * (cX - 2 * r - pad) + r);
		int x = (int) (Math.random() * (cY - 2 * r - pad) + r);
		setRectangle(new Rectangle(x, y, w, h));

		centerX = x;
		centerY = y;
		radius = r;
		angle = 0;
	}

	public Color getColor() {
		return new Color(randomColorNum(), randomColorNum(), randomColorNum());
	}

	public int randomColorNum() {
		return (int) (Math.random() * 256);
	}

	public void move() {

		angle += 0.1;

		Rectangle rect = getRectangle();

		rect.x = (int) (centerX + radius * Math.cos(angle)) - rect.width / 2;
		rect.y = (int) (centerY + radius * Math.sin(angle)) - rect.height / 2;
	}

	public void draw(Graphics g) {

		super.draw(g);

		g.setColor(Color.BLACK);
		g.fillOval(centerX - 5, centerY - 5, 10, 10);
	}
}
