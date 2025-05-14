import java.awt.*;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

// import java.awt.Graphics2D;
// import java.awt.geom.Rectangle2D;

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

public class Breakout extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	private Rectangle ball = new Rectangle(); // a rectangle that represents the ball
	private Point nextCalc = new Point();

	private int level = 1;
	private int highScore = 1;

	private boolean left, right; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer
	private boolean paused; // the update timer

	private final int dialogDelay = 2000;

	private static int frameRate = 60; // roughly frame rate per second

	private final int velStartX = 1; // roughly frame rate per second
	private final int velStartY = 3; // roughly frame rate per second

	private Point vel = new Point(); // velocity of ball
	// private Point velSign = new Point(); // velocity of ball
	private Point newBall = new Point(); // ball.x + velocity.x, ball.y + velocity.y);

	private final int size = 10; // ball size
	// private final int radius = 5; // ball radius

	private final int blockRows = 4;
	private final int blockCols = 10;
	private final int blockWidth = 40;
	private final int blockHeight = 15;
	private final int padCol = 1; // padding between columns
	private final int padRow = 1; // padding between rows
	private final int padTop = 60; // padding above blocks
	private final int padMiddle = 130; // padding between blocks and paddle
	private final int padBottom = 20; // padding below paddle
	private Block[][] blocks = new Block[blockRows][blockCols];
	private int blockCnt = blockRows * blockCols;
	private final boolean blockColNeighbors = size > padCol + 2; // (blockWidth + padCol) + 1;
	private final boolean blockRowNeighbors = size > padRow + 2; // (blockWidth + padCol) + 1;

	private final int ballStartX = 90;
	private final int ballStartY = 10; // padTop + blockRows * (blockHeight + padRow) + 10;

	private final int ballMiddle = size / 2;
	private final int playerW = 96 - ballMiddle;
	private final int playerH = 10;

	// the width of the game area
	private final int gameWidth = padCol + blockCols * (blockWidth + padCol);
	// the height of the game area
	private final int gameHeight = padTop + blockRows * (blockHeight + padRow) + padMiddle + playerH + padBottom;

	private final int playerSegments = 6;
	// private final int playerSegment = playerW / 2 / playerSegments;

	private Point[] bounces = new Point[playerSegments];
	private final int playerStartX = 10;
	private final int playerStartY = gameHeight - padBottom - playerH;

	private final int maxWidth = gameWidth - 1 - size;
	private final int maxHeight = gameHeight - 1 - size;
	// max player.x position
	private final int mouseWidth = gameWidth - playerW;

	private static JLabel dialogLabel;
	private static JFrame frame;
	private static JDialog dialog;
	private static int screenWidth;
	private boolean keyboard = true;

	final int horzBlockLeft = 0;
	final int horzBlockRight = 1;
	final int vertBlockBottom = 2;
	final int vertBlockTop = 3;
	final int horzWall = 0;
	final int vertWall = 1;

	// Distance calcs
	static Distances dist = new Distances();

	// MIDI
	static Receiver rcvr;
	static Synthesizer synth = null;
	static ShortMessage paddleMsg = new ShortMessage();
	static ShortMessage wallMsg = new ShortMessage();
	static ShortMessage brickMsg = new ShortMessage();
	static ShortMessage paddleOffMsg = new ShortMessage();
	static ShortMessage wallOffMsg = new ShortMessage();
	// static ShortMessage brickOffMsg = new ShortMessage();
	static boolean soundPossible = false;
	static boolean mute = true;

	void playSound(ShortMessage msg, int time) {
		if (!mute) {
			long t = synth.getMicrosecondPosition();
			rcvr.send(msg, -1); // time in microseconds
			rcvr.send(paddleOffMsg, t + 100000); // time in microseconds
			rcvr.send(wallOffMsg, t + 100000); // time in microseconds
		}
	}

	// Sets up the basic GUI for the game
	public static void main(String[] args) {
		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
			// note Middle C = 60,
			// moderately loud (velocity = 93).
			final int noteVelocity = 83;
			paddleMsg.setMessage(ShortMessage.NOTE_ON, 0, 50, noteVelocity);
			wallMsg.setMessage(ShortMessage.NOTE_ON, 0, 40, noteVelocity);
			brickMsg.setMessage(ShortMessage.NOTE_ON, 0, 100, noteVelocity);
			paddleOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 50, noteVelocity);
			wallOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 40, noteVelocity);
			// brickOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 100, noteVelocity);
			rcvr = MidiSystem.getReceiver();
			soundPossible = true;
			mute = false;
		} catch (Exception e) {
			System.out.println("Warning: cound not initialize the MIDI system for audio. Sound disabled");
			// System.exit(1);
		}
		frame = new JFrame();

		dialog = new JDialog(frame, "Status");
		dialogLabel = new JLabel("");
		dialogLabel.setHorizontalAlignment(JLabel.CENTER);
		dialog.add(dialogLabel);
		dialog.setBounds(125, 125, 100, 70);
		dialog.setVisible(false);

		frame.setTitle("Obstacle Game");
		frame.setLayout(new BorderLayout());
		// frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));

		Breakout game = new Breakout();
		// frame.add(game, BorderLayout.CENTER);

		// add box to keep game in center while resizing window
		// from:
		// https://stackoverflow.com/questions/7223530/how-can-i-properly-center-a-jpanel-fixed-size-inside-a-jframe
		Box box = new Box(BoxLayout.Y_AXIS);

		box.add(Box.createVerticalGlue());
		box.add(game);
		box.add(Box.createVerticalGlue());
		frame.add(box);

		game.addKeyListener(game);
		frame.addKeyListener(game);
		dialog.addKeyListener(game);
		// frame.addMouseMotionListener(game);
		// frame.removeMouseMotionListener(game);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		// System.out.println(graphicsEnvironment.getMaximumWindowBounds());
		screenWidth = graphicsEnvironment.getMaximumWindowBounds().width;

		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		System.out.println("refresh rate: " + device.getDisplayMode().getRefreshRate());
		frameRate = device.getDisplayMode().getRefreshRate();

		game.setUpGame();
		// game.enterFullScreen();
	}

	// Constructor for the game panel
	public Breakout() {
		Dimension d = new Dimension(gameWidth, gameHeight);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);

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
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) {
			left = true;
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			right = true;
		} else if (keyCode == KeyEvent.VK_A) {
			left = true;
		} else if (keyCode == KeyEvent.VK_D) {
			right = true;
		} else if (keyCode == KeyEvent.VK_Q) {
			System.exit(0);
		} else if (keyCode == KeyEvent.VK_R) {
			setUpGame();
		} else if (keyCode == KeyEvent.VK_K) {
			if (keyboard) {
				enterFullScreen();
				frame.addMouseMotionListener(this);
			} else {
				exitFullScreen();
				frame.removeMouseMotionListener(this);
			}
			keyboard = !keyboard;
		} else if (keyCode == KeyEvent.VK_M) {
			if (soundPossible)
				mute = !mute;
		} else if (keyCode == KeyEvent.VK_P) {
			paused = !paused;
		} else if (keyCode == KeyEvent.VK_MINUS) {
			if (frameRate > 2) {
				frameRate /= 2;
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (keyCode == KeyEvent.VK_EQUALS || keyCode == KeyEvent.VK_PLUS) {
			frameRate *= 2;
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (keyCode == KeyEvent.VK_SPACE) {
			paused = !paused;
		}
	}

	// Called every time a key is released
	// Stores the down state for use in the update method
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_LEFT) {
			left = false;
		} else if (keyCode == KeyEvent.VK_RIGHT) {
			right = false;
		} else if (keyCode == KeyEvent.VK_A) {
			left = false;
		} else if (keyCode == KeyEvent.VK_D) {
			right = false;
		}
	}

	// Called every time a key is typed
	public void keyTyped(KeyEvent e) {
	}

	// Sets the initial state of the game
	// Could be modified to allow for multiple levels
	public void setUpGame() {
		boolean ignoreDeadCode = Math.random() == 2;
		if (size > blockWidth + 1 || ignoreDeadCode) {
			System.out.println("ball size cannot exeed blockWidth + 1");
			System.exit(1);
		}
		if (size > blockHeight + 1 || ignoreDeadCode) {
			System.out.println("ball size cannot exeed blockHeight + 1");
			System.exit(1);
		}

		// for (int i=0;i<bounces.length;i++){
		bounces[0] = new Point(-3, -1);
		bounces[1] = new Point(-2, -2);
		bounces[2] = new Point(-1, -3);
		bounces[3] = new Point(1, -3);
		bounces[4] = new Point(2, -2);
		bounces[5] = new Point(3, -1);

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
		vel.x = velStartX;
		vel.y = velStartY;
		int framesTillNextCalc = (player.y - 1 - size - ball.y) / vel.y;
		nextCalc.x = ball.x + vel.x * framesTillNextCalc;
		nextCalc.y = ball.y + vel.y * framesTillNextCalc;
		System.out.println(nextCalc + ", " + framesTillNextCalc);

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

	public void enterFullScreen() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			device.setFullScreenWindow(frame);
			// device.getDisplayModes();
			frame.validate();
		}
	}

	public void exitFullScreen() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			device.setFullScreenWindow(null);
			// device.getDisplayModes();
			frame.validate();
		}
	}

	public static double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public static float calcDist(int x1, int y1, int x2, int y2) {
		return (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public Point calc(Point ball, Point ball2, Rectangle block) {
		if (Line2D.linesIntersect(ball.x, ball.y, ball2.x, ball2.y, block.x, block.x + block.width, block.y, block.y)) {
		}
		return new Point(0, 0);
	}

	public int blockCol(int x) {
		x = (x - padCol);
		if (x < 0)
			return -1;
		// make x = col
		x /= (blockWidth + padCol);
		if (x >= blockCols)
			return blockCols - 1;
		return x;
	}

	public int blockRow(int y) {
		y = (y - padTop);
		if (y < 0)
			return -1;
		// make y = row
		y /= (blockHeight + padRow);
		if (y >= blockRows)
			return blockRows - 1;
		return y;
	}

	public boolean nextHitDR() {
		for (int i = 0; i < dist.wall.length; i++) {
			dist.wall[i].dist = Double.MAX_VALUE;
		}
		for (int i = 0; i < dist.block.length; i++) {
			dist.block[i].dist = Double.MAX_VALUE;
		}
		double m = (double) vel.y / vel.x;

		boolean foundHit = false;
		double min = Double.MAX_VALUE;
		do {
			if (vel.x > 0 && vel.y > 0) {
				// horizontal wall hit
				if (newBall.y > maxHeight) {
					foundHit = true;
					double dy = (maxHeight - (ball.y + size));
					double dx = dy / m;
					double d = Math.pow(dx, 2) + Math.pow(dy, 2);
					if (d <= min) {
						min = d;
						dist.wall[horzWall].dist = d;
						dist.wall[horzWall].ballX = ball.x + (int) dx;
						dist.wall[horzWall].ballY = maxHeight;
					}
				}
				// vertical wall hit
				if (newBall.x > maxWidth) {
					foundHit = true;
					double dx = (maxWidth - ball.x);
					double dy = dx * m;
					double d = Math.pow(dx, 2) + Math.pow(dy, 2);
					if (d <= min) {
						min = d;
						dist.wall[vertWall].dist = d;
						dist.wall[vertWall].ballX = maxWidth;
						dist.wall[vertWall].ballY = ball.y + (int) dy;
						// System.out.println("a. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
						// newBall.x: " + newBall.x
						// + ", newBall.y: " + newBall.y);
						// System.out.println(
						// "ballX: " + maxWidth + ", ball.y: " + ball.y + ", dx: " + (int) dx + ", dy: "
						// + (int) dy);
					}
				}
				int rowBeg = blockRow(ball.y + size) + 1;
				int rowEnd = blockRow(newBall.y + size) + 1;
				int colBeg;
				int colEnd;
				for (int r = rowBeg; r < rowEnd; r++) {
					// LR hit horiz block check
					int hitY = blocks[r][0].point.y;
					int hitX = (int) ((hitY - (ball.y + size)) / m + (ball.x + size));
					int bc = blockCol(hitX);
					if (bc > -1 && hitX >= blocks[r][bc].point.x && hitX < blocks[r][bc].point.x + blockWidth
							&& blocks[r][bc].alive) {
						foundHit = true;
						double d = Math.pow(hitX - (ball.x + size), 2)
								+ Math.pow(hitY - (ball.y + size), 2);
						if (d <= min) {
							min = d;
							dist.block[horzBlockRight].dist = d;
							dist.block[horzBlockRight].blockRow = r;
							dist.block[horzBlockRight].blockCol = bc;
							dist.block[horzBlockRight].ballX = hitX;
							dist.block[horzBlockRight].ballY = hitY - size;
						}
					}
					// LL hit horiz block check
					hitX -= size;
					if (hitX - 0 > padCol) {
						int bc2 = blockCol(hitX - 0);
						if (bc2 > -1 && bc != bc2 && blocks[r][bc2].alive) {
							foundHit = true;
							double d = Math.pow((hitX - 0) - ball.x, 2)
									+ Math.pow(hitY - (ball.y + size), 2);
							if (d <= min) {
								min = d;
								dist.block[horzBlockLeft].dist = d;
								dist.block[horzBlockLeft].blockRow = r;
								dist.block[horzBlockLeft].blockCol = bc2;
								dist.block[horzBlockLeft].ballX = hitX;
								dist.block[horzBlockLeft].ballY = hitY - size;
							}
						}
					}
				}
				colBeg = blockCol(ball.x + size) + 1;
				colEnd = blockCol(newBall.x + size) + 1;
				for (int c = colBeg; c < colEnd; c++) {
					System.out.println(colBeg + " " + colEnd + " " + ball.x + " " + newBall.x + " "
							+ (colBeg * (blockWidth + padCol) + padCol));
					// LR hit vert block check
					int hitX2 = blocks[0][c].point.x;
					int hitY2 = (int) ((hitX2 - (ball.x + size)) * m + ball.y + size);
					int br = blockRow(hitY2);
					if (br > -1 && hitY2 >= blocks[br][c].point.y && hitY2 < blocks[br][c].point.y + blockHeight
							&& blocks[br][c].alive) {
						foundHit = true;
						int x2 = hitX2 - (ball.x + size);
						double d = Math.pow(x2, 2)
								+ Math.pow(hitY2 - (ball.y + size), 2);
						if (d <= min) {
							min = d;
							dist.block[vertBlockBottom].dist = d;
							dist.block[vertBlockBottom].blockRow = br;
							dist.block[vertBlockBottom].blockCol = c;
							dist.block[vertBlockBottom].ballX = hitX2 - size;
							dist.block[vertBlockBottom].ballY = hitY2 - size;
						}
					}
					hitY2 -= size;
					System.out.println("hitY2: " + hitY2);
					if (hitY2 - 0 > padTop) {
						// UR hit vert block check
						int br2 = blockRow(hitY2 - 0);
						System.out.println("br2: " + br2);
						if (br2 > -1 && blocks[br2][c].alive) { // br != br2 &&
							foundHit = true;
							int x2 = hitX2 - (ball.x + size);
							double d = Math.pow(x2 - 0 - ball.x, 2)
									+ Math.pow(blocks[br2][c].point.y - (ball.y - 0), 2);
							if (d <= min) {
								min = d;
								dist.block[vertBlockTop].dist = d;
								dist.block[vertBlockTop].blockRow = br2;
								dist.block[vertBlockTop].blockCol = c;
								dist.block[vertBlockTop].ballX = hitX2 - size;
								dist.block[vertBlockTop].ballX = hitY2 - 0;
							}
						}
					}
				}

				if (!foundHit) {
					return false;
				}

				System.out.println("min: " + min);
				for (int i = 0; i < dist.wall.length; i++) {
					System.out.println("dist wall  " + i + ": " + dist.wall[i].dist);
				}
				for (int i = 0; i < dist.block.length; i++) {
					System.out.println("dist block " + i + ": " + dist.block[i].dist);
				}

				if (dist.wall[vertWall].dist == min) {
					if (dist.block[horzBlockRight].dist == min) { // LR
						BlockDist bd = dist.block[horzBlockRight];
						blocks[bd.blockRow][bd.blockCol].alive = false;
						vel.y *= -1;
						// newBall.x = ball.x - (newBall.x - ball.x);
						newBall.y = 2 * ball.y - newBall.y;
					} else {
					}
					ball.x = dist.wall[vertWall].ballX;
					ball.y = dist.wall[vertWall].ballY;
					vel.x *= -1;
					// System.out.println("b. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
					newBall.x = 2 * ball.x - newBall.x;
					// System.out.println("c. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
				} else if (dist.block[vertBlockBottom].dist == min || dist.block[vertBlockTop].dist == min) {
					if (dist.block[vertBlockBottom].dist == min) {
						BlockDist bd = dist.block[vertBlockBottom];
						blocks[bd.blockRow][bd.blockCol].alive = false;
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dist.block[vertBlockTop].dist == min) {
						BlockDist bd = dist.block[vertBlockTop];
						blocks[bd.blockRow][bd.blockCol].alive = false;
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dist.block[horzBlockLeft].dist == min || dist.block[horzBlockRight].dist == min) {
					if (dist.block[horzBlockLeft].dist == min) {
						BlockDist bd = dist.block[horzBlockLeft];
						blocks[bd.blockRow][bd.blockCol].alive = false;
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dist.block[horzBlockRight].dist == min) {
						BlockDist bd = dist.block[horzBlockRight];
						blocks[bd.blockRow][bd.blockCol].alive = false;
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
				} else if (dist.wall[horzWall].dist == min) { // Possible to have padTop small enough to hit top and
																// block
					ball.x = dist.wall[horzWall].ballX;
					ball.y = dist.wall[horzWall].ballY;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					// if (level > 1) {
					// synchronized (countMutex) {
					// if (count == 0) {
					// count++;
					// onLose();
					// resetLevel();
					// return false;
					// }
					// }
					// }
				} else { // no hits
					ball.x = newBall.x;
					ball.y = newBall.y;
				}
			} else if (vel.x > 0 && vel.y < 0) {
			} else if (vel.x < 0 && vel.y > 0) {
			} else { // if (vel.x < 0 && vel.y < 0) {
			}
			return foundHit; // INFO: fill in rest of this method
		} while (foundHit);
	}

	public boolean hitBlockDR() {

		if (ball.y > padTop + blockRows * (blockHeight + padRow)) { // || ball.y < padTop) {
			return false;
		}

		int rowBeg = blockRow(ball.y + size) + 1;
		int rowEnd = blockRow(newBall.y + size) + 1;
		// System.out.println("rowBeg: " + rowBeg + ", rowEnd: " + rowEnd + ",
		// ball.y+size: " + (ball.y + size)
		// + ", padTop: " + padTop + "newBall.y + size: " + (newBall.y + size));
		int colBeg;
		int colEnd;

		// boolean hitLR = false;
		// boolean hitUR = false;
		// boolean hitLL = false;
		final double maxDist = 100;
		double hitLR = maxDist;
		double hitUR = maxDist;
		double hitLL = maxDist;
		int hitLRr = -1;
		int hitLRc = -1;
		int hitLLr = -1;
		int hitLLc = -1;
		int hitURr = -1;
		int hitURc = -1;
		float m = (float) vel.y / vel.x;
		for (int r = rowBeg; r < rowEnd; r++) {
			int x = (int) ((blocks[r][0].point.y - (ball.y + size)) / m + ball.x + size);
			int bc = blockCol(x);
			if (bc > -1 && blocks[r][bc].alive) {
				// hitLR = true;
				double dist = Math.pow(x - (ball.x + size), 2)
						+ Math.pow(blocks[r][bc].point.y - (ball.y + size), 2);
				if (dist < hitLR) {
					hitLR = dist;
					hitLRr = r;
					hitLRc = bc;
				}
			}
			if (x - size > padCol) {
				int bc2 = blockCol(x - size);
				// System.out.println("bc:" + bc + ",bc2:" + bc2);
				if (bc2 > -1 && bc != bc2 && blocks[r][bc2].alive) {
					// hitLL = true;
					double dist = Math.pow(x - size - ball.x, 2)
							+ Math.pow(blocks[r][bc2].point.y - (ball.y + size), 2);
					if (dist < hitLL) {
						hitLL = dist;
						hitLLr = r;
						hitLLc = bc2;
					}
				}
			}
			colBeg = blockCol(ball.x + size);
			colEnd = blockCol(newBall.x + size);
			for (int c = colBeg; c < colEnd; c++) {
				int y = (int) ((blocks[r][c].point.x - (ball.x + size)) * m + ball.y);
				if (y >= blocks[r][c].point.y && y < blocks[r][c].point.y + blockHeight && blocks[r][c].alive) {
					// hitUR = true;
					double dist = Math.pow(blocks[r][c].point.x - (ball.x + size), 2)
							+ Math.pow(blocks[r][c].point.y - ball.y, 2);
					if (dist < hitUR) {
						hitUR = dist;
						hitURr = r;
						hitURc = c;
					}
				}
			}
		}

		if (hitLR == hitUR && hitLR < hitLL) {
			System.out.println("2 hits LR,UR: (" + hitLRr + "," + hitLRc + ") (" + hitURr + "," + hitURc + ")");
		} else if (hitLL == hitLR && hitLL < hitUR) {
			System.out.println("2 hits LL,LR: (" + hitLLr + "," + hitLLc + ") (" + hitLRr + "," + hitLRc + ")");
		} else if (hitLR < hitUR && hitLR < hitLL) {
			System.out.println("1 hit LR: (" + hitLRr + "," + hitLRc + ")");
			// System.out.println("hitLR: " + hitLR + ", hitLL: " + hitLL);
		} else if (hitLL < hitLR && hitLL < hitUR) {
			System.out.println("1 hit LL: (" + hitLLr + "," + hitLLc + ")");
		} else if (hitUR < hitLR && hitUR < hitLL) {
			System.out.println("1 hit UR: (" + hitURr + "," + hitURc + ")");
		}
		// System.out.println("hitLR:" + hitLR + ", hitLL:" + hitLL + ", hitUR:" +
		// hitUR);

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
		// System.out.println("rows: " + rowStart + "-" + rowStop + ", cols: " +
		// colStart + "-" + colStop);

		// float m = (float) velocity.y / velocity.x;
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
						vel.y *= -1;
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
						vel.x *= -1;
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
						vel.y *= -1;
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
						vel.x *= -1;
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
						vel.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						return true;
					}

					y1 -= size; // = (int) ((block.x - (ball.x + size)) * m + (ball.y + size));
					if (y1 >= block.y && y1 <= block.y + blockHeight) {
						ball.y = y1;
						ball.x = block.x - size; // (ball.x + size);
						newBall.x = block.x - ((newBall.x + size) - block.x) - size;
						vel.x *= -1;
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
			// System.out.println("rows: " + rowStart);
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
			if (ball.x + size >= block.x && newBall.x <= block.x + blockWidth) {
				colStart = c;
				break;
			}
		}

		if (colStart == -1) {
			System.out.println("rows: " + rowStart + "-" + rowStop + ", cols: " + colStart);
			return false;
		}

		int colStop = colStart;
		for (int c = colStart - 1; c >= 0; c--) {
			if (newBall.x + size > blocks[0][c].point.x) {
				colStop = c;
				break;
			}
		}
		// System.out.println("rows: " + rowStart + "-" + rowStop + ", cols: " +
		// colStart + "-" + colStop);

		float m = (float) -vel.y / vel.x;
		for (int r = rowStart; r < rowStop; r++) {
			for (int c = colStart; c >= colStop; c--) {
				if (blocks[r][c].alive) {
					block = blocks[r][c].point;

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
					int x1 = (int) ((block.y - (ball.y + size)) / m + ball.x + size);
					if (x1 >= block.x && x1 < block.x + blockWidth) {
						// hits horiz
						ball.x = x1;
						ball.y = block.y - size;
						newBall.y = block.y - ((newBall.y + size) - block.y) - size;
						vel.y *= -1;
						blocks[r][c].alive = false;
						blockCnt--;
						return true;
					} else if (x1 < block.x) {
						// x1 misses block to left so check lr for horiz hit
						// DL: lower right edge of ball
						// int llX = (int) ((block.y - (ball.y + size)) / m + ball.x + size);
						x1 += size;
						if (x1 >= block.x && x1 < block.x + blockWidth) {
							ball.x = x1 - size;
							ball.y = block.y - size;
							newBall.y = block.y - ((newBall.y + size) - block.y) - size;
							vel.y *= -1;
							blocks[r][c].alive = false;
							blockCnt--;
							if (blockColNeighbors && c - 1 > 0 && blocks[r][c - 1].alive
									&& ball.x < blocks[r][c - 1].point.x + blockWidth) {
								blocks[r][c - 1].alive = false;
								blockCnt--;
							}
							return true;
						}
					} else {
						// check for ll hit vertical
						int y1 = (int) ((ball.x - block.x + blockHeight) * m + (ball.y + size));
						if (y1 >= block.y && y1 <= block.y + blockHeight) {
							// if (r + 1 < blockRows && blocks[r + 1][c].alive) {
							// // check lr for neighbor downward hit here
							// int lrX = (int) ((block.y - (ball.y + size)) / m + ball.x + size);
							// if (lrX >= block.x && lrX < block.x + blockWidth) {
							// float llVertDist = calculateDistance(ball.x, ball.y + size, block.x +
							// blockHeight,
							// y1);
							// float lrDist = calculateDistance(ball.x + size, ball.y + size, lrX, block.y);
							// if (lrDist < llVertDist) {
							// ball.x = lrX - size;
							// ball.y = block.y - size;
							// newBall.y = block.y - ((newBall.y + size) - block.y) - size;
							// velocity.y *= -1;
							// blocks[r][c].alive = false;
							// blockCnt--;
							// return true;
							// }
							// }
							// }
							ball.y = y1 - size;
							ball.x = block.x + blockWidth;
							newBall.x = block.x + blockWidth + (block.x + blockWidth - newBall.x);
							vel.x *= -1;
							blocks[r][c].alive = false;
							blockCnt--;
							return true;
						} else if (y1 >= block.y + blockHeight) {
							// check for ul hit vertical
							y1 -= size;
							if (y1 >= block.y && y1 < block.y + blockHeight) {
								// check lr hit neighbor down
								if (r + 1 < blockRows && blocks[r + 1][c].alive) {
									Point lowerBlock = blocks[r + 1][c].point;
									int lrX = (int) ((lowerBlock.y - (ball.y + size)) / m + ball.x + size);
									if (lrX >= lowerBlock.x && lrX < lowerBlock.x + blockWidth) {
										float lrDist = calcDist(ball.x + size, ball.y + size, lrX, lowerBlock.y);
										float ulVertDist = calcDist(ball.x, ball.y,
												block.x + blockWidth,
												y1);
										if (lrDist < ulVertDist) {
											ball.x = lrX - size;
											ball.y = block.y - size;
											newBall.y = block.y - ((newBall.y + size) - block.y) - size;
											vel.y *= -1;
											blocks[r][c].alive = false;
											blockCnt--;
											return true;
										}
									}
								}
								ball.y = y1;
								ball.x = block.x; // - (ball.x + size);
								newBall.x = block.x + blockWidth + (block.x + blockWidth - newBall.x);
								vel.x *= -1;
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

				}
			}
		}
		return false;

	}

	public boolean hitBlock() {
		// return Rectangle.(ball.x, ball.y, ball2.x, ball2.y, block.x, block.x +
		// block.width, block.y, block.y);
		// int hitCnt = 0;
		boolean found = false;
		for (int r = 0; r < blockRows; r++) {
			for (int c = 0; c < blockCols; c++) {
				if (blocks[r][c].alive) {
					if (ball.intersects(blocks[r][c].point.x, blocks[r][c].point.y, blockWidth, blockHeight)) {
						// hitCnt++;
						blocks[r][c].alive = false;
						blockCnt--;
						if (blockCnt <= 0) {
							// onWin();
							return true;
						}
						found = true;
					}
				}
			}
		}
		if (found) {
			// ball.y += velocity.y;
			// velocity.y *= -1;
			vel.y = Math.abs(vel.y);
		}
		return found;
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

		// if (ball.x != nextCalc.x || ball.y != nextCalc.y)
		// return;
		// System.out.println(ball);

		// if (ball.x == nextCalc.x && ball.y == nextCalc.y) {
		// if (ball.y + size == player.y - 1) {
		// // check player hit
		// }
		// int framesTillNextCalc = (player.y - 1 - size - ball.y) / velocity.y;
		// nextCalc.x = ball.x + velocity.x * framesTillNextCalc;
		// nextCalc.y = ball.y + velocity.y * framesTillNextCalc;
		// System.out.println(nextCalc + ", " + framesTillNextCalc);
		// paused = true;
		// }

		// if ((int) Math.random() == 0)
		// return;

		newBall = new Point(ball.x + vel.x, ball.y + vel.y);

		// Check for player paddle hit ball
		if (ball.y + size < player.y && newBall.y + size >= player.y) {
			int hitX = (int) (ball.x + (float) vel.x / vel.y * (player.y - (ball.y + size)));
			if (hitX >= player.x - (size - 1) && hitX < player.x + playerW) {
				int hit = (hitX - (player.x - (size - 1))) * playerSegments / (playerW + (size - 1));
				vel.x = bounces[hit].x;
				vel.y = bounces[hit].y;
				// System.out.println("vel:" + velocity + ", hit:" + hit);
				// velocity.y *= -1;
				newBall.y = 2 * player.y - newBall.y - 2 * size;
				playSound(paddleMsg, -1);
				// newBall.y = player.y - size;
			}
		}
		// if (velocity.y > 0
		// && Line2D.linesIntersect(ball.x, ball.y, newBall.x, newBall.y, player.x,
		// player.y, player.x + playerW,
		// player.y)) {
		// velocity.y *= -1;
		// newBall.y = 2 * player.y - newBall.y;
		// }

		// bounce off walls
		while (true) {
			if (vel.x < 0 && vel.y < 0) {
				if (hitBlock())
					playSound(brickMsg, -1);
				// bounce off blocks going up and to the left
				// if (hitBlock()) {
				// continue;
				// }
				if (newBall.x < 0 && newBall.y < 0) {
					if (newBall.x < newBall.y) {
						ball.y -= ball.x * vel.y / vel.x;
						ball.x = 0;
						vel.x *= -1;
						newBall.x *= -1;
						playSound(wallMsg, -1);
					} else {
						ball.x -= ball.y * vel.x / vel.y;
						ball.y = 0;
						vel.y *= -1;
						newBall.y *= -1;
						playSound(wallMsg, -1);
					}
					continue;
				}
				if (newBall.x < 0) {
					ball.y -= ball.x * vel.y / vel.x;
					ball.x = 0;
					vel.x *= -1;
					newBall.x *= -1;
					playSound(wallMsg, -1);
					break;
				}
				if (newBall.y < 0) {
					ball.x -= ball.y * vel.x / vel.y;
					ball.y = 0;
					vel.y *= -1;
					newBall.y *= -1;
					playSound(wallMsg, -1);
					break;
				}
			} else if (vel.x > 0 && vel.y < 0) {
				if (hitBlock())
					playSound(brickMsg, -1);
				if (newBall.x > maxWidth) {
					vel.x *= -1;
					newBall.x = 2 * (maxWidth) - newBall.x;
					playSound(wallMsg, -1);
					continue;
				}
				if (newBall.y < 0) {
					vel.y *= -1;
					newBall.y *= -1;
					playSound(wallMsg, -1);
					continue;
				}
			} else if (vel.x < 0 && vel.y > 0) {
				if (hitBlock()) // was DL
					playSound(brickMsg, -1);
				if (newBall.x < 0) {
					vel.x *= -1;
					newBall.x *= -1;
					playSound(wallMsg, -1);
					continue;
				}
				if (newBall.y > maxHeight) {
					if (level > 1) {
						synchronized (countMutex) {
							if (count == 0) {
								count++;
								onLose();
								resetLevel();
								return;
							}
						}
					}
					vel.y *= -1;
					newBall.y = 2 * (maxHeight) - newBall.y;
					// System.out.println("y: " + newBall.y);
					continue;
				}
			} else { // (vel.x > 0 && vel.y > 0)
				if (nextHitDR()) { // hitBlockDR())
					playSound(brickMsg, -1);
					// System.out.println("hit");
					// paused = true;
				}
				// if (newBall.x > maxWidth) {
				// vel.x *= -1;
				// newBall.x = 2 * (maxWidth) - newBall.x;
				// playSound(wallMsg, -1);
				// continue;
				// }
				// if (newBall.y > maxHeight) {
				// if (level > 1) {
				// synchronized (countMutex) {
				// if (count == 0) {
				// count++;
				// onLose();
				// resetLevel();
				// return;
				// }
				// }
				// }
				// vel.y *= -1;
				// newBall.y = 2 * (maxHeight) - newBall.y;
				// playSound(wallMsg, -1);
				// // System.out.println("gameHeight-1: " + (maxHeight) + ", newBall.y: " +
				// // newBall.y);
				// continue;
				// }
			}
			break;
		}

		ball.x = newBall.x;
		ball.y = newBall.y;
		if (blockCnt <= 0) {
			synchronized (countMutex) {
				if (count == 0) {
					count++;
					onWin();
					resetLevel();
					return;
				}
			}
			return;
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
		// g2.setColor(Color.red);
		// g2.fillRect(200f, 200f, 40f, 40f);
		// Rectangle2D rect = new Rectangle2D.Double(100, 100, 200, 100);
		// g2.draw(rect);

		// g2.draw(new Line2D.Float(21.50f, 132.50f, 459.50f, 132.50f));
		// g2.setColor(Color.yellow);
		// g2.draw(new Line2D.Float(31.50f, 132.70f, 44.50f, 132.70f));
		// g2.setColor(Color.white);
		// g2.drawLine(44, 133, 54, 133);
		// g2.draw(new Line2D.Float(54f, 132.70f, 64.50f, 132.10f));
		// g2.draw(new Rectangle2D.Float(54f, 134.70f, 100f, 0f));
		// g.setColor(Color.blue);
		// g.fillRect(54, 136, 100, 5);
		// g.setColor(Color.white);
		// g.drawLine(54, 135, 40, 135);
		// g.drawLine(54, 140, 40, 140);

		g.setFont(new Font("Algerian", Font.BOLD, 15));
		g.setColor(Color.white);
		g.drawString("Level: " + level + " Highscore: " + highScore, 5, 15);

		g.setColor(Color.blue);
		g.fillRect(player.x, player.y, player.width, player.height);

		g.setColor(Color.green);
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
			g.setColor(Color.white);
			int startY = padTop + blockRows * (blockHeight + padRow) + 10;
			int height = 20;
			g.drawString("Left/Right Arrows or A/D: move paddle left/right", 20, startY);
			startY += height;
			g.drawString("R: Reset Level", 20, startY);
			startY += height;
			g.drawString("Q: Quit, +: Speed up, -: Slow down", 20, startY);

			if (soundPossible) {
				startY += height;
				g.drawString("M: Mute", 20, startY);
			}

			startY += height;
			g.drawString("K: toggle Mouse/Keyboard", 20, startY);
			startY += height;
			g.drawString("P or Space: toggle pause", 20, startY);
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

		resetLevel();
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
				System.out.println("count: " + count);
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

	public void mouseDragged(MouseEvent e) {
		// update the label to show the point
		// through which point mouse is dragged
		// label1.setText("mouse is dragged through point "
		// + e.getX() + " " + e.getY());
	}

	// invoked when the cursor is moved from
	// one point to another within the component
	public void mouseMoved(MouseEvent e) {
		// update the label to show the point to which the cursor moved
		// label2.setText("mouse is moved to point "
		// + e.getX() + " " + e.getY());
		player.x = mouseWidth * e.getX() / screenWidth;
	}
}

class Distances {
	WallDist[] wall = new WallDist[2]; // 0-1: horz, vert
	BlockDist[] block = new BlockDist[4]; // 0-3: LL, LR horz hit, LR vert hit, UR

	Distances() {
		for (int i = 0; i < wall.length; i++) {
			wall[i] = new WallDist();
		}
		for (int i = 0; i < block.length; i++) {
			block[i] = new BlockDist();
		}
	}

	enum WallHit {
		horzWall,
		vertWall,
	}

	enum BlockHit {
		horzBrickLeft,
		horzBrickRight,
		vertBrickTop,
		vertBrickBottom,
	}

}

class WallDist {
	double dist = Double.MAX_VALUE;
	int ballX;
	int ballY;
}

class BlockDist {
	double dist = Double.MAX_VALUE;
	// double vert = Double.MAX_VALUE;
	int blockRow;
	int blockCol;
	int ballX;
	int ballY;

	// double distToHorzBlck = Float.MAX_VALUE;
	// double distToVertBlck = Float.MAX_VALUE;
}
