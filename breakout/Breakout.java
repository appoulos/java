import java.awt.*;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

// import java.awt.Graphics2D;
// import java.awt.geom.Rectangle2D;

class Block {
	Point point;
	boolean alive;
	int hits;
	Color color;

	Block(Point p, Color c, int h) {
		alive = true;
		point = p;
		color = c;
		hits = h;
	}
}

class Dist {
	float dist = Float.POSITIVE_INFINITY;
	int blockRow = -1;
	int blockCol = -1;
	float ballX;
	float ballY;
	float newBallX;
	float newBallY;
}

public class Breakout extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	// private Rectangle ball = new Rectangle(); // a rectangle that represents the
	// ball
	public Rectangle2D.Float ball = new Rectangle2D.Float(); // a rectangle that
	// represents the ball
	// private Point nextCalc = new Point();

	private int level = 1;
	private int highScore = 1;

	private boolean left, right; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer
	private boolean paused; // the update timer

	private final int dialogDelay = 2000;
	private final int cheatLevels = 1; // Number of levels to have no game over

	private static int frameRate = 60; // roughly frame rate per second

	private final float velStartX = 1f; // start velocity roughly frame rate per second
	private final float velStartY = 3f; // start velocity roughly frame rate per second

	private Point2D.Float vel = new Point2D.Float(); // velocity of ball
	// private Point velSign = new Point(); // velocity of ball
	private Point2D.Float newBall = new Point2D.Float(); // ball.x + vel.x, ball.y + vel.y);

	private final int ballSize = 7; // ODD ball size
	private final int otherEdge = ballSize - 1; // ball size
	private final int leftEdge = 0; // ball size
	private final int rightEdge = ballSize - 1; // ball size
	private final int upperEdge = 0; // ball size
	private final int lowerEdge = ballSize - 1; // ball size

	private final int blockRows = 4;
	private final int blockCols = 10;
	private final int blockWidth = 40;
	private final int blockHeight = 15;
	private final int padCol = 2; // padding between columns
	private final int padRow = 2; // padding between rows
	private final int padTop = 30; // padding above blocks
	private final int padMiddle = 150; // padding between blocks and paddle
	private final int padBottom = 20; // padding below paddle
	private Block[][] blocks = new Block[blockRows][blockCols];
	private int blockCnt = blockRows * blockCols;
	// private final boolean blockColNeighbors = size > padCol + 2; // (blockWidth +
	// padCol) + 1;
	// private final boolean blockRowNeighbors = size > padRow + 2; // (blockWidth +
	// padCol) + 1;

	private final int ballStartX = 90;
	private final int ballStartY = padTop + blockRows * (blockHeight + padRow) + 10;

	private final int ballMiddle = ballSize / 2; // ballSize must be odd
	private final int playerW = 48 - ballMiddle; // pick number divisible by playerSegments - ballMiddle
	private final int playerH = 10;

	// the width of the game area
	private final int gameWidth = padCol + blockCols * (blockWidth + padCol);
	// the height of the game area
	private final int gameHeight = padTop + blockRows * (blockHeight + padRow) + padMiddle + playerH + padBottom;

	private final int playerSegments = 6;
	// private final int playerSegment = playerW / 2 / playerSegments;

	// private Point[] bounces = new Point[playerSegments];
	private Point2D.Float[] bounces = new Point2D.Float[playerSegments];
	private final int playerStartX = 10;
	private final int playerStartY = gameHeight - padBottom - playerH;

	private final int maxWidth = gameWidth - 1 - ballSize;
	private final int maxHeight = gameHeight - 1 - ballSize;
	// max player.x position
	private final int mouseWidth = gameWidth - playerW;

	private static JLabel dialogLabel;
	private static JFrame frame;
	private static JDialog dialog;
	private static int screenWidth;
	private boolean keyboard = true;

	final int horzWall = 0;
	final int vertWall = 1;
	final int horzBlockLeft = 2;
	final int horzBlockRight = 3;
	final int vertBlockBottom = 4;
	final int vertBlockTop = 5;

	// Distance calcs
	// static Distances dist = new Distances();
	final static String[] distNames = {
			"horzWall       ",
			"vertWall       ",
			"horzBlockLeft  ",
			"horzBlockRight ",
			"vertBlockBottom",
			"vertBlockTop   ",
	};
	static Dist[] dists = new Dist[distNames.length];

	// MIDI
	static Receiver rcvr;
	static Synthesizer synth = null;
	static ShortMessage paddleMsg = new ShortMessage();
	static ShortMessage wallMsg = new ShortMessage();
	static ShortMessage loseMsg = new ShortMessage();
	static ShortMessage brickMsg = new ShortMessage();
	static ShortMessage paddleOffMsg = new ShortMessage();
	static ShortMessage wallOffMsg = new ShortMessage();
	// static ShortMessage brickOffMsg = new ShortMessage();
	static boolean soundPossible = false;
	static boolean mute = true;
	static float frameTimeuSec = 0f;
	static float frameDist = 0f;
	static float currDist = 0f;

	void playSound(ShortMessage msg, int time) {
		if (!mute) {
			long t = synth.getMicrosecondPosition();
			rcvr.send(msg, -1); // time in microseconds
			rcvr.send(paddleOffMsg, t + time); // time in microseconds
			rcvr.send(wallOffMsg, t + time); // time in microseconds
		}
	}

	private void blockRemove(int r, int c) {
		if (blocks[r][c].alive) {
			// playSound(brickMsg, (int) (currDist / frameDist * frameTimeuSec));
			blocks[r][c].hits--;
			if (blocks[r][c].hits <= 0) {
				blocks[r][c].alive = false;
				blockCnt--;
			}
		}
	}

	private void lose() {
		if (level > cheatLevels) {
			synchronized (countMutex) {
				if (count == 0) {
					count++;
					onLose();
					resetLevel();
					return;
				}
			}
		}
	}

	String printBall() {
		return "ball: (" + ball.x + ", " + ball.y + ") newBall: (" + newBall.x + ", " + newBall.y + ")";
	}

	void setSoundParameters() {
		frameDist = (float) Math.sqrt(vel.x * vel.x + vel.y * vel.y);
		frameTimeuSec = 1_000_000 / frameRate;
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
			loseMsg.setMessage(ShortMessage.NOTE_ON, 0, 37, noteVelocity);
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
		dialog.setBounds(125, 125, 200, 70);
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

	// Method that is called by the timer framerate times per second (roughly)
	// Most games go through states - updating objects, then drawing them
	public void actionPerformed(ActionEvent e) {
		// long st = System.currentTimeMillis();
		update();
		// long st2 = System.currentTimeMillis();
		repaint();
		// long st3 = System.currentTimeMillis();
		// long t1 = st2 - st;
		// long t2 = st3 - st2;
		// if (t1 != 0 || t2 != 0) {
		// System.out.println("update: " + t1 + ", paint: " + t2);
		// }
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
			if (frameRate >= 2) {
				frameRate /= 2;
				setSoundParameters();
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (keyCode == KeyEvent.VK_EQUALS || keyCode == KeyEvent.VK_PLUS) {
			if (frameRate <= Integer.MAX_VALUE / 2) {
				frameRate *= 2;
				setSoundParameters();
			}
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
		for (int i = 0; i < dists.length; i++) {
			dists[i] = new Dist();
		}
		int ignoreDeadCode = 0;
		if (ballSize > blockWidth + 1 + ignoreDeadCode) {
			System.out.println("ball size cannot exeed blockWidth + 1");
			System.exit(1);
		}
		if (ballSize > blockHeight + 1 + ignoreDeadCode) {
			System.out.println("ball size cannot exeed blockHeight + 1");
			System.exit(1);
		}
		if (ballSize % 2 != 1 + ignoreDeadCode) {
			System.out.println("ball size must be odd");
			System.exit(1);
		}
		if (playerW % (playerSegments - ballMiddle) != 0 + ignoreDeadCode) {
			System.out.println("playerW must be divisible by (playerSegments-ballMiddle)");
			System.exit(1);
		}

		// for (int i=0;i<bounces.length;i++){
		bounces[0] = new Point2D.Float(-3, -1);
		bounces[1] = new Point2D.Float(-2, -2);
		bounces[2] = new Point2D.Float(-1, -3);
		bounces[3] = new Point2D.Float(1, -3);
		bounces[4] = new Point2D.Float(2, -2);
		bounces[5] = new Point2D.Float(3, -1);

		// bounces[0] = new Point(-6, -2);
		// bounces[1] = new Point(-4, -4);
		// bounces[2] = new Point(-2, -6);
		// bounces[3] = new Point(2, -6);
		// bounces[4] = new Point(4, -4);
		// bounces[5] = new Point(6, -2);
		//
		// bounces[0] = new Point2D.Float(-12, -4);
		// bounces[1] = new Point2D.Float(-8, -8);
		// bounces[2] = new Point2D.Float(-4, -12);
		// bounces[3] = new Point2D.Float(4, -12);
		// bounces[4] = new Point2D.Float(8, -8);
		// bounces[5] = new Point2D.Float(12, -4);

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
		ball = new Rectangle2D.Float(ballStartX, ballStartY, ballSize, ballSize);
		vel.x = velStartX * (1 + level * 0.2f);
		vel.y = velStartY * (1 + level * 0.2f);

		setSoundParameters();
		// int framesTillNextCalc = (player.y - 1 - size - ball.y) / vel.y;
		// nextCalc.x = ball.x + vel.x * framesTillNextCalc;
		// nextCalc.y = ball.y + vel.y * framesTillNextCalc;
		// System.out.println(nextCalc + ", " + framesTillNextCalc);

		blockCnt = blockRows * blockCols;
		Color color = Color.pink;
		int maxHits = 1;
		for (int r = 0; r < blockRows; r++) {
			switch (r) {
				case 0:
					color = Color.RED;
					maxHits = 3;
					break;
				case 1:
					color = Color.YELLOW;
					maxHits = 2;
					break;
				case 2:
					color = Color.ORANGE;
					maxHits = 1;
					break;
				case 3:
					color = Color.BLUE;
					maxHits = 1;
					break;
			}
			for (int c = 0; c < blockCols; c++) {
				blocks[r][c] = new Block(
						new Point(padCol + (padCol + blockWidth) * c, padTop + (padRow + blockHeight) * r),
						color, maxHits);
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

	public int blockCol(int sign, float x) {
		if (sign == 1)
			return blockColPos(x);
		else
			return blockColNeg(x);
	}

	public int blockRow(int sign, float y) {
		if (sign == 1)
			return blockRowPos(y);
		else
			return blockRowNeg(y);
	}

	public int blockColPos(float x) {
		x = (x - padCol);
		if (x < 0)
			return -1;
		// make x = col
		x /= (blockWidth + padCol);
		if (x >= blockCols)
			return blockCols - 1;
		return (int) x;
	}

	public int blockColNeg(float x) {
		x = (x - padCol - blockWidth);
		if (x < 0)
			return -1;
		// make x = col
		x /= (blockWidth + padCol);
		if (x >= blockCols)
			return blockCols - 1;
		return (int) x;
	}

	// new Point(padCol + (padCol + blockWidth) * c, padTop + (padRow + blockHeight)
	// * r),
	public int blockRowPos(float y) {
		y = (y - padTop);
		if (y < 0)
			return -1;
		// make y = row
		y /= (blockHeight + padRow);
		if (y >= blockRows)
			return blockRows - 1;
		return (int) y;
	}

	public int blockRowNeg(float y) {
		y = (y - padTop - blockHeight);
		if (y < 0)
			return -1;
		// make y = row
		y /= (blockHeight + padRow);
		if (y >= blockRows)
			return blockRows - 1;
		return (int) y;
	}

	public boolean nextHit2() {
		boolean retLose = false; // return true if game over
		float m; // = (float) vel.y / vel.x;

		float min;
		boolean foundHit;
		float ballx = ball.x;
		float bally = ball.y;
		boolean wallHit = false;
		int blockEdgeX, blockEdgeY;
		ret: do { // bounces
			m = (float) vel.y / vel.x;
			wallHit = false;
			boolean blockHit = false;
			min = Float.POSITIVE_INFINITY;
			for (int i = 0; i < dists.length; i++) {
				dists[i].dist = Float.POSITIVE_INFINITY;
			}
			foundHit = false;
			int signX, signY;
			boolean posX, posY;
			int boundaryX, boundaryY;
			int edgeX, edgeY;
			int rowBeg, rowEnd;
			int colBeg, colEnd;
			int revEdgeX, revEdgeY;
			if (vel.x > 0) {
				signX = 1;
				posX = true;
				boundaryX = maxWidth;
				edgeX = rightEdge;
				revEdgeX = leftEdge;
				blockEdgeX = 0;
				colBeg = blockColPos(ball.x + edgeX) + 1;
				colEnd = blockColPos(newBall.x + edgeX) + 1;
			} else {
				signX = -1;
				posX = false;
				boundaryX = 0;
				edgeX = leftEdge;
				revEdgeX = rightEdge;
				blockEdgeX = blockWidth;
				colBeg = blockColNeg(ball.x + edgeX);
				colEnd = blockColNeg(newBall.x + edgeX);
			}
			if (vel.y > 0) {
				signY = 1;
				posY = true;
				boundaryY = maxHeight;
				edgeY = lowerEdge;
				revEdgeY = upperEdge;
				rowBeg = blockRowPos(ball.y + edgeY) + 1;
				rowEnd = blockRowPos(newBall.y + edgeY) + 1;
				blockEdgeY = 0;
			} else {
				signY = -1;
				posY = false;
				boundaryY = 0;
				edgeY = upperEdge;
				revEdgeY = lowerEdge;
				rowBeg = blockRowNeg(ball.y + edgeY);
				rowEnd = blockRowNeg(newBall.y + edgeY);
				blockEdgeY = blockHeight;
			}
			// horizontal wall hit
			if (newBall.y < 0 || newBall.y > maxWidth) {
				float dy = boundaryY - ball.y;
				float dx = dy / m;
				float d = dx * dx + dy * dy;
				if (d <= min) {
					foundHit = true;
					min = d;
					dists[horzWall].dist = d;
					dists[horzWall].ballX = ball.x + dx;
					dists[horzWall].ballY = boundaryY;
				}
			}
			// vertical wall hit
			if (newBall.x < 0 || newBall.x > maxWidth) {
				float dx = boundaryX - ball.x;
				float dy = dx * m;
				float d = dx * dx + dy * dy;
				if (d <= min) {
					foundHit = true;
					min = d;
					dists[vertWall].dist = d;
					dists[vertWall].ballX = boundaryX;
					dists[vertWall].ballY = ball.y + dy;
				}
			}
			int r = rowBeg;
			while (r != rowEnd) {
				// for (int r = rowBeg; r < rowEnd; r++) {
				// NOTE: velDownRight ballLowerRightEdge hit horizontal block check
				float hitY = blocks[r][0].point.y + blockEdgeY;
				float hitX = ((hitY - (ball.y + edgeY)) / m + (ball.x + edgeX));
				int bc = blockColPos(hitX);
				float d = -1;
				boolean hit = false;
				if (bc > -1 && hitX >= blocks[r][bc].point.x && hitX < blocks[r][bc].point.x + blockWidth
						&& blocks[r][bc].alive) {
					float dx = hitX - (ball.x + edgeX);
					float dy = hitY - (ball.y + edgeY);
					d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						hit = true;
						min = d;
						dists[horzBlockRight].dist = d;
						dists[horzBlockRight].blockRow = r;
						dists[horzBlockRight].blockCol = bc;
						dists[horzBlockRight].ballX = hitX - (edgeX == 0 ? 0 : ballSize);
						dists[horzBlockRight].ballY = hitY - (edgeY == 0 ? 0 : ballSize);
					}
				}
				// NOTE: velDownRight ballLowerLeftEdge hit horizontal block check
				hitX -= signX * otherEdge;
				if (hitX - 0 > padCol) {
					int bc2 = blockColPos(hitX - 0);
					if (!(hit && bc2 == bc) && bc2 > -1 && hitX >= blocks[r][bc2].point.x
							&& hitX < blocks[r][bc2].point.x + blockWidth
							&& blocks[r][bc2].alive) {
						if (d == -1) {
							float dx = hitX - (ball.x + revEdgeX);
							float dy = hitY - (ball.y + edgeY);
							d = dx * dx + dy * dy;
						}
						if (d <= min) {
							foundHit = true;
							min = d;
							dists[horzBlockLeft].dist = d;
							dists[horzBlockLeft].blockRow = r;
							dists[horzBlockLeft].blockCol = bc2;
							dists[horzBlockLeft].ballX = hitX - (revEdgeX == 0 ? ballSize : 0); // reversed edge
							dists[horzBlockLeft].ballY = hitY - (edgeY == 0 ? 0 : ballSize);
						}
					}
				}
				r += signY;
			} // while (r != rowEnd)

			// NOTE: velDownRight ballLowerRightEdge hit vertical block check
			int c = colBeg;
			while (c != colEnd) {
				// LR hit vert block check
				float hitX = blocks[0][c].point.x + blockEdgeY;
				float hitY = ((hitX - (ball.x + edgeX)) * m + (ball.y + edgeY));
				int br = blockRowPos(hitY);
				float d = -1;
				boolean hit = false;
				if (br > -1 && hitY > blocks[br][c].point.y && hitY < blocks[br][c].point.y + blockHeight
						&& blocks[br][c].alive) {
					float dx = hitX - (ball.x + edgeX);
					float dy = hitY - (ball.y + edgeY);
					d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						hit = true;
						min = d;
						dists[vertBlockBottom].dist = d;
						dists[vertBlockBottom].blockRow = br;
						dists[vertBlockBottom].blockCol = c;
						dists[vertBlockBottom].ballX = hitX - (edgeX == leftEdge ? signX : ballSize);
						dists[vertBlockBottom].ballY = hitY - (edgeY == upperEdge ? signY : ballSize);
					}
				}

				// NOTE: velDownRight ballUpperRightEdge hit vertical block check
				hitY -= signY * otherEdge;
				if (hitY - 0 > padTop) {
					int br2 = blockRowPos(hitY);
					if (!(hit && br2 == br) && br2 > -1 && hitY >= blocks[br2][c].point.y
							&& hitY < blocks[br2][c].point.y + blockHeight
							&& blocks[br2][c].alive) { // br != br2 &&
						if (d == -1) {
							float dx = hitX - (ball.x + edgeX);
							float dy = hitY - (ball.y + revEdgeY);
							d = dx * dx + dy * dy;
						}
						if (d <= min) {
							foundHit = true;
							min = d;
							dists[vertBlockTop].dist = d;
							dists[vertBlockTop].blockRow = br2;
							dists[vertBlockTop].blockCol = c;
							dists[vertBlockTop].ballX = hitX - ballSize;
							dists[vertBlockTop].ballY = hitY;
							dists[horzBlockLeft].ballX = hitX - (edgeX == 0 ? ballSize : 0); // reversed edge
							dists[horzBlockLeft].ballY = hitY - (revEdgeY == 0 ? 0 : ballSize);
						}
					}
				}
				c += signX;
			} // while (c != colEnd)

			if (!foundHit) {
				break ret;
			}

			System.out.println("Ball dir DR");
			printDist();

			if (dists[vertWall].dist == min) {
				wallHit = true;
				if (dists[horzBlockRight].dist == min) { // LR
					blockHit = true;
					Dist bd = dists[horzBlockRight];
					blockRemove(bd.blockRow, bd.blockCol);
					vel.y *= -1;
					newBall.y = 2 * ball.y - signY * newBall.y;
				} else {
				}
				ball.x = dists[vertWall].ballX;
				ball.y = dists[vertWall].ballY;
				vel.x *= -1;
				newBall.x = 2 * ball.x - newBall.x;
			} else if ((dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min)
					&& (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min)) {
				System.out.println("hit two: reversing");
				if (dists[vertBlockTop].dist == min) {
					blockHit = true;
					System.out.println("    hit vertBlockTop");
					Dist bd = dists[vertBlockTop];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				if (dists[horzBlockLeft].dist == min) {
					blockHit = true;
					System.out.println("    hit horzBlockLeft");
					Dist bd = dists[horzBlockLeft];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				vel.x *= -1;
				vel.y *= -1;
				newBall.y = 2 * ball.y - newBall.y;
				newBall.x = 2 * ball.x - newBall.x;
			} else if (dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min) {
				if (dists[vertBlockBottom].dist == min) {
					blockHit = true;
					System.out.println("hit vertBlockBottom");
					Dist bd = dists[vertBlockBottom];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				if (dists[vertBlockTop].dist == min) {
					blockHit = true;
					System.out.println("hit vertBlockTop");
					Dist bd = dists[vertBlockTop];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				vel.x *= -1;
				newBall.x = 2 * ball.x - newBall.x;
			} else if (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min) {
				if (dists[horzBlockLeft].dist == min) {
					blockHit = true;
					System.out.println("hit horzBlockLeft");
					Dist bd = dists[horzBlockLeft];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				if (dists[horzBlockRight].dist == min) {
					blockHit = true;
					System.out.println("hit horzBlockRight");
					Dist bd = dists[horzBlockRight];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				vel.y *= -1;
				newBall.y = 2 * ball.y - newBall.y;
			} else if (dists[horzWall].dist == min) {
				wallHit = true;
				ball.x = dists[horzWall].ballX;
				ball.y = dists[horzWall].ballY;
				vel.y *= -1;
				newBall.y = 2 * ball.y - newBall.y;
				lose();
				retLose = true;
				break ret;
			}
		} while (foundHit);

		if (!retLose) {
			ball.x = newBall.x;
			ball.y = newBall.y;
		} else {
			playSound(loseMsg, (int) (currDist / frameDist * frameTimeuSec));
			if (wallHit) {
			}
		}
		return retLose;
	}

	public boolean nextHit() {
		boolean retLose = false; // return true if game over
		float m; // = (float) vel.y / vel.x;
		// float ballx = 0, bally = 0;

		float min;
		boolean foundHit;
		float ballx = ball.x;
		float bally = ball.y;
		boolean wallHit = false;
		// int cnt = 0;
		ret: do { // bounces
			m = (float) vel.y / vel.x;
			wallHit = false;
			boolean blockHit = false;
			min = Float.POSITIVE_INFINITY;
			for (int i = 0; i < dists.length; i++) {
				dists[i].dist = Float.POSITIVE_INFINITY;
			}
			// cnt++;
			// if (cnt > 2) {
			// System.out.println("################################ cnt: " + cnt);
			// break;
			// }
			foundHit = false;
			if (vel.x > 0 && vel.y > 0) {
				// ********************************* Down and Right Ball movement *************
				// horizontal wall hit
				// System.out.println("before dr: " + printBall());
				if (newBall.y > maxHeight) {
					float dy = (maxHeight - (ball.y + 0));
					float dx = dy / m;
					float d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[horzWall].dist = d;
						dists[horzWall].ballX = ball.x + dx;
						dists[horzWall].ballY = maxHeight;
					}
				}
				// vertical wall hit
				if (newBall.x > maxWidth) {
					float dx = (maxWidth - ball.x);
					float dy = dx * m;
					float d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[vertWall].dist = d;
						dists[vertWall].ballX = maxWidth;
						dists[vertWall].ballY = ball.y + dy;
						// System.out.println("a. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
						// newBall.x: " + newBall.x
						// + ", newBall.y: " + newBall.y);
						// System.out.println(
						// "ballX: " + maxWidth + ", ball.y: " + ball.y + ", dx: " + (int) dx + ", dy: "
						// + (int) dy);
					}
				}
				int rowBeg = blockRowPos(ball.y + lowerEdge) + 1;
				int rowEnd = blockRowPos(newBall.y + lowerEdge) + 1;
				for (int r = rowBeg; r < rowEnd; r++) {
					// NOTE: velDownRight ballLowerRightEdge hit horizontal block check
					float hitY = blocks[r][0].point.y;
					float hitX = ((hitY - (ball.y + lowerEdge)) / m + (ball.x + rightEdge));
					int bc = blockColPos(hitX);
					float d = -1;
					boolean hit = false;
					if (bc > -1 && hitX >= blocks[r][bc].point.x && hitX < blocks[r][bc].point.x + blockWidth
							&& blocks[r][bc].alive) {
						float dx = hitX - (ball.x + rightEdge);
						float dy = hitY - (ball.y + lowerEdge);
						d = dx * dx + dy * dy;
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							dists[horzBlockRight].dist = d;
							dists[horzBlockRight].blockRow = r;
							dists[horzBlockRight].blockCol = bc;
							dists[horzBlockRight].ballX = hitX - ballSize;
							dists[horzBlockRight].ballY = hitY - ballSize;
							// dists[horzBlockRight].newBallX = (ball.x + leftEdge)
							// - (vel.x - (hitX - (ball.x + rightEdge)));
							// dists[horzBlockRight].newBallY = (ball.y + upperEdge)
							// - (vel.y - (hitY - (ball.y + lowerEdge)));
						}
					}
					// NOTE: velDownRight ballLowerLeftEdge hit horizontal block check
					hitX -= otherEdge;
					if (hitX - 0 > padCol) {
						int bc2 = blockColPos(hitX - 0);
						if (!(hit && bc2 == bc) && bc2 > -1 && hitX >= blocks[r][bc2].point.x
								&& hitX < blocks[r][bc2].point.x + blockWidth
								&& blocks[r][bc2].alive) {
							// System.out.println("************************************************ a");
							// if (bc2 > -1 && bc != bc2 && blocks[r][bc2].alive) { // efficient for blocks
							// all the way to wall
							if (d == -1) {
								float dx = hitX - (ball.x + leftEdge);
								float dy = hitY - (ball.y + lowerEdge);
								d = dx * dx + dy * dy;
							}
							// System.out.println("************************************************ d: " +
							// d);
							if (d <= min) {
								foundHit = true;
								// System.out.println("************************************************ hit");
								min = d;
								dists[horzBlockLeft].dist = d;
								dists[horzBlockLeft].blockRow = r;
								dists[horzBlockLeft].blockCol = bc2;
								dists[horzBlockLeft].ballX = hitX;
								dists[horzBlockLeft].ballY = hitY - ballSize;
							}
						}
					}
				}
				// NOTE: velDownRight ballLowerRightEdge hit vertical block check
				int colBeg = blockColPos(ball.x + rightEdge) + 1;
				int colEnd = blockColPos(newBall.x + rightEdge) + 1;
				for (int c = colBeg; c < colEnd; c++) {
					// System.out.println("colbeg-end: " + colBeg + "-" + colEnd);
					// System.out.println(colBeg + " " + colEnd + " " + ball.x + " " + newBall.x + "
					// "
					// + (colBeg * (blockWidth + padCol) + padCol));
					// LR hit vert block check
					float hitX2 = blocks[0][c].point.x;
					float hitY2 = ((hitX2 - (ball.x + rightEdge)) * m + (ball.y + lowerEdge));
					// if (hitY2 > newBall.y + lowerEdge) {
					// break;
					// }
					int br = blockRowPos(hitY2);
					float d = -1;
					boolean hit = false;
					if (br > -1 && hitY2 > blocks[br][c].point.y && hitY2 < blocks[br][c].point.y + blockHeight
							&& blocks[br][c].alive) {
						float dx = hitX2 - (ball.x + rightEdge);
						float dy = hitY2 - (ball.y + lowerEdge);
						d = dx * dx + dy * dy;
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							dists[vertBlockBottom].dist = d;
							dists[vertBlockBottom].blockRow = br;
							dists[vertBlockBottom].blockCol = c;
							dists[vertBlockBottom].ballX = hitX2 - ballSize;
							dists[vertBlockBottom].ballY = hitY2 - ballSize;
						}
					}
					// NOTE: velDownRight ballUpperRightEdge hit vertical block check
					hitY2 -= otherEdge;
					if (hitY2 - 0 > padTop) {
						// UR hit vert block check
						int br2 = blockRowPos(hitY2);
						// System.out.println("hitXY2: " + hitX2 + "," + hitY2 + " br2: " + br2 + " c: "
						// + c);
						if (!(hit && br2 == br) && br2 > -1 && hitY2 >= blocks[br2][c].point.y
								&& hitY2 < blocks[br2][c].point.y + blockHeight
								&& blocks[br2][c].alive) { // br != br2 &&
							if (d == -1) {
								float dx = hitX2 - (ball.x + rightEdge);
								float dy = hitY2 - (ball.y + upperEdge);
								d = dx * dx + dy * dy;
							}
							// if (d > 15) {
							// paused = true;
							// }
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[vertBlockTop].dist = d;
								dists[vertBlockTop].blockRow = br2;
								dists[vertBlockTop].blockCol = c;
								dists[vertBlockTop].ballX = hitX2 - ballSize;
								dists[vertBlockTop].ballY = hitY2;
							}
						}
					}
				}

				if (!foundHit) {
					break ret;
				}

				System.out.println("Ball dir DR");
				printDist();

				if (dists[vertWall].dist == min) {
					wallHit = true;
					if (dists[horzBlockRight].dist == min) { // LR
						blockHit = true;
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						vel.y *= -1;
						// newBall.x = ball.x - (newBall.x - ball.x);
						newBall.y = 2 * ball.y - newBall.y;
					} else {
					}
					ball.x = dists[vertWall].ballX;
					ball.y = dists[vertWall].ballY;
					vel.x *= -1;
					// System.out.println("b. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
					newBall.x = 2 * ball.x - newBall.x;
					// System.out.println("c. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
				} else if ((dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min)
						&& (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min)) {
					System.out.println("hit two: reversing");
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("    hit vertBlockTop");
						Dist bd = dists[vertBlockTop];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockLeft].dist == min) {
						blockHit = true;
						System.out.println("    hit horzBlockLeft");
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min) {
					if (dists[vertBlockBottom].dist == min) {
						blockHit = true;
						System.out.println("hit vertBlockBottom");
						Dist bd = dists[vertBlockBottom];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("hit vertBlockTop");
						Dist bd = dists[vertBlockTop];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min) {
					if (dists[horzBlockLeft].dist == min) {
						blockHit = true;
						System.out.println("hit horzBlockLeft");
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockRight].dist == min) {
						blockHit = true;
						System.out.println("hit horzBlockRight");
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
				} else if (dists[horzWall].dist == min) { // Possible to have padTop small enough to hit top and
															// block
					wallHit = true;
					ball.x = dists[horzWall].ballX;
					ball.y = dists[horzWall].ballY;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					lose();
					retLose = true;
					break ret;
				} else { // no hits
					// ball.x = newBall.x;
					// ball.y = newBall.y;
				}
				// System.out.println("after dr: " + printBall());
			} else if (vel.x > 0 && vel.y < 0) {
				// *********************************** Up and Right Ball movement *************
				// horizontal wall hit
				if (newBall.y < 0) {
					float dy = (0 - (ball.y + 0));
					float dx = dy / m;
					float d = dx * dx + dy * dy;
					// System.out.println(dx + "," + dy + " " + d);
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[horzWall].dist = d;
						dists[horzWall].ballX = ball.x + dx;
						dists[horzWall].ballY = 0;
					}
				}
				// vertical wall hit
				if (newBall.x > maxWidth) {
					float dx = (maxWidth - ball.x);
					float dy = dx * m;
					float d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[vertWall].dist = d;
						dists[vertWall].ballX = maxWidth;
						dists[vertWall].ballY = ball.y + dy;
						// System.out.println("a. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
						// newBall.x: " + newBall.x
						// + ", newBall.y: " + newBall.y);
						// System.out.println(
						// "ballX: " + maxWidth + ", ball.y: " + ball.y + ", dx: " + dx + ", dy: "
						// + dy);
					}
				}
				int rowBeg = blockRowNeg(ball.y + 0);
				int rowEnd = blockRowNeg(newBall.y + 0);
				// System.out.println("Beg,End: " + rowBeg + "," + rowEnd);
				for (int r = rowBeg; r > rowEnd; r--) {
					// UR hit horiz block check
					float hitY = blocks[r][0].point.y + blockHeight;
					float hitX = ((hitY - (ball.y + upperEdge)) / m + (ball.x + rightEdge));
					int bc = blockColPos(hitX);
					float d = -1;
					boolean hit = false;
					if (bc > -1 && hitX >= blocks[r][bc].point.x && hitX < blocks[r][bc].point.x + blockWidth
							&& blocks[r][bc].alive) {
						float dx = hitX - (ball.x + rightEdge);
						float dy = hitY - (ball.y + upperEdge);
						d = dx * dx + dy * dy;
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							dists[horzBlockRight].dist = d;
							dists[horzBlockRight].blockRow = r;
							dists[horzBlockRight].blockCol = bc;
							dists[horzBlockRight].ballX = hitX;
							dists[horzBlockRight].ballY = hitY - 0;
						}
					}
					// UL hit horiz block check
					hitX -= otherEdge;
					if (hitX - 0 > padCol) {
						int bc2 = blockColPos(hitX - 0);
						if (!(hit && bc2 == bc) && bc2 > -1 && hitX >= blocks[r][bc2].point.x
								&& hitX < blocks[r][bc2].point.x + blockWidth
								&& blocks[r][bc2].alive) {
							// if (bc2 > -1 && bc != bc2 && blocks[r][bc2].alive) { // efficient for blocks
							// all the way to wall
							if (d == -1) {
								float dx = hitX - (ball.x + leftEdge);
								float dy = hitY - (ball.y + upperEdge);
								d = dx * dx + dy * dy;
							}
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[horzBlockLeft].dist = d;
								dists[horzBlockLeft].blockRow = r;
								dists[horzBlockLeft].blockCol = bc2;
								dists[horzBlockLeft].ballX = hitX;
								dists[horzBlockLeft].ballY = hitY - 0;
							}
						}
					}
				}
				int colBeg = blockColPos(ball.x + rightEdge) + 1;
				int colEnd = blockColPos(newBall.x + rightEdge) + 1;
				for (int c = colBeg; c < colEnd; c++) {
					// System.out.println(colBeg + " " + colEnd + " " + ball.x + " " + newBall.x + "
					// "
					// + (colBeg * (blockWidth + padCol) + padCol));
					// UR hit vert block check
					float hitX2 = blocks[0][c].point.x;
					float hitY2 = ((hitX2 - (ball.x + rightEdge)) * m + ball.y + 0);
					int br = blockRowPos(hitY2); // Neg???
					float d = -1;
					boolean hit = false;
					if (br > -1 && hitY2 >= blocks[br][c].point.y && hitY2 < blocks[br][c].point.y + blockHeight
							&& blocks[br][c].alive) {
						float dx = hitX2 - (ball.x + rightEdge);
						float dy = hitY2 - (ball.y + upperEdge);
						d = dx * dx + dy * dy;
						// if (d > 15) {
						// printDist();
						// System.out.println("1. problem d: " + d + " cnt: " + cnt);
						// paused = true;
						// }
						if (d <= min) {
							System.out.println(
									"zzz" + blocks[br][c].point.y + " " + ball.y + " " + newBall.y + " " + hitY2 + " "
											+ br);
							/*
							 * zzz ball: 200.0,24.0
							 * zzz30 24.0 17.0 30.0
							 * Ball dir UR
							 * dist wall vertBlockTop : 72.00 ballX: 206.0 ballY: 30.0 Row: 0 Col: 5
							 * hit vertBlockTop
							 */
							foundHit = true;
							hit = true;
							min = d;
							dists[vertBlockTop].dist = d;
							dists[vertBlockTop].blockRow = br;
							dists[vertBlockTop].blockCol = c;
							dists[vertBlockTop].ballX = hitX2 - ballSize;
							dists[vertBlockTop].ballY = hitY2;
						}
					}
					hitY2 += otherEdge;
					// System.out.println("hitY2: " + hitY2);
					if (hitY2 - 0 < padTop + blockRows * (blockHeight + padRow)) {
						// LR hit vert block check
						int br2 = blockRowPos(hitY2 - 0); // Neg???
						// System.out.println("br2: " + br2);
						if (!(hit && br2 == br) && br2 > -1 && hitY2 > blocks[br2][c].point.y
								&& hitY2 < blocks[br2][c].point.y + blockHeight
								&& blocks[br2][c].alive) { // br != br2 &&
							if (d == -1) {
								float dx = hitX2 - (ball.x + rightEdge);
								float dy = hitY2 - (ball.y + lowerEdge);
								d = dx * dx + dy * dy;
							}
							// if (d > 15) {
							// printDist();
							// System.out.println("2. problem d: " + d + " cnt: " + cnt);
							// System.out
							// .println("dx: " + (hitX2 - (ball.x + rightEdge)) + " dy: "
							// + (hitY2 - (ball.y + lowerEdge)));
							// System.out.println("br2: " + br2 + " c: " + c);
							// paused = true;
							// }
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[vertBlockBottom].dist = d;
								dists[vertBlockBottom].blockRow = br2;
								dists[vertBlockBottom].blockCol = c;
								dists[vertBlockBottom].ballX = hitX2 - ballSize;
								dists[vertBlockBottom].ballY = hitY2;
							}
						}
					}
				}

				if (!foundHit) {
					break ret;
				}

				System.out.println("Ball dir UR");
				printDist();

				if (dists[vertWall].dist == min) {
					wallHit = true;
					if (dists[horzBlockRight].dist == min) { // UR
						blockHit = true;
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						vel.y *= -1;
						newBall.y = 2 * ball.y + newBall.y;
					} else {
					}
					ball.x = dists[vertWall].ballX;
					ball.y = dists[vertWall].ballY;
					vel.x *= -1;
					// System.out.println("b. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
					newBall.x = 2 * ball.x - newBall.x;
					// System.out.println("c. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
				} else if ((dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min)
						&& (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min)) {
					System.out.println("hit two: reversing");
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("    hit vertBlockBottom");
						Dist bd = dists[vertBlockBottom];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockLeft].dist == min) {
						blockHit = true;
						System.out.println("    hit horzBlockLeft");
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					vel.y *= -1;
					newBall.y = 2 * ball.y + newBall.y;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min) {
					if (dists[vertBlockBottom].dist == min) {
						blockHit = true;
						System.out.println("    hit vertBlockBottom");
						Dist bd = dists[vertBlockBottom];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("    hit vertBlockTop");
						Dist bd = dists[vertBlockTop];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min) {
					if (dists[horzBlockLeft].dist == min) {
						blockHit = true;
						System.out.println("    hit horzBlockLeft");
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockRight].dist == min) {
						blockHit = true;
						System.out.println("    hit horzBlockRight");
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					// System.out.println(ball.x + "," + ball.y + " " + newBall.x + "," +
					// newBall.y);
				} else if (dists[horzWall].dist == min) { // Possible to have padTop small enough to hit top and
															// block
					wallHit = true;
					ball.x = dists[horzWall].ballX;
					ball.y = dists[horzWall].ballY;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
				} else { // no hits
					// ball.x = newBall.x;
					// ball.y = newBall.y;
				}

			} else if (vel.x < 0 && vel.y > 0) {

				// ********************************* Down and Left Ball movement *************
				// horizontal wall hit
				if (newBall.y > maxHeight) {
					float dy = (maxHeight - (ball.y + 0));
					float dx = dy / m;
					float d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[horzWall].dist = d;
						dists[horzWall].ballX = ball.x + dx;
						dists[horzWall].ballY = maxHeight;
					}
				}
				// vertical wall hit
				if (newBall.x < 0) {
					float dx = (0 - ball.x);
					float dy = dx * m;
					float d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[vertWall].dist = d;
						dists[vertWall].ballX = 0;
						dists[vertWall].ballY = ball.y + dy;
						// System.out.println("a. ball.x: " + ball.x + "," + ball.y + " newBall: " +
						// newBall.x
						// + "," + newBall.y + " corrected: 0," + (ball.y + dy));
						// System.out.println(
						// "ballX: " + maxWidth + ", ball.y: " + ball.y + ", dx: " + dx + ", dy: "
						// + dy);
					}
				}
				int rowBeg = blockRowPos(ball.y + lowerEdge) + 1;
				int rowEnd = blockRowPos(newBall.y + lowerEdge) + 1;
				// System.out.println("qqq rows: " + rowBeg + "-" + rowEnd + " ball: " + ball.x
				// + "," + ball.y
				// + " newBall: " + newBall.x + "-" + newBall.y);
				for (int r = rowBeg; r < rowEnd; r++) {
					// LL hit horiz block check
					float hitY = blocks[r][0].point.y;
					float hitX = ((hitY - (ball.y + lowerEdge)) / m + (ball.x + leftEdge));
					int bc = blockColPos(hitX);
					float d = -1;
					boolean hit = false;
					// System.out.println("d. ball.x: " + ball.x + "," + ball.y + " newBall: " +
					// newBall.x
					// + "," + newBall.y + " hitXY: " + hitX + "," + hitY + " rows: " + rowBeg + "-"
					// + rowEnd
					// + " r: " + r + " block.y: " + blocks[r][0].point.y);
					if (bc > -1 && hitX >= blocks[r][bc].point.x && hitX < blocks[r][bc].point.x + blockWidth
							&& blocks[r][bc].alive) {
						// System.out.println("d. ball.x: " + ball.x + "," + ball.y + " newBall: " +
						// newBall.x
						// + "," + newBall.y + " hitXY: " + hitX + "," + hitY + " rows: " + rowBeg + "-"
						// + rowEnd
						// + " r: " + r + " block.y: " + blocks[r][0].point.y);
						float dx = hitX - (ball.x + leftEdge);
						float dy = hitY - (ball.y + lowerEdge);
						d = dx * dx + dy * dy;
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							dists[horzBlockLeft].dist = d;
							dists[horzBlockLeft].blockRow = r;
							dists[horzBlockLeft].blockCol = bc;
							dists[horzBlockLeft].ballX = hitX;
							dists[horzBlockLeft].ballY = hitY - ballSize;
						}
					}
					// LR hit horiz block check
					hitX += otherEdge;
					if (hitX - 0 < padCol + blockCols * (blockWidth + padCol)) {
						int bc2 = blockColPos(hitX - 0);
						if (!(hit && bc2 == bc) && bc2 > -1 && hitX >= blocks[r][bc2].point.x
								&& hitX < blocks[r][bc2].point.x + blockWidth
								&& blocks[r][bc2].alive) { // bc2 < blockCols after changing blockCol max???
							// if (bc2 > -1 && bc != bc2 && blocks[r][bc2].alive) { // efficient for blocks
							// all the way to wall
							if (d == -1) {
								float dx = hitX - (ball.x + rightEdge);
								float dy = hitY - (ball.y + lowerEdge);
								d = dx * dx + dy * dy;
							}
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[horzBlockRight].dist = d;
								dists[horzBlockRight].blockRow = r;
								dists[horzBlockRight].blockCol = bc2;
								dists[horzBlockRight].ballX = hitX - ballSize;
								dists[horzBlockRight].ballY = hitY - ballSize;
							}
						}
					}
				}
				int colBeg = blockColNeg(ball.x + 0);
				int colEnd = blockColNeg(newBall.x + 0);
				// System.out.println("zzz");
				/*
				 * colBeg: 0 colEnd: -1 ball: 42,76 newBall: 40,78 blockRightEdge: 41
				 * hit_2: (41,85)
				 * vert zone: 77 -> 92
				 * Ball dir DL
				 * cnt: 3
				 */
				for (int c = colBeg; c > colEnd; c--) {
					// System.out.println("colBeg: " + colBeg + " colEnd: " + colEnd + " ball: " +
					// ball.x + "," + ball.y
					// + " newBall: " + newBall.x + "," + newBall.y + " blockRightEdge: "
					// + (blocks[0][colBeg].point.x + blockWidth));

					// LL hit vert block check
					float hitX2 = blocks[0][c].point.x + blockWidth;
					float hitY2 = ((hitX2 - (ball.x + leftEdge)) * m + ball.y + lowerEdge);
					// System.out.println("hit_2: (" + hitX2 + "," + hitY2 + ")");
					// if (hitY2 > newBall.y + lowerEdge) { // NOTE: changes things: reveals bug?

					// System.out.println("hitY2 > newBall.y + lowerEdge: " + hitY2 + " > " +
					// newBall.y + lowerEdge);
					// break;
					// }
					int br = blockRowPos(hitY2);
					// System.out.println("aaall: br: " + br + " c: " + c);
					// if (br > -1) {
					// System.out.println(
					// "vert zone: " + blocks[br][c].point.y + " -> " + (blocks[br][c].point.y +
					// blockHeight));
					// }
					float d = -1;
					boolean hit = false;
					if (br > -1 && hitY2 > blocks[br][c].point.y && hitY2 < blocks[br][c].point.y + blockHeight // was
																												// >=
							&& blocks[br][c].alive) {
						// System.out.println("aaa");
						float dx = hitX2 - (ball.x + leftEdge);
						float dy = hitY2 - (ball.y + lowerEdge);
						d = dx * dx + dy * dy;
						// System.out.println("d: " + d + " min: " + min);
						if (d <= min) {
							foundHit = true;
							hit = true;
							// System.out.println("bbb");
							min = d;
							dists[vertBlockBottom].dist = d;
							dists[vertBlockBottom].blockRow = br;
							dists[vertBlockBottom].blockCol = c;
							dists[vertBlockBottom].ballX = hitX2;
							dists[vertBlockBottom].ballY = hitY2 - ballSize;
						}
					}
					hitY2 -= otherEdge;
					// System.out.println("hitY2: " + hitY2);
					if (hitY2 - 0 > padTop) {
						// UL hit vert block check
						int br2 = blockRowPos(hitY2 - 0);
						// System.out.println("br2: " + br2);
						if (!(hit && br2 == br) && br2 >= -1 && hitY2 > blocks[br2][c].point.y
								&& hitY2 < blocks[br2][c].point.y + blockHeight
								&& blocks[br2][c].alive) { // br != br2 &&
							// float d = Math.pow(hitX2 - 0 - ball.x, 2)
							if (d == -1) {
								float dx = hitX2 - (ball.x + leftEdge);
								float dy = hitY2 - (ball.y + upperEdge);
								d = dx * dx + dy * dy;
							}
							// if (d > 15) {
							// paused = true;
							// }
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[vertBlockTop].dist = d;
								dists[vertBlockTop].blockRow = br2;
								dists[vertBlockTop].blockCol = c;
								dists[vertBlockTop].ballX = hitX2 - 0;
								dists[vertBlockTop].ballY = hitY2 - 0;
							}
						}
					}
				}

				if (!foundHit) {
					break ret;
				}

				System.out.println("Ball dir DL");
				printDist();

				if (dists[vertWall].dist == min) {
					wallHit = true;
					if (dists[horzBlockLeft].dist == min) { // LR
						blockHit = true;
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						vel.y *= -1;
						// newBall.x = ball.x - (newBall.x - ball.x);
						newBall.y = 2 * ball.y - newBall.y;
					} else {
					}
					ball.x = dists[vertWall].ballX;
					ball.y = dists[vertWall].ballY;
					vel.x *= -1;
					// System.out.println("b. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
					// System.out.println("b. ball.x: " + ball.x + "," + ball.y + " newBall: " +
					// newBall.x
					// + "," + newBall.y);
					newBall.x = 2 * ball.x - newBall.x;
					// System.out.println("c. ball.x: " + ball.x + "," + ball.y + " newBall: " +
					// newBall.x
					// + "," + newBall.y);
				} else if ((dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min)
						&& (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min)) {
					System.out.println("hit two: reversing");
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("    hit vertBlockTop");
						Dist bd = dists[vertBlockTop];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockRight].dist == min) {
						blockHit = true;
						System.out.println("    hit horzBlockRight");
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min) {
					if (dists[vertBlockBottom].dist == min) {
						blockHit = true;
						System.out.println("hit vertBlockBottom");
						Dist bd = dists[vertBlockBottom];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("hit vertBlockTop");
						Dist bd = dists[vertBlockTop];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					// System.out.println("before newBall: " + newBall.x + "," + newBall.y);
					// System.out.println("before ball: " + ball.x + "," + ball.y);
					newBall.x = 2 * ball.x - newBall.x;
					// System.out.println("after newBall: " + newBall.x + "," + newBall.y);
					// System.out.println("after ball: " + ball.x + "," + ball.y);
					// newBall.x = ball.x + (ball.x - newBall.x);
				} else if (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min) {
					if (dists[horzBlockLeft].dist == min) {
						blockHit = true;
						System.out.println("hit horzBlockLeft");
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockRight].dist == min) {
						blockHit = true;
						System.out.println("hit horzBlockRight");
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
				} else if (dists[horzWall].dist == min) { // Possible to have padTop small enough to hit top and
															// block
					wallHit = true;
					ball.x = dists[horzWall].ballX;
					ball.y = dists[horzWall].ballY;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					lose();
					retLose = true;
					break ret;
				} else { // no hits
					// ball.x = newBall.x;
					// ball.y = newBall.y;
				}
			} else { // if (vel.x < 0 && vel.y < 0) {
				// *********************************** Up and Left Ball movement *************
				// horizontal wall hit
				if (newBall.y < 0) {
					float dy = (0 - (ball.y + 0));
					float dx = dy / m;
					float d = dx * dx + dy * dy;
					// System.out.println(dx + "," + dy + " " + d);
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[horzWall].dist = d;
						dists[horzWall].ballX = ball.x + dx;
						dists[horzWall].ballY = 0;
					}
				}
				// vertical wall hit
				if (newBall.x < 0) {
					float dx = (0 - ball.x);
					float dy = dx * m;
					float d = dx * dx + dy * dy;
					if (d <= min) {
						foundHit = true;
						min = d;
						dists[vertWall].dist = d;
						dists[vertWall].ballX = 0;
						dists[vertWall].ballY = ball.y + dy;
						// System.out.println("a. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
						// newBall.x: " + newBall.x
						// + ", newBall.y: " + newBall.y);
						// System.out.println(
						// "ballX: " + maxWidth + ", ball.y: " + ball.y + ", dx: " + dx + ", dy: "
						// + dy);
					}
				}
				int rowBeg = blockRowNeg(ball.y + 0);
				int rowEnd = blockRowNeg(newBall.y + 0);
				for (int r = rowBeg; r > rowEnd; r--) {
					// UL hit horiz block check
					float hitY = blocks[r][0].point.y + blockHeight;
					float hitX = ((hitY - (ball.y + 0)) / m + (ball.x + 0));
					int bc = blockColPos(hitX);
					// System.out.println(
					// "$$$$$$$$$$$$$$ Beg,End: " + rowBeg + "," + rowEnd + " bc: " + bc + " hitXY:
					// " + hitX + ","
					// + hitY);
					// System.out.println("ball: " + ball.x + "," + ball.y + " newball: " +
					// newBall.x + "," + newBall.y);
					float d = -1;
					boolean hit = false;
					if (bc > -1 && hitX >= blocks[r][bc].point.x && hitX < blocks[r][bc].point.x + blockWidth
							&& blocks[r][bc].alive) {
						// System.out.println("aaa");
						float dx = hitX - (ball.x + leftEdge);
						float dy = hitY - (ball.y + upperEdge);
						d = dx * dx + dy * dy;
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							dists[horzBlockRight].dist = d;
							dists[horzBlockRight].blockRow = r;
							dists[horzBlockRight].blockCol = bc;
							dists[horzBlockRight].ballX = hitX;
							dists[horzBlockRight].ballY = hitY - 0;
						}
					}
					// UR hit horiz block check
					hitX += otherEdge;
					if (hitX - 0 > padCol) {
						int bc2 = blockColPos(hitX - 0);
						if (!(hit && bc2 == bc) && bc2 > -1 && hitX >= blocks[r][bc2].point.x
								&& hitX < blocks[r][bc2].point.x + blockWidth
								&& blocks[r][bc2].alive) {
							// if (bc2 > -1 && bc != bc2 && blocks[r][bc2].alive) { // efficient for blocks
							// all the way to wall
							// System.out.println("bbb");
							if (d == -1) {
								float dx = hitX - (ball.x + rightEdge);
								float dy = hitY - (ball.y + upperEdge);
								d = dx * dx + dy * dy;
							}
							// if (d > 5) {
							// System.err.println(new Throwable().getStackTrace()[0].getLineNumber());
							// System.out.println("d: " + d);
							// paused = true;
							// }
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[horzBlockLeft].dist = d;
								dists[horzBlockLeft].blockRow = r;
								dists[horzBlockLeft].blockCol = bc2;
								dists[horzBlockLeft].ballX = hitX - ballSize;
								dists[horzBlockLeft].ballY = hitY - 0;
							}
						}
					}
				}
				int colBeg = blockColNeg(ball.x + 0) + 0;
				int colEnd = blockColNeg(newBall.x + 0) + 0;
				for (int c = colBeg; c > colEnd; c--) {
					/*
					 * zcolBeg: 3 colEnd: 2 ball.x:164 newBall.x:162 colBeg.x:124
					 * m: 1.0 hitX2: 164 hitY2: 246
					 * zcolBeg: 2 colEnd: 1 ball.x:124 newBall.x:122 colBeg.x:83
					 * m: 1.0 hitX2: 123 hitY2: 205
					 * zcolBeg: 1 colEnd: 0 ball.x:82 newBall.x:80 colBeg.x:42
					 * m: 1.0 hitX2: 82 hitY2: 164
					 * zcolBeg: 0 colEnd: -1 ball.x:42 newBall.x:40 colBeg.x:1
					 * m: 1.0 hitX2: 41 hitY2: 123
					 * d: 2.0 br: 3 c: 0
					 *****************
					 * Ball dir UL
					 * dist wall horzWall : 1.7976931348623157E308
					 * dist wall vertWall : 1.7976931348623157E308
					 * dist block horzBlockLeft : 1.7976931348623157E308
					 * dist block horzBlockRight : 1.7976931348623157E308
					 * dist block vertBlockBottom: 2.0
					 * dist block vertBlockTop : 1.7976931348623157E308
					 */
					// UL hit vert block check
					float hitX2 = blocks[0][c].point.x + blockWidth;
					float hitY2 = ((hitX2 - (ball.x + 0)) * m + ball.y + 0);
					if (hitY2 < newBall.y) {
						break;
					}
					// System.out.println("zcolBeg: " + colBeg + " colEnd: " + colEnd + " ball.x:" +
					// ball.x + " newBall.x:"
					// + newBall.x + " colBeg.x:"
					// + (colBeg * (blockWidth + padCol) + padCol));
					// if (hitY2 >= newBall.y) {
					// System.out.println("m: " + m + " hitX2: " + hitX2 + " hitY2: " + hitY2);
					int br = blockRowPos(hitY2);
					float d = -1;
					boolean hit = false;
					if (br > -1 && hitY2 >= newBall.y && hitY2 >= blocks[br][c].point.y
							&& hitY2 < blocks[br][c].point.y + blockHeight
							&& blocks[br][c].alive) {
						float dx = hitX2 - (ball.x + leftEdge);
						float dy = hitY2 - (ball.y + upperEdge);
						d = dx * dx + dy * dy;
						// if (d > 5) {
						// System.err.println("Line #" + new
						// Throwable().getStackTrace()[0].getLineNumber());
						// System.out.println("d: " + d);
						// paused = true;
						// }
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							dists[vertBlockTop].dist = d;
							dists[vertBlockTop].blockRow = br;
							dists[vertBlockTop].blockCol = c;
							dists[vertBlockTop].ballX = hitX2 - 0;
							dists[vertBlockTop].ballY = hitY2 - 0;
							// System.out.println("d: " + d + " br: " + br + " c: " + c);
							// paused = true;
						}
					}
					hitY2 += otherEdge;
					// System.out.println("hitY2: " + hitY2);
					if (hitY2 - 0 < padTop + blockRows * (blockHeight + padRow)) {
						// LL hit vert block check
						int br2 = blockRowPos(hitY2 - 0);
						// System.out.println("br2: " + br2);
						if (!(hit && br2 == br) && br2 > -1 && hitY2 >= blocks[br2][c].point.y
								&& hitY2 < blocks[br2][c].point.y + blockHeight
								&& blocks[br2][c].alive) { // br != br2 &&
							if (d == -1) {
								float dx = hitX2 - (ball.x + leftEdge);
								float dy = hitY2 - (ball.y + lowerEdge);
								d = dx * dx + dy * dy;
							}
							if (d <= min) {
								foundHit = true;
								min = d;
								dists[vertBlockBottom].dist = d;
								dists[vertBlockBottom].blockRow = br2;
								dists[vertBlockBottom].blockCol = c;
								dists[vertBlockBottom].ballX = hitX2 - 0;
								dists[vertBlockBottom].ballY = hitY2 - ballSize;
							}
						}
					}
				}

				if (!foundHit) {
					break ret;
				}

				// System.out.println("*****************");
				System.out.println("Ball dir UL");
				printDist();

				if (dists[vertWall].dist == min) {
					wallHit = true;
					if (dists[horzBlockLeft].dist == min) { // UL
						blockHit = true;
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						vel.y *= -1;
						newBall.y = 2 * ball.y + newBall.y;
					} else {
					}
					ball.x = dists[vertWall].ballX;
					ball.y = dists[vertWall].ballY;
					vel.x *= -1;
					// System.out.println("b. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
					newBall.x = 2 * ball.x - newBall.x; // newBall.x < 0
					// System.out.println("c. ball.x: " + ball.x + ", ball.y: " + ball.y + ",
					// newBall.x: " + newBall.x
					// + ", newBall.y: " + newBall.y);
				} else if ((dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min)
						&& (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min)) {
					System.out.println("hit two: reversing");
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("    hit vertBlockBottom");
						Dist bd = dists[vertBlockBottom];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockRight].dist == min) {
						blockHit = true;
						System.out.println("    hit horzBlockRight");
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					vel.y *= -1;
					newBall.y = 2 * ball.y + newBall.y;
					newBall.x = 2 * ball.x - newBall.x;
				} else if (dists[vertBlockBottom].dist == min || dists[vertBlockTop].dist == min) {
					if (dists[vertBlockBottom].dist == min) {
						blockHit = true;
						System.out.println("hit vertBlockBottom");
						Dist bd = dists[vertBlockBottom];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX + 1;
						ball.y = bd.ballY;
					}
					if (dists[vertBlockTop].dist == min) {
						blockHit = true;
						System.out.println("hit vertBlockTop");
						Dist bd = dists[vertBlockTop];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX + 1;
						ball.y = bd.ballY;
					}
					vel.x *= -1;
					// System.out.println("(((((((((((((((((((((((((( newBall: " + newBall.x + "," +
					// newBall.y);
					newBall.x = 2 * ball.x - newBall.x;
					// System.out.println(")))))))))))))))))))))))))) newBall: " + newBall.x + "," +
					// newBall.y);
				} else if (dists[horzBlockLeft].dist == min || dists[horzBlockRight].dist == min) {
					if (dists[horzBlockLeft].dist == min) {
						blockHit = true;
						System.out.println("hit horzBlockLeft");
						Dist bd = dists[horzBlockLeft];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					if (dists[horzBlockRight].dist == min) {
						blockHit = true;
						System.out.println("hit horzBlockRight");
						Dist bd = dists[horzBlockRight];
						blockRemove(bd.blockRow, bd.blockCol);
						ball.x = bd.ballX;
						ball.y = bd.ballY;
					}
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
					// System.out.println(ball.x + "," + ball.y + " " + newBall.x + "," +
					// newBall.y);
				} else if (dists[horzWall].dist == min) { // Possible to have padTop small enough to hit top and
															// block
					wallHit = true;
					ball.x = dists[horzWall].ballX;
					ball.y = dists[horzWall].ballY;
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
				} else { // no hits
					// ball.x = newBall.x;
					// ball.y = newBall.y;
				}

			}
			// ball.x = newBall.x;
			// ball.y = newBall.y;
			// System.out.println("zzz ball: " + ball.x + "," + ball.y);

			// currDist used for sound timing
			currDist += (float) Math.sqrt(Math.pow(ball.x - ballx, 2) + Math.pow(ball.y - bally, 2));
			if (wallHit) {
				playSound(wallMsg, (int) (currDist / frameDist * frameTimeuSec));
			}
			if (blockHit) {
				playSound(brickMsg, (int) (currDist / frameDist * frameTimeuSec));
			}

		} while (foundHit);
		// ball.x = (int) ballx;
		// ball.y = (int) bally;

		if (!retLose) {
			ball.x = newBall.x;
			ball.y = newBall.y;
		} else {
			playSound(loseMsg, (int) (currDist / frameDist * frameTimeuSec));
			if (wallHit) {
			}
		}
		return retLose;
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
		currDist = 0;

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

		newBall = new Point2D.Float(ball.x + vel.x, ball.y + vel.y);

		// Check for player paddle hit ball
		if (ball.y + lowerEdge < player.y && newBall.y + lowerEdge >= player.y) {
			int hitX = (int) (ball.x + (float) vel.x / vel.y * (player.y - (ball.y + lowerEdge)));
			if (hitX >= player.x - (ballSize - 1) && hitX < player.x + playerW) {
				int hit = (hitX - (player.x - (ballSize - 1))) * playerSegments / (playerW + (ballSize - 1));
				vel.x = bounces[hit].x;
				vel.y = bounces[hit].y;
				setSoundParameters();
				// System.out.println("vel:" + velocity + ", hit:" + hit);
				// velocity.y *= -1;
				newBall.y = 2 * player.y - newBall.y - 2 * ballSize;
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

		if (nextHit()) { // return true when level lost
			return;
		}
		// ball.x = newBall.x;
		// ball.y = newBall.y;
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

		Graphics2D g2 = (Graphics2D) g;
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

		g.setFont(new Font("Algerian", Font.BOLD, 14));
		g.setColor(Color.white);
		g.drawString("Level: " + level + "/" + highScore, 5, 15);

		g.setColor(Color.blue);
		g.fillRect(player.x, player.y, player.width, player.height);

		// g.setColor(Color.white);
		// g.fillRect(ball.x, ball.y, ball.width + 1, ball.height + 1);
		// g.drawLine(ball.x + ball.width, ball.y + 3, ball.x + ball.width, ball.y + 9);
		// g.setColor(Color.white);
		// g.fillRect(350, 10, 10, 1);
		// g.drawLine(340, 10, 348, 10);
		// g.drawLine(345, 11, 348, 11);

		for (int r = 0; r < blockRows; r++) {
			for (int c = 0; c < blockCols; c++) {
				g.setColor(blocks[r][0].color);
				if (blocks[r][c].alive) {
					g.fillRect(blocks[r][c].point.x, blocks[r][c].point.y, blockWidth, blockHeight);
					if (blocks[r][c].hits > 1) {
						g.setColor(Color.black);
						g.drawString("" + blocks[r][c].hits, blocks[r][c].point.x + 5, blocks[r][c].point.y + 12);
					}
				}
			}
		}
		g.setColor(Color.green);
		g2.fill(ball); // ball.x, ball.y, ball.width, ball.height);

		g.setColor(Color.white);
		g.drawString("fps: " + (frameRate), 5, gameHeight);
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

		System.out.println("Level: " + level);
		createDialog("You Won! Level: " + level, 1000);

		resetLevel();
	}

	public void onLose() {
		// player.setRect(new Rectangle(playerStartX, playerStartY, playerW, playerH));

		if (level > 1) {
			level--;
		}

		System.out.println("Level: " + level);
		createDialog("You Lost. Level: " + level, dialogDelay);

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
				// System.out.println("count: " + count);
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

	public void printDist() {
		for (int i = 0; i < dists.length; i++) {
			if (dists[i].dist < Float.POSITIVE_INFINITY) {
				System.out
						.print("dist wall  " + distNames[i] + ": " + String.format("%.2f", dists[i].dist) + " ballX: "
								+ dists[i].ballX + " ballY: "
								+ dists[i].ballY);
				if (dists[i].blockRow != -1) {
					System.out.print(" Row: " + dists[i].blockRow + " Col: " + dists[i].blockCol);
				}
				System.out.println();
			}

		}
	}

}
