import java.awt.*;
import java.awt.Color;

class Block {
	Point point;
	boolean alive;
	Color color;
	int hits;

	Block(Point p, Color c, int h) {
		alive = true;
		point = p;
		color = c;
		hits = h;
	}
}
