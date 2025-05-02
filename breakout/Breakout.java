import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

class Block {
	Point point;
	boolean alive;
	Color color;

	Block(Point p, Color c) {
		alive = true;
		point = p;
		color = c;
	}
}

public class Breakout extends JPanel implements ActionListener, KeyListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	private Rectangle ball = new Rectangle(); // a rectangle that represents the ball

	private int level = 1;
	private int highScore = 1;

	private boolean left, right; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer
	private boolean paused; // the update timer

	private final int dialogDelay = 1000;

	private int frameRate = 60; // roughly frame rate per second

	private final int velStartX = 1; // roughly frame rate per second
	private final int velStartY = 3; // roughly frame rate per second

	private Point velocity = new Point(); // velocity of ball
	private Point newBall = new Point(); // ball.x + velocity.x, ball.y + velocity.y);

	private final int size = 10; // ball size

	private final int blockRows = 4;
	private final int blockCols = 10;
	private final int blockWidth = 40;
	private final int blockHeight = 15;
	private final int padCol = 3; // size - 1;
	private final int padRow = 2; // size - 1;
	private final int padTop = 200;
	private final int padMiddle = 100;
	private final int padBottom = 20;
	private Block[][] blocks = new Block[blockRows][blockCols];
	private int blockCnt = blockRows * blockCols;
	private final boolean blockColNeighbors = size > padCol + 2; // (blockWidth + padCol) + 1;
	private final boolean blockRowNeighbors = size > padRow + 2; // (blockWidth + padCol) + 1;

	private final int ballStartX = 10;
	private final int ballStartY = 10; // padTop + blockRows * (blockHeight + padRow) + 10;

	private final int playerW = 50;
	private final int playerH = 10;

	// the width of the game area
	private final int gameWidth = padCol + blockCols * (blockWidth + padCol);
	// the height of the game area
	private final int gameHeight = padTop + blockRows * (blockHeight + padRow) + padMiddle + playerH + padBottom;

	private final int playerStartX = 10;
	private final int playerStartY = gameHeight - padBottom - playerH;

	private final int maxWidth = gameWidth - 1 - size;
	private final int maxHeight = gameHeight - 1 - size;

	private static JLabel dialogLabel;
	private static JFrame frame;
	private static JDialog dialog;

	// Sets up the basic GUI for the game
	public static void main(String[] args) {
		frame = new JFrame();

		dialog = new JDialog(frame, "Status");
		dialogLabel = new JLabel("");
		dialogLabel.setHorizontalAlignment(JLabel.CENTER);
		dialog.add(dialogLabel);
		dialog.setBounds(125, 125, 100, 70);
		dialog.setVisible(false);

		frame.setTitle("Obstacle Game");
		frame.setLayout(new BorderLayout());

		Breakout game = new Breakout();
		frame.add(game, BorderLayout.CENTER);

		game.addKeyListener(game);
		frame.addKeyListener(game);
		dialog.addKeyListener(game);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();

		game.setUpGame();
		// game.enterFullScreen();
	}

	// Constructor for the game panel
	public Breakout() {
		setPreferredSize(new Dimension(gameWidth, gameHeight));
	}

	// Method that is called by the timer 30 times per second (roughly)
	// Most games go through states - updating objects, then drawing them
	public void actionPerformed(ActionEvent e) {
		update();
		repaint();
	}

	// Called every time a key is pressed
	// Stores the down state for use in the update method
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_Q) {
			System.exit(0);
		} else if (e.getKeyCode() == KeyEvent.VK_R) {
			setUpGame();
		} else if (e.getKeyCode() == KeyEvent.VK_P) {
			paused = !paused;
		} else if (e.getKeyCode() == KeyEvent.VK_I) {
			if (frameRate > 2) {
				frameRate /= 2;
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (e.getKeyCode() == KeyEvent.VK_O) {
			frameRate *= 2;
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			paused = !paused;
		}
	}

	// Called every time a key is released
	// Stores the down state for use in the update method
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = false;
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			right = false;
		}
	}

	// Called every time a key is typed
	public void keyTyped(KeyEvent e) {
	}

	// Sets the initial state of the game
	// Could be modified to allow for multiple levels
	public void setUpGame() {
		if (size > blockWidth + 1) {
			System.out.println("ball size cannot exeed blockWidth + 1");
			System.exit(1);
		}
		if (size > blockHeight + 1) {
			System.out.println("ball size cannot exeed blockHeight + 1");
			System.exit(1);
		}
		level = 1;

		if (timer != null) {
			timer.stop();
		}

		timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
		timer.start();

		left = right = false;

		resetLevel();
	}

	private void resetLevel() {
		paused = true;

		player = new Rectangle(playerStartX, playerStartY, playerW, playerH);
		ball = new Rectangle(ballStartX, ballStartY, size, size);
		velocity.x = velStartX;
		velocity.y = velStartY;

		blockCnt = blockRows * blockCols;
		Color color = Color.pink;
		for (int r = 0; r < blockRows; r++) {
			switch (r) {
				case 0:
					color = Color.RED;
					break;
				case 1:
					color = Color.YELLOW;
					break;
				case 2:
					color = Color.ORANGE;
					break;
				case 3:
					color = Color.BLUE;
					break;
			}
			for (int c = 0; c < blockCols; c++) {
				blocks[r][c] = new Block(
						new Point(padCol + (padCol + blockWidth) * c, padTop + padRow + (padRow + blockHeight) * r),
						color);
				// g.fillRect(padCol * (c + 1) + blockWidth * c, padTop + padRow * (r + 1) +
				// blockHeight * r,
				// blockWidth, blockHeight);
			}
		}
		System.out.println("Level: " + level);
	}

	// private void enterFullScreen() {
	// GraphicsEnvironment graphicsEnvironment =
	// GraphicsEnvironment.getLocalGraphicsEnvironment();
	// GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
	// if (device.isFullScreenSupported()) {
	// device.setFullScreenWindow(frame);
	// frame.validate();
	// }
	// }

	public static double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public Point calc(Point ball, Point ball2, Rectangle block) {
		if (Line2D.linesIntersect(ball.x, ball.y, ball2.x, ball2.y, block.x, block.x + block.width, block.y, block.y)) {
		}
		return new Point(0, 0);
	}

	public boolean hitBlockDR() {
		Point block;
		int rowStart = -1;
		for (int r = 0; r < blockRows; r++) {
			block = blocks[r][0].point;
			if (ball.y <= block.y + blockHeight && newBall.y + size >= block.y) {
				rowStart = r;
				break;
			}
		}
		if (rowStart == -1) {
			return false;
		}

		int rowStop = blockRows;
		for (int r = rowStart + 1; r < blockRows; r++) {
			// System.out.println("a" + newBall.y + "b" + size + "c" +
			// blocks[r][0].point.y);
			if (newBall.y + size < blocks[r][0].point.y) {
				rowStop = r;
				break;
			}
		}

		int colStart = -1;
		for (int c = 0; c < blockCols; c++) {
			block = blocks[0][c].point;
			if (ball.x <= block.x + blockWidth) { // && newBall.x + size >= block.x) {
				colStart = c;
				break;
			}
		}

		if (colStart == -1) {
			return false;
		}

		int colStop = blockCols;
		for (int c = colStart + 1; c < blockCols; c++) {
			if (newBall.x + size < blocks[0][c].point.x) {
				colStop = c;
				break;
			}
		}
		System.out.println("rows: " + rowStart + "-" + rowStop + ", cols: " + colStart + "-" + colStop);

		float m = (float) velocity.y / velocity.x;
		for (int r = rowStart; r < rowStop; r++) {
			for (int c = colStart; c < colStop; c++) {
				if (blocks[r][c].alive) {
					block = blocks[r][c].point;

					// DR: lower left edge of ball
					int x1 = (int) ((block.y - (ball.y + size)) / m + ball.x);
					if (x1 >= block.x && x1 <= block.x + blockWidth) {
						// reflect ball here
						ball.x = x1;
						ball.y = block.y - size; // (ball.y + size);
						// System.out.println("1. block.y: " + block.y + ", newBall.y: " + newBall.y);
						newBall.y = block.y - ((newBall.y + size) - block.y) - size;
						velocity.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockColNeighbors) {
							if (c + 1 < blockCols && blocks[r][c + 1].alive
									&& ball.x + size >= blocks[r][c + 1].point.x) {
								blocks[r][c + 1].alive = false;
								blockCnt--;
							}
						}
						return true;
					}

					int y1 = (int) ((block.x - ball.x) * m + (ball.y + size));
					if (y1 >= block.y && y1 <= block.y + blockHeight) {
						// reflect ball here
						ball.y = y1;
						ball.x = block.x - size; // (ball.x + size);
						newBall.x = block.x - ((newBall.x + size) - block.x) - size;
						velocity.x *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockRowNeighbors) {
							if (r + 1 < blockRows && blocks[r + 1][c].alive
									&& ball.y + size >= blocks[r + 1][c].point.y) {
								blocks[r + 1][c].alive = false;
								blockCnt--;
							}
						}
						return true;
					}

					// DR: lower right edge of ball
					x1 += size; // = (int) ((block.y - (ball.y + size)) / m + (ball.x + size));
					if (x1 >= block.x && x1 <= block.x + blockWidth) {
						// reflect ball here
						ball.x = x1 - size;
						ball.y = block.y - size; // (ball.y + size);
						newBall.y = block.y - ((newBall.y + size) - block.y) - size;
						velocity.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						return true;
					}

					y1 = (int) ((block.x - (ball.x + size)) * m + (ball.y + size));
					if (y1 >= block.y && y1 <= block.y + blockHeight) {
						// reflect ball here
						ball.y = y1;
						ball.x = block.x - size; // ball.x;
						newBall.x = block.x - ((newBall.x + size) - block.x) - size;
						velocity.x *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						return true;
					}

					// DR: upper right edge of ball
					x1 = (int) ((block.y - ball.y) / m + (ball.x + size));
					if (x1 >= block.x && x1 <= block.x + blockWidth) {
						ball.x = x1 - size;
						ball.y = block.y - size; // (ball.y + size);
						newBall.y = block.y - ((newBall.y + size) - block.y) - size;
						velocity.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						return true;
					}

					y1 -= size; // = (int) ((block.x - (ball.x + size)) * m + (ball.y + size));
					if (y1 >= block.y && y1 <= block.y + blockHeight) {
						ball.y = y1;
						ball.x = block.x - size; // (ball.x + size);
						newBall.x = block.x - ((newBall.x + size) - block.x) - size;
						velocity.x *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						return true;
					}
				}
			}
		}
		return false;
	}

	public boolean hitBlockDL() {
		Point block;
		int rowStart = -1;
		for (int r = 0; r < blockRows; r++) {
			block = blocks[r][0].point;
			if (ball.y <= block.y + blockHeight && newBall.y + size >= block.y) {
				rowStart = r;
				break;
			}
		}
		if (rowStart == -1) {
			return false;
		}

		int rowStop = blockRows;
		for (int r = rowStart + 1; r < blockRows; r++) {
			if (newBall.y + size < blocks[r][0].point.y) {
				rowStop = r;
				break;
			}
		}

		int colStart = -1;
		for (int c = blockCols - 1; c >= 0; c--) {
			block = blocks[0][c].point;
			if (ball.x >= block.x) {
				colStart = c;
				break;
			}
		}

		if (colStart == -1) {
			return false;
		}

		int colStop = 0;
		for (int c = colStart - 1; c >= 0; c--) {
			if (newBall.x < blocks[0][c].point.x) {
				colStop = c;
				break;
			}
		}
		System.out.println("rows: " + rowStart + "-" + rowStop + ", cols: " + colStart + "-" + colStop);

		float m = (float) -velocity.y / velocity.x;
		for (int r = rowStart; r < rowStop; r++) {
			for (int c = colStart; c >= colStop; c--) {
				if (blocks[r][c].alive) {
					block = blocks[r][c].point;

					// DL: lower right edge of ball
					int x1 = (int) ((block.y - (ball.y + size)) / m + ball.x + size);
					if (x1 >= block.x && x1 < block.x + blockWidth) {
						ball.x = x1 - size;
						ball.y = block.y - size;
						newBall.y = block.y - ((newBall.y + size) - block.y) - size;
						velocity.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockColNeighbors && c - 1 > 0 && blocks[r][c - 1].alive
								&& ball.x <= blocks[r][c - 1].point.x + blockWidth) {
							blocks[r][c - 1].alive = false;
							blockCnt--;
						}
						return true;
					}

					// int y1 = (int) ((block.x + blockWidth - ball.x + size) * m + (ball.y +
					// size));
					// if (y1 >= block.y && y1 < block.y + blockHeight) {
					// ball.y = y1;
					// ball.x = block.x + blockWidth;
					// newBall.x = block.x + blockWidth + (block.x + blockWidth - newBall.x);
					// velocity.x *= -1;
					// blocks[r][c].alive = false;
					// blockCnt--;
					// if (blockRowNeighbors && r + 1 < blockRows && blocks[r + 1][c].alive
					// && ball.y + size >= blocks[r + 1][c].point.y) {
					// blocks[r + 1][c].alive = false;
					// blockCnt--;
					// }
					// return true;
					// }

					// DL: lower left edge of ball
					x1 -= size;
					if (x1 >= block.x && x1 < block.x + blockWidth) {
						ball.x = x1;
						ball.y = block.y - size;
						newBall.y = block.y - ((newBall.y + size) - block.y) - size;
						velocity.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockColNeighbors && c - 1 > 0 && blocks[r][c - 1].alive
								&& ball.x <= blocks[r][c - 1].point.x + blockWidth) {
							blocks[r][c - 1].alive = false;
							blockCnt--;
						}
						return true;
					}

					int y1 = (int) ((ball.x - block.x + blockHeight) * m + (ball.y + size));
					if (y1 >= block.y && y1 < block.y + blockHeight) {
						ball.y = y1 - size;
						ball.x = block.x + blockWidth;
						newBall.x = block.x + blockWidth + (block.x + blockWidth - newBall.x);
						velocity.x *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockRowNeighbors && r + 1 < blockRows && blocks[r + 1][c].alive
								&& ball.y + size >= blocks[r + 1][c].point.y) {
							blocks[r + 1][c].alive = false;
							blockCnt--;
						}
						return true;
					}

					// DL: upper left edge of ball
					// x1 = (int) ((block.y - ball.y) / m + (ball.x));
					// if (x1 >= block.x && x1 < block.x + blockWidth) {
					// ball.x = x1;
					// ball.y = block.y - size;
					// newBall.y = block.y - ((newBall.y + size) - block.y) - size;
					// velocity.y *= -1;
					// blocks[r][c].alive = false;
					// blockCnt--;
					// if (blockColNeighbors && c - 1 > 0 && blocks[r][c - 1].alive
					// && ball.x <= blocks[r][c - 1].point.x + blockWidth) {
					// blocks[r][c - 1].alive = false;
					// blockCnt--;
					// }
					// return true;
					// }

					y1 -= size;
					if (y1 >= block.y && y1 < block.y + blockHeight) {
						ball.y = y1;
						ball.x = block.x; // - (ball.x + size);
						newBall.x = block.x + blockWidth + (block.x + blockWidth - newBall.x);
						velocity.x *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockRowNeighbors && r + 1 < blockRows && blocks[r + 1][c].alive
								&& ball.y + size >= blocks[r + 1][c].point.y) {
							blocks[r + 1][c].alive = false;
							blockCnt--;
						}
						return true;
					}
				}
			}
		}
		return false;

	}

	public boolean hitBlock() {
		// return Rectangle.(ball.x, ball.y, ball2.x, ball2.y, block.x, block.x +
		// block.width, block.y, block.y);
		boolean found = false;
		for (int r = 0; r < blockRows; r++) {
			for (int c = 0; c < blockCols; c++) {
				if (blocks[r][c].alive) {
					if (ball.intersects(blocks[r][c].point.x, blocks[r][c].point.y, blockWidth, blockHeight)) {
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockCnt <= 0) {
							onWin();
						}
						found = true;
					}
				}
			}
		}
		if (found) {
			// ball.y += velocity.y;
			velocity.y *= -1;
		}
		return false;
	}

	// The update method does 5 things
	// 1 - it has the player move based on what key is currently being pressed
	// 2 - it prevents the player from leaving the screen
	// 3 - it checks if the player has reached the goal, and if so congratualtes
	// them and restarts the game
	public void update() {
		if (paused)
			return;
		if (left) {
			player.x -= 10;
		}
		if (right) {
			player.x += 10;
		}

		if (player.x < 0) {
			player.x = 0;
		} else if (player.x + player.width >= gameWidth) {
			player.x = gameWidth - player.width;
		}

		newBall = new Point(ball.x + velocity.x, ball.y + velocity.y);

		if (velocity.y > 0
				&& Line2D.linesIntersect(ball.x, ball.y, newBall.x, newBall.y, player.x, player.y, player.x + playerW,
						player.y)) {
			velocity.y *= -1;
			newBall.y = 2 * player.y - newBall.y;
		}

		// bounce off walls
		while (true) {
			if (velocity.x < 0 && velocity.y < 0) {
				hitBlock();
				// bounce off blocks going up and to the left
				// if (hitBlock()) {
				// continue;
				// }
				if (newBall.x < 0 && newBall.y < 0) {
					if (newBall.x < newBall.y) {
						ball.y -= ball.x * velocity.y / velocity.x;
						ball.x = 0;
						velocity.x *= -1;
						newBall.x *= -1;
					} else {
						ball.x -= ball.y * velocity.x / velocity.y;
						ball.y = 0;
						velocity.y *= -1;
						newBall.y *= -1;
					}
					continue;
				}
				if (newBall.x < 0) {
					ball.y -= ball.x * velocity.y / velocity.x;
					ball.x = 0;
					velocity.x *= -1;
					newBall.x *= -1;
					break;
				}
				if (newBall.y < 0) {
					ball.x -= ball.y * velocity.x / velocity.y;
					ball.y = 0;
					velocity.y *= -1;
					newBall.y *= -1;
					break;
				}
			} else if (velocity.x > 0 && velocity.y < 0) {
				hitBlock();
				if (newBall.x > maxWidth) {
					velocity.x *= -1;
					newBall.x = 2 * (maxWidth) - newBall.x;
					continue;
				}
				if (newBall.y < 0) {
					velocity.y *= -1;
					newBall.y *= -1;
					continue;
				}
			} else if (velocity.x < 0 && velocity.y > 0) {
				hitBlockDL();
				if (newBall.x < 0) {
					velocity.x *= -1;
					newBall.x *= -1;
					continue;
				}
				if (newBall.y > maxHeight) {
					velocity.y *= -1;
					newBall.y = 2 * (maxHeight) - newBall.y;
					continue;
				}
			} else { // (vel.x > 0 && vel.y > 0)
				hitBlockDR();
				if (newBall.x > maxWidth) {
					velocity.x *= -1;
					newBall.x = 2 * (maxWidth) - newBall.x;
					continue;
				}
				if (newBall.y > maxHeight) {
					velocity.y *= -1;
					newBall.y = 2 * (maxHeight) - newBall.y;
					// System.out.println("gameHeight-1: " + (maxHeight) + ", newBall.y: " +
					// newBall.y);
					continue;
				}
				// if (newBall.y >= gameHeight - 1 - size) {
				// newBall.y = gameHeight - 1 - size;
				// vel.y *= -1;
				// }
				// // synchronized (countMutex) {
				// // if (count == 0) {
				// // count++;
				// // onLose();
				// // }
				// // }
				// if (newBall.y <= 0) {
				// newBall.y = 0;
				// vel.y *= -1;
				// }
			}
			break;
		}

		ball.x = newBall.x;
		ball.y = newBall.y;
		if (blockCnt <= 0) {
			onWin();
		}

	}

	// The paint method does 3 things
	// 1 - it draws a white background
	// 2 - it draws the player in blue
	// 3 - it draws the ball in green
	// 4 - it draws all the blocks
	public void paint(Graphics g) {

		g.setColor(Color.darkGray);
		g.fillRect(0, 0, gameWidth, gameHeight);

		// Graphics2D g2 = (Graphics2D) g;
		// g2.setColor(Color.RED);
		// g2.fill.fillRect(200f, 200f, 40f, 40f);

		g.setFont(new Font("Algerian", Font.BOLD, 15));
		g.setColor(Color.BLACK);
		g.drawString("Level: " + level + " Highscore: " + highScore, 5, 15);

		g.setColor(Color.BLUE);
		g.fillRect(player.x, player.y, player.width, player.height);

		g.setColor(Color.GREEN);
		g.fillRect(ball.x, ball.y, ball.width, ball.height);

		for (int r = 0; r < blockRows; r++) {
			g.setColor(blocks[r][0].color);
			for (int c = 0; c < blockCols; c++) {
				if (blocks[r][c].alive) {
					g.fillRect(blocks[r][c].point.x, blocks[r][c].point.y, blockWidth, blockHeight);
				}
			}
		}

		if (paused) {
			g.setColor(Color.BLACK);
			g.drawString("PAUSED (space to toggle)", gameWidth / 2 - 90, gameHeight / 2);
		}
	}

	public int getGameHeight() {
		return gameHeight;
	}

	public void onWin() {
		// player.setRect(new Rectangle(50, 50, size, size));

		level++;
		if (level > highScore) {
			highScore = level;
			// System.out.println("HighScore: " + highScore);
		}

		// System.out.println("Level: " + level);
		createDialog("You Won!", 1000);

		resetLevel();
	}

	public void onLose() {
		// player.setRect(new Rectangle(playerStartX, playerStartY, playerW, playerH));

		if (level > 1) {
			level--;
		}

		System.out.println("Level: " + level);
		createDialog("You Lost", dialogDelay);
		ball = new Rectangle(ballStartX, ballStartY, size, size);
	}

	// Sets visible a Pseudo-dialog that removes itself after a fixed time interval
	// Uses a thread to not block the rest of the program
	//
	// @param: message: String -> The message that will appear on the dialog
	// @param: delay: int -> How long (in milliseconds) that Dialog is visible
	private void createDialog(String message, int delay) {
		dialogLabel.setText(message);
		dialog.setVisible(true);
		frame.requestFocus();

		Thread thread = new Thread(() -> {
			try {
				// Show pop up for [delay] milliseconds
				Thread.sleep(delay);
			} catch (Exception e) {
				System.out.println("Thread failed :(");
				dialog.setVisible(false);
				frame.requestFocus();
			}
			// End of 3 seconds
			// Close the pop up
			dialog.setVisible(false);
			frame.requestFocus();

			synchronized (countMutex) {
				count--;
			}
		});
		thread.start();
	}

	public static void delay(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception e) {
		}
	}
}
