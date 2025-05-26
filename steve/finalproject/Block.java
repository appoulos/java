import java.awt.Color;
import java.awt.Point;

/**
 * Block used in Breakout game.
 */
class Block {
	Point point;
	boolean alive;
	Color color;
	int hits;

	/**
	 * Set inital block class variables.
	 * 
	 * @param p location of block upper left corner.
	 * @param c color of block.
	 * @param h number of hits to destroy block.
	 */
	Block(Point p, Color c, int h) {
		alive = true;
		point = p;
		color = c;
		hits = h;
	}
}
