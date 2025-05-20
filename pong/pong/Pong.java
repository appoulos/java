import java.awt.*;
// NOTE:
// large paddle to begin with
// serve hits paddle at start
// 144 fps has to be slower on level 1
// +/- only on level 1
// advance the sound
// add lives

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
	Color color;
	int hits;

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

public class Pong extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	// private Rectangle ball = new Rectangle(); // a rectangle that represents the
	// ball
	public Rectangle2D.Float ball = new Rectangle2D.Float(); // a rectangle that
	public Rectangle2D.Float prevball = new Rectangle2D.Float(); // a rectangle that
	// represents the ball
	// private Point nextCalc = new Point();

	private int level = 1;
	private final int startLives = 3;
	private int lives;
	private int highScore = 1;

	private boolean up, down; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer
	private boolean paused; // the update timer
	private boolean pauseTimerActive = false; // the update timer
	private long pauseTimer = 0;
	private String message = "";

	// private final int dialogDelay = 2000;
	private final int cheatLevels = 1; // Number of levels to have no game over

	private static int frameRate = 60; // roughly frame rate per second

	private float ballVelocity = 1f; // start velocity roughly frame rate per second
	private static float startBallVelocity = 2f; // start velocity roughly frame rate per second
	// private final float velStartX = 1f; // start velocity roughly frame rate per
	// second
	// private final float velStartY = 3f; // start velocity roughly frame rate per
	// second
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
	private final int blockHeight = 20;
	private final int padCol = 3; // padding between columns
	private final int padRow = 3; // padding between rows
	private final int padTop = 50; // padding above blocks
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
	private final int playerSegments = 30; // must be even
	private final int playerH = 5 * (playerSegments - ballMiddle); // pick number divisible by playerSegments -
																	// ballMiddle
	private final int playerW = 10;

	// the width of the game area
	private final int gameWidth = padCol + blockCols * (blockWidth + padCol);
	// the height of the game area
	private final int gameHeight = padTop + blockRows * (blockHeight + padRow) + padMiddle + playerH + padBottom;

	// private final int playerSegment = playerW / 2 / playerSegments;

	// private Point[] bounces = new Point[playerSegments];
	private Point2D.Float[] bounces = new Point2D.Float[playerSegments];

	private final int padRight = 30;
	private final int playerStartX = gameWidth - padRight;
	private final int playerStartY = gameHeight - padBottom - playerH;
	private static float playerVelocity = 10.0f;

	private final int maxWidth = gameWidth - 1 - ballSize;
	private final int maxHeight = gameHeight - 1 - ballSize;
	// max player.x position
	private final int mouseHeight = gameHeight - playerH;

	// private static JLabel dialogLabel;
	private static JFrame frame;
	// private static JDialog dialog;
	private static int screenHeight;
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

	String printBall() {
		return "\nball: (" + ball.x + ", " + ball.y + ") newBall: (" + newBall.x + ", " + newBall.y + ")";
	}

	void setSoundParameters() {
		frameDist = vel.x * vel.x + vel.y * vel.y;
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

		// dialog = new JDialog(frame, "Status");
		// dialogLabel = new JLabel("");
		// dialogLabel.setHorizontalAlignment(JLabel.CENTER);
		// dialog.add(dialogLabel);
		// dialog.setBounds(125, 125, 200, 70);
		// dialog.setVisible(false);

		frame.setTitle("Pong Game");
		frame.setLayout(new BorderLayout());
		// frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));

		Pong game = new Pong();
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
		// dialog.addKeyListener(game);
		// frame.addMouseMotionListener(game);
		// frame.removeMouseMotionListener(game);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();

		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		// GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		// System.out.println(graphicsEnvironment.getMaximumWindowBounds());
		screenHeight = graphicsEnvironment.getMaximumWindowBounds().height;

		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		System.out.println("refresh rate: " + device.getDisplayMode().getRefreshRate());
		frameRate = device.getDisplayMode().getRefreshRate();
		startBallVelocity *= 60 / frameRate;
		playerVelocity *= (int) 60 / frameRate;

		game.setUpGame();
		// game.enterFullScreen();
	}

	// Constructor for the game panel
	public Pong() {
		Dimension d = new Dimension(gameWidth, gameHeight);
		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);

	}

	// Method that is called by the timer framerate times per second (roughly)
	// Most games go through states - updating objects, then drawing them
	public void actionPerformed(ActionEvent e) {
		// long st = System.currentTimeMillis();
		// long st = System.nanoTime();
		update();
		// long st2 = System.currentTimeMillis();
		// long st2 = System.nanoTime();
		repaint();
		// long st3 = System.currentTimeMillis();
		// long st3 = System.nanoTime();
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
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
			up = true;
		} else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			down = true;
		} else if (keyCode == KeyEvent.VK_Q) {
			System.exit(0);
		} else if (keyCode == KeyEvent.VK_R) {
			setUpGame();
		} else if (keyCode == KeyEvent.VK_K) {
			if (keyboard) {
				enterFullScreen();
				if (!paused) {
					frame.addMouseMotionListener(this);
				}
			} else {
				exitFullScreen();
				frame.removeMouseMotionListener(this);
			}
			keyboard = !keyboard;
		} else if (keyCode == KeyEvent.VK_M) {
			if (soundPossible)
				mute = !mute;
		} else if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_SPACE) {
			if (paused && pauseTimerActive) {
				return;
				// long currTime = System.currentTimeMillis();
				// if (pauseTimerActive && currTime - pauseTimer > 2000) {
				// paused = false;
				// pauseTimerActive = false;
				// }
			}
			if (!keyboard) {
				if (paused) {
					frame.addMouseMotionListener(this);
				} else {
					frame.removeMouseMotionListener(this);
				}
			}
			paused = !paused;
		} else if (keyCode == KeyEvent.VK_MINUS) {
			if (playerVelocity > 1)
				playerVelocity -= 1;
		} else if (keyCode == KeyEvent.VK_EQUALS || keyCode == KeyEvent.VK_PLUS) {
			playerVelocity += 1;
		} else if (keyCode == KeyEvent.VK_9) {
			ballVelocity /= 2;
			vel.x /= 2;
			vel.y /= 2;
			System.out.println("ballVelocity: " + ballVelocity);
		} else if (keyCode == KeyEvent.VK_0) {
			ballVelocity *= 2;
			vel.x *= 2;
			vel.y *= 2;
			System.out.println("ballVelocity: " + ballVelocity);
		} else if (keyCode == KeyEvent.VK_7) {
			if (frameRate >= 2) {
				frameRate /= 2;
				setSoundParameters();
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (keyCode == KeyEvent.VK_8) {
			if (frameRate <= Integer.MAX_VALUE / 2) {
				frameRate *= 2;
				setSoundParameters();
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		}
	}

	// Called every time a key is released
	// Stores the down state for use in the update method
	public void keyReleased(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
			up = false;
		} else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			down = false;
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
		if (playerSegments % 2 != 0 + ignoreDeadCode) {
			System.out.println("playerSegments must be even");
			System.exit(1);
		}
		if (playerH % (playerSegments - ballMiddle) != 0 + ignoreDeadCode) {
			System.out.println("playerW must be divisible by (playerSegments-ballMiddle)");
			System.exit(1);
		}

		double phi = Math.atan2(3.0, 1.0);
		double theta = Math.atan2(1.0, 3.0);
		double dPhi = (phi - theta) / ((bounces.length - 1) / 2);
		System.out.println("phi: " + Math.toDegrees(phi));
		System.out.println("theta: " + Math.toDegrees(theta));
		System.out.println("dPhi: " + Math.toDegrees(dPhi));
		for (int i = 0; i < bounces.length / 2; i++) {
			System.out.println("angle: " + Math.toDegrees(i * dPhi + theta));
			float dy = (float) (startBallVelocity * Math.cos(i * dPhi + theta));
			float dx = (float) (startBallVelocity * Math.sin(i * dPhi + theta));
			bounces[i] = new Point2D.Float(-dx, -dy);
			bounces[bounces.length - i - 1] = new Point2D.Float(-dx, dy);
		}

		level = 1;

		if (timer != null) {
			timer.stop();
		}

		timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
		timer.start();

		up = down = false;

		lives = startLives;

		resetLevel();
	}

	private void resetLevel() {
		for (int i = 0; i < bounces.length; i++) {
			System.out.println(bounces[i]);
		}
		vel.x = bounces[playerSegments / 2].x;
		vel.y = bounces[playerSegments / 2].y;
		ballVelocity = startBallVelocity * (1 + (level - 1) * 0.2f);
		vel.x *= ballVelocity;
		vel.y *= -ballVelocity;
		System.out.println("vel: " + vel.x + "," + vel.y + " ballVelocity: " + ballVelocity);

		paused = true;

		// if (level == 1) {
		// lives = startLives;
		// }

		player = new Rectangle(playerStartX, playerStartY, playerW, playerH);
		ball = new Rectangle2D.Float(ballStartX, ballStartY, ballSize, ballSize);
		prevball = new Rectangle2D.Float(ballStartX, ballStartY, ballSize, ballSize);
		// vel.x = velStartX * (1 + (level - 1) * 0.2f);
		// vel.y = velStartY * (1 + (level - 1) * 0.2f);

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
		System.out.println("Level: " + level + ", lives: " + lives);
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

	void debug(String msg, float hitX, float hitY, int r, int c, float dx, float dy, int edgeX, int edgeY) {
		System.out.println("*****************");
		System.out.println(msg);
		System.out.println("vel    : " + vel.x + "," + vel.y);
		System.out.println("ball   : " + ball.x + "," + ball.y);
		System.out.println("newBall: " + newBall.x + "," + newBall.y);
		System.out.println("hitX,Y: " + hitX + "," + hitY + " r,c: " + r + "," + c + " dx,dy: " + dx + "," + dy
				+ " edgeX,Y: " + edgeX + "," + edgeY);
		System.out.println("*****************");
	}

	public boolean nextHit() { // assumes ball and newBall are set
		boolean retLose = false; // return true if game over
		boolean foundHit; // found collision in next ball step
		int cnt = 0;

		ret: do { // bounces
			cnt++;
			if (cnt > 5) {
				System.out.println("######################## error cnt: " + cnt);
				paused = true;
				// return false;
			}

			// NOTE: Setup variables

			float m = vel.y / vel.x;// Math.abs(vel.y / vel.x); // ball velocity slope
			float min; // next hit minimum distance
			boolean blockHit = false;
			boolean wallHit = false;
			min = Float.POSITIVE_INFINITY;
			for (int i = 0; i < dists.length; i++) {
				dists[i].dist = Float.POSITIVE_INFINITY;
			}
			foundHit = false;
			float signX, signY;
			int boundaryX, boundaryY;
			int blockEdgeX, blockEdgeY;
			int edgeX, edgeY;
			int rowBeg, rowEnd;
			int colBeg, colEnd;
			int revEdgeX, revEdgeY;
			Dist bd;
			if (vel.x > 0) {
				signX = 1;
				boundaryX = maxWidth;
				edgeX = rightEdge;
				revEdgeX = leftEdge;
				blockEdgeX = 0;
				colBeg = blockColPos(ball.x + edgeX) + 1;
				colEnd = blockColPos(newBall.x + edgeX) + 1;
			} else {
				signX = -1;
				boundaryX = 0;
				edgeX = leftEdge;
				revEdgeX = rightEdge;
				blockEdgeX = blockWidth; // TEST:
				colBeg = blockColNeg(ball.x + edgeX);
				colEnd = blockColNeg(newBall.x + edgeX);
			}
			if (vel.y > 0) {
				signY = 1;
				boundaryY = maxHeight;
				edgeY = lowerEdge;
				revEdgeY = upperEdge;
				rowBeg = blockRowPos(ball.y + edgeY) + 1;
				rowEnd = blockRowPos(newBall.y + edgeY) + 1;
				blockEdgeY = 0;
			} else {
				signY = -1;
				boundaryY = 0;
				edgeY = upperEdge;
				revEdgeY = lowerEdge;
				rowBeg = blockRowNeg(ball.y + edgeY);
				rowEnd = blockRowNeg(newBall.y + edgeY);
				blockEdgeY = blockHeight; // TEST:
			}

			// NOTE: horizontal wall hit

			if (newBall.y < 0 || newBall.y > maxHeight) {
				float dy = boundaryY - ball.y;
				float dx = dy / m; // * signX * signY;
				float d = dx * dx + dy * dy;
				if (d <= min) {
					foundHit = true;
					min = d;
					dists[horzWall].dist = d;
					dists[horzWall].ballX = ball.x + dx;
					dists[horzWall].ballY = boundaryY;
				}
			}

			// NOTE: vertical wall hit

			if (newBall.x < 0 || newBall.x > maxWidth) {
				float dx = boundaryX - ball.x;
				float dy = dx * m; // * signY * signX;
				float d = dx * dx + dy * dy;
				if (d <= min) {
					foundHit = true;
					min = d;
					dists[vertWall].dist = d;
					dists[vertWall].ballX = boundaryX;
					dists[vertWall].ballY = ball.y + dy;
				}
			}

			if (!foundHit) {
				break ret;
			}

			System.out.println();
			System.out.println("******** Dir: (" + signX + "," + signY + ")");
			printDist();

			// printBall();

			if (dists[vertWall].dist == min) {
				wallHit = true;
				System.out.println("    hit vertWall");
				ball.x = dists[vertWall].ballX;
				ball.y = dists[vertWall].ballY;
				vel.x *= -1;
				newBall.x = 2 * ball.x - newBall.x;
			} else if (dists[horzWall].dist == min) {
				wallHit = true;
				System.out.println("    hit horzWall");
				ball.x = dists[horzWall].ballX;
				ball.y = dists[horzWall].ballY;
				vel.y *= -1;
				newBall.y = 2 * ball.y - newBall.y;
				if (signY > 0 && level > cheatLevels) {
					onLose();
					retLose = true;
					break ret;
				}
			}

			if (foundHit) {
				currDist += min;
			}
			if (wallHit) {
				playSound(wallMsg, (int) (currDist / frameDist * frameTimeuSec));
			}
			if (blockHit) {
				playSound(brickMsg, (int) (currDist / frameDist * frameTimeuSec));
			}

			// printBall();

			// Single step debugging (press return in console)
			// try {
			// System.in.read();
			// } catch (Exception e) {
			// }

		} while (foundHit);

		if (!retLose) {
			prevball.x = ball.x;
			prevball.y = ball.y;
			ball.x = newBall.x;
			ball.y = newBall.y;
		} else {
			playSound(loseMsg, (int) (currDist / frameDist * frameTimeuSec));
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
							onWin();
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
		if (up) {
			player.y -= playerVelocity;
		}
		if (down) {
			player.y += playerVelocity;
		}

		if (player.y < 0) {
			player.y = 0;
		} else if (player.y + player.height >= gameHeight) {
			player.y = gameHeight - player.height;
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

		// newBall = new Point2D.Float(ball.x + vel.x, ball.y + vel.y);
		newBall.x = ball.x + vel.x;
		newBall.y = ball.y + vel.y;

		// Check for player paddle hit ball
		if (ball.x + rightEdge < player.x && newBall.x + rightEdge >= player.x) {
			int hitY = (int) (ball.y + (float) vel.y / vel.x * (player.x - (ball.x + rightEdge)));
			if (hitY >= player.y - (ballSize - 1) && hitY < player.y + playerH) {
				int hit = (hitY - (player.y - (ballSize - 1))) * playerSegments / (playerH + (ballSize - 1));
				vel.x = bounces[hit].x * ballVelocity;
				vel.y = bounces[hit].y * ballVelocity;
				// vel.x *= (1 + (level - 1) * 0.2f);
				// vel.y *= (1 + (level - 1) * 0.2f);
				System.out
						.println("hit segment: " + hit + "/" + playerSegments + " vel: (" + vel.x + "," + vel.y + ")");
				setSoundParameters();
				// System.out.println("vel:" + velocity + ", hit:" + hit);
				// velocity.y *= -1;
				newBall.x = 2 * player.x - newBall.x - 2 * ballSize;
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
				}
			}
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

		g.setFont(new Font("Algerian", Font.BOLD, 14));
		g.setColor(Color.white);
		g.drawString("Level: " + level + "/" + highScore, 5, 15);
		g.drawString("Lives: " + lives, gameWidth - 70, 15);

		g.setColor(Color.blue);
		g.fillRect(player.x, player.y, player.width, player.height);

		// Blocks for breakout
		// for (int r = 0; r < blockRows; r++) {
		// for (int c = 0; c < blockCols; c++) {
		// g.setColor(blocks[r][0].color);
		// if (blocks[r][c].alive) {
		// g.fillRect(blocks[r][c].point.x, blocks[r][c].point.y, blockWidth,
		// blockHeight);
		// if (blocks[r][c].hits > 1) {
		// g.setColor(Color.black);
		// g.drawString("" + blocks[r][c].hits, blocks[r][c].point.x + 5,
		// blocks[r][c].point.y + 12);
		// }
		// }
		// }
		// }

		g.setColor(Color.magenta);
		g2.fill(prevball); // ball.x, ball.y, ball.width, ball.height);
		g.setColor(Color.green);
		g2.fill(ball); // ball.x, ball.y, ball.width, ball.height);

		g.setColor(Color.white);
		g.drawString("fps: " + frameRate + " vel: " + ballVelocity + " paddle: " + playerVelocity, 5, gameHeight);
		if (pauseTimerActive) {
			long currTime = System.currentTimeMillis();
			if (pauseTimerActive && currTime - pauseTimer > 2000) {
				// paused = false;
				pauseTimerActive = false;
			} else {
				g.drawString(message, gameWidth / 2 - 80, gameHeight - (padMiddle + padBottom) / 2);
			}

		} else if (paused) {
			g.setColor(Color.white);
			int startY = padTop + blockRows * (blockHeight + padRow) + 10;
			int height = 20;
			g.drawString("Up/Down Arrows or W/S: move paddle up/down", 20, startY);
			startY += height;
			g.drawString("R: Reset Level", 20, startY);
			startY += height;
			g.drawString("Q: Quit", 20, startY);
			startY += height;
			g.drawString("7/8: fps, 9/0: vel, -/+: paddle", 20, startY);

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
		lives++;
		if (level > highScore) {
			highScore = level;
			// System.out.println("HighScore: " + highScore);
		}

		System.out.println("Level: " + level);
		// createDialog("You Won! Level: " + level, 1000);
		startMessage("Level completed!");

		resetLevel();
	}

	public void onLose() {
		// player.setRect(new Rectangle(playerStartX, playerStartY, playerW, playerH));
		// if (level > cheatLevels) {
		// synchronized (countMutex) {
		// if (count == 0) {
		// count++;
		// // onLose();
		// // resetLevel();
		// // return;
		// }
		// }
		// }

		if (level > cheatLevels) {
			lives--;
		}
		if (lives <= 0) {

			if (level > 1) {
				level--;
				// lives = startLives;
			} else {
			}
			lives = startLives;

			System.out.println("Level: " + level + ", lives: " + lives);
			// createDialog("You Lost. Level: " + level, dialogDelay);
			startMessage("Lost level!");
		}

		resetLevel();
	}

	private void startMessage(String m) {
		message = m; // "Lost level! Now at level: " + level + ", lives: " + lives;
		pauseTimerActive = true;
		pauseTimer = System.currentTimeMillis();
		paused = true;
	}

	// Sets visible a Pseudo-dialog that removes itself after a fixed time interval
	// Uses a thread to not block the rest of the program
	//
	// @param: message: String -> The message that will appear on the dialog
	// @param: delay: int -> How long (in milliseconds) that Dialog is visible
	// private void createDialog(String message, int delay) {
	// dialogLabel.setText(message);
	// dialog.setVisible(true);
	// frame.requestFocus();
	//
	// Thread thread = new Thread(() -> {
	// try {
	// // Show pop up for [delay] milliseconds
	// Thread.sleep(delay);
	// } catch (Exception e) {
	// System.out.println("Thread failed :(");
	// dialog.setVisible(false);
	// frame.requestFocus();
	// }
	// // End of 3 seconds
	// // Close the pop up
	// dialog.setVisible(false);
	// frame.requestFocus();
	//
	// synchronized (countMutex) {
	// count--;
	// // System.out.println("count: " + count);
	// }
	// });
	// thread.start();
	// }

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
		player.y = mouseHeight * e.getY() / screenHeight;
	}

	public void printDist() {
		for (int i = 0; i < dists.length; i++) {
			if (dists[i].dist < Float.POSITIVE_INFINITY) {
				System.out
						.print("dist " + distNames[i] + ": " + String.format("%.2f", dists[i].dist) + " ballX: "
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
