import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Breakout plays a brick breaking game.
 */
public class Breakout extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

	// ball
	public Rectangle2D.Float ball = new Rectangle2D.Float();
	public Rectangle2D.Float prevball = new Rectangle2D.Float(); // show ball previous hit for debugging
	private Point2D.Float newBall = new Point2D.Float(); // (ball.x + vel.x, ball.y + vel.y)
	private final int ballStartX = 40;
	private int ballStartY = 0; // padTop + blockRows * (blockHeight + padRow) + 10;
	private final int ballSize = 7; // must be ODD ball size
	private final int ballMiddle = ballSize / 2; // ballSize must be odd
	private final int otherEdge = ballSize - 1;
	private final int leftEdge = 0;
	private final int rightEdge = ballSize - 1;
	private final int upperEdge = 0;
	private final int lowerEdge = ballSize - 1;
	// ball velocity
	private Point2D.Float vel = new Point2D.Float(); // velocity of ball
	private float ballVelocity = 1f; // used to set vel x and y
	private static float startBallVelocity = 4f; // 60 fps level 1 speed. Actual machine fps will adjust

	// scoring
	private int level = 1;
	private final int cheatLevels = 0; // Number of levels to have no game over
	private int lives;
	private final int startLives = 3;
	private int highScore = 1;

	// Pause logic
	private boolean paused; // pause game
	private boolean help; // help menu on pause
	private boolean pauseTimerActive = false; // pause forced after win/lose
	private long pauseTimer = 0;
	private String message = "";

	// blocks
	private final int blockRows = 4;
	private static final int blockCols = 10;
	private Block[][] blocks = new Block[blockRows][blockCols];
	private int blockCnt = blockRows * blockCols;
	private static final int blockWidth = 40;
	private final int blockHeight = 20;
	private static final int padCol = 3; // padding between columns
	private final int padRow = 3; // padding between rows
	private final int padTop = 50; // padding above blocks
	private final int padMiddle = 150; // padding between blocks and paddle
	private final int padBottom = 20; // padding below paddle

	// player (1/2)
	private Rectangle player = new Rectangle(); // the player paddle
	private boolean left, right; // booleans that track which keys are currently pressed
	private final int playerSegments = 30; // must be even
	private final int playerW = 4 * (playerSegments - ballMiddle); // divisible by playerSegments - ballMiddle
	private final int playerH = 10;
	private Point2D.Float[] bounces = new Point2D.Float[playerSegments];
	private static float playerVelocity = 10.0f;

	// GUI
	private static Font font; // scale frame to fill screen
	private static double scale; // scale frame to fill screen
	private static int origFrameRate = 60; // roughly frame rate per second
	private static int frameRate = 60; // roughly frame rate per second
	private Timer timer; // the update timer
	private static JFrame frame;
	private static int screenWidth;
	private static int screenHeight;
	private boolean keyboard = true;
	// the width of the game area
	private static final int gameWidth = padCol + blockCols * (blockWidth + padCol);
	// the height of the game area
	private final int gameHeight = padTop + blockRows * (blockHeight + padRow) + padMiddle + playerH + padBottom;
	private final int maxWidth = gameWidth - 1 - ballSize;
	private final int maxHeight = gameHeight - 1 - ballSize;
	// max player.x position
	private final int mouseWidth = gameWidth - playerW;

	// player (2/2)
	private final int playerStartY = gameHeight - padBottom - playerH;
	private int playerStartX = 0; // (int) ((playerStartY - ballStartY) / 3.0f + ballStartX);

	// Distance calcs
	final int horzWall = 0;
	final int vertWall = 1;
	final int horzBlockLeft = 2;
	final int horzBlockRight = 3;
	final int vertBlockBottom = 4;
	final int vertBlockTop = 5;
	final static String[] distNames = {
			"horzWall       ",
			"vertWall       ",
			"horzBlockLeft  ",
			"horzBlockRight ",
			"vertBlockBottom",
			"vertBlockTop   ",
	};
	static Dist[] dists = new Dist[distNames.length];

	// sound
	static Receiver rcvr;
	static Synthesizer synth = null;
	static ShortMessage paddleMsg = new ShortMessage();
	static ShortMessage wallMsg = new ShortMessage();
	static ShortMessage loseMsg = new ShortMessage();
	static ShortMessage brickMsg = new ShortMessage();
	static ShortMessage paddleOffMsg = new ShortMessage();
	static ShortMessage wallOffMsg = new ShortMessage();
	static boolean soundPossible = false;
	static boolean mute = true;
	static float frameTimeuSec = 0f;
	static float frameDist = 0f;
	static float currDist = 0f;
	static int maxChan = 1;
	static int curChan = 0;
	static MidiChannel chan[];

	// /**
	// * Setup frame distance and timing so multiple hits during one frame will be
	// * played at the correct time.
	// */
	// void setSoundParameters() {
	// frameDist = ballVelocity * ballVelocity; // no sqrt to match nextHit min
	// calculations
	// frameTimeuSec = 1_000_000 / frameRate;
	// }

	enum hitType {
		brick,
		paddle,
		wall,
		lose,
	}

	/**
	 * Turn off a MIDI note in the future.
	 * 
	 * @param c    channel to turn off.
	 * @param note note to turn off.
	 */
	void shortenNote(int c, int note) {
		new Thread(new Runnable() {
			public void run() {
				delay(100);
				chan[c].noteOff(note, 0);
			}
		}).start();
	}

	/**
	 * Play a note on the synthesizer and shorten paddle and wall notes.
	 * 
	 * @param hit type of hit sound to play
	 */
	void playHit(hitType hit) {
		// note Middle C = 60,
		// moderately loud (velocity = 93).
		switch (hit) {
			case brick:
				chan[curChan].noteOn(100, 90);
				shortenNote(curChan, 100);
				break;
			case paddle:
				chan[curChan].noteOn(50, 60);
				shortenNote(curChan, 50);
				break;
			case wall:
				chan[curChan].noteOn(40, 55);
				shortenNote(curChan, 40);
				break;
			case lose:
				chan[curChan].noteOn(37, 60);
				break;
		}
		curChan = (curChan + 1) % maxChan;
	}

	/**
	 * Remove a block if it is alive.
	 * 
	 * @param r block row.
	 * @param c block column.
	 */
	private void blockRemove(int r, int c) {
		if (blocks[r][c].alive) {
			blocks[r][c].hits--;
			if (blocks[r][c].hits <= 0) {
				blocks[r][c].alive = false;
				blockCnt--;
			}
		}
	}

	/**
	 * Debug print some info about ball and newBall positions.
	 */
	String printBall() {
		return "\nball: (" + ball.x + ", " + ball.y + ") newBall: (" + newBall.x + ", " + newBall.y + ")";
	}

	/**
	 * Instantiate a new <code>Pong</code> object which starts a new
	 * <code>JFrame</code>.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		new Breakout();
	}

	/**
	 * Setup the GUI and prepare the MIDI.
	 */
	public Breakout() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		screenWidth = graphicsEnvironment.getMaximumWindowBounds().width;
		screenHeight = graphicsEnvironment.getMaximumWindowBounds().height;

		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		origFrameRate = device.getDisplayMode().getRefreshRate();
		if (origFrameRate == 0) {
			System.out.println("Warning: refresh rate was 0 so setting it to 60");
			origFrameRate = 60;
		}
		System.out.println("refresh rate: " + origFrameRate);
		frameRate = origFrameRate;
		startBallVelocity *= 60.0f / frameRate;
		playerVelocity *= (int) 60.0f / frameRate;

		int ignoreDeadCode = 0;

		if ((double) gameWidth / gameHeight >= (double) screenWidth / screenHeight + ignoreDeadCode) {
			scale = (double) screenWidth / gameWidth;
		} else {
			scale = (double) screenHeight / gameHeight;
		}
		scale *= 0.9;

		Dimension d = new Dimension((int) (scale * gameWidth), (int) (scale * gameHeight));

		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);

		font = new Font("Arial", Font.BOLD, 14);

		try {
			synth = MidiSystem.getSynthesizer();
			synth.open();
			// rcvr = MidiSystem.getReceiver(); // too slow on windows
			soundPossible = true;
			mute = false;
			chan = synth.getChannels();
			maxChan = chan.length;
			if (maxChan == 0) {
				System.out.println("No polyphony, disabling MIDI");
				soundPossible = false;
			} else {
				maxChan = 8; // chan 9 is not piano
				System.out.println("MIDI latency: " + synth.getLatency());
				System.out.println("max polyphony: " + synth.getMaxPolyphony());
				// verify channels in chan
				for (int i = 0; i < chan.length; i++) {
					// System.out.println("playing MIDI channel " + i);
					// chan[i].noteOn(50, 70);
					// delay(500);
					// chan[i].noteOff(50, 70);
					if (chan[i] == null) {
						System.out.println("MIDI channel " + i + " is null");
						if (i == 0) {
							System.out.println("MIDI channel 0 is null. Disabling MIDI");
							soundPossible = false;
							break;
						}
						maxChan = i;
						System.out.println("MIDI max channels: " + i);
						break;
					}
				}
			}
			chan[0].noteOff(50, 0); // get MIDI ready
		} catch (Exception e) {
			System.out.println("Warning: cound not initialize the MIDI system for audio. Sound disabled");
		}

		frame = new JFrame();

		frame.setTitle("Breakout Game");
		frame.setLayout(new BorderLayout());
		// frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));

		// Breakout game = new Breakout();

		// frame.add(game, BorderLayout.CENTER);
		// add box to keep game in center while resizing window
		// from:
		// https://stackoverflow.com/questions/7223530/how-can-i-properly-center-a-jpanel-fixed-size-inside-a-jframe
		// Box box = new Box(BoxLayout.Y_AXIS);
		// box.add(Box.createVerticalGlue());
		// box.add(this);
		// frame.add(box);
		frame.add(this);

		// this.addKeyListener(this);
		frame.addKeyListener(this);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		// enterFullScreen();

		this.setUpGame();
	}

	/**
	 * Method that is called by the timer framerate times per second (roughly)
	 */
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

	/**
	 * Called when a key is pressed and performs the requested action.
	 */
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
			if (timer != null) {
				timer.stop();
			}
			frame.dispose();
			Games game = new Games();
			game.setVisible(true);
		} else if (keyCode == KeyEvent.VK_R) {
			setUpGame();
		} else if (keyCode == KeyEvent.VK_K) {
			if (keyboard) {
				enterFullScreen();
				if (!paused) {
					this.addMouseMotionListener(this);
				}
			} else {
				exitFullScreen();
				this.removeMouseMotionListener(this);
			}
			keyboard = !keyboard;
		} else if (keyCode == KeyEvent.VK_M) {
			if (soundPossible)
				mute = !mute;
		} else if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_SPACE) {
			if (paused && pauseTimerActive) {
				return;
			}
			if (!keyboard) {
				if (paused) {
					this.addMouseMotionListener(this);
				} else {
					this.removeMouseMotionListener(this);
				}
			}
			paused = !paused;
			help = false;
		} else if (keyCode == KeyEvent.VK_H) {
			help = !help;
			if (help) {
				paused = true;
			}
		} else if (keyCode == KeyEvent.VK_MINUS) {
			if (playerVelocity > 1)
				playerVelocity -= 1;
		} else if (keyCode == KeyEvent.VK_EQUALS || keyCode == KeyEvent.VK_PLUS) {
			playerVelocity += 1;
		} else if (keyCode == KeyEvent.VK_9) {
			ballVelocity /= 2;
			vel.x /= 2;
			vel.y /= 2;
			// setSoundParameters();
			System.out.println("ballVelocity: " + ballVelocity);
		} else if (keyCode == KeyEvent.VK_0) {
			ballVelocity *= 2;
			vel.x *= 2;
			vel.y *= 2;
			// setSoundParameters();
			System.out.println("ballVelocity: " + ballVelocity);
		} else if (keyCode == KeyEvent.VK_7) {
			if (frameRate >= 2) {
				frameRate /= 2;
				// setSoundParameters();
				if (timer != null) {
					timer.stop();
				}
				timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
				timer.start();
			}
		} else if (keyCode == KeyEvent.VK_8) {
			if (frameRate <= 1000 / 2) {
				frameRate *= 2;
				// setSoundParameters();
				if (timer != null) {
					timer.stop();
				}
				timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
				timer.start();
			}
		}
	}

	/**
	 * Called every time a key is released.
	 * Stores the down state for use in the update method.
	 * 
	 * @param e KeyEvent
	 */
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

	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Sets the initial state of the game.
	 */
	public void setUpGame() {
		int ignoreDeadCode = 0;

		for (int i = 0; i < dists.length; i++) {
			dists[i] = new Dist();
		}
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
		if (playerW % (playerSegments - ballMiddle) != 0 + ignoreDeadCode) {
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
			// System.out.println("angle: " + Math.toDegrees(i * dPhi + theta));
			float dx = (float) (Math.cos(i * dPhi + theta));
			float dy = (float) (Math.sin(i * dPhi + theta));
			bounces[i] = new Point2D.Float(-dx, -dy);
			bounces[bounces.length - i - 1] = new Point2D.Float(dx, -dy);
		}

		level = 1;

		if (timer != null) {
			timer.stop();
		}

		timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
		timer.start();

		left = right = false;

		lives = startLives;

		resetLevel();
	}

	/**
	 * Set starting positions of ball a new round.
	 */
	private void resetBall() {
		paused = true;
		// for (int i = 0; i < bounces.length; i++) {
		// System.out.println(bounces[i]);
		// }
		vel.x = bounces[playerSegments / 2].x;
		vel.y = bounces[playerSegments / 2].y;
		ballVelocity = startBallVelocity * (1 + (level - 1) * 0.2f);
		vel.x *= ballVelocity;
		vel.y *= -ballVelocity;
		System.out.println("vel: " + vel.x + "," + vel.y + " ballVelocity: " + ballVelocity);
		frameRate = origFrameRate;

		player = new Rectangle(playerStartX, playerStartY, playerW, playerH);
		ball = new Rectangle2D.Float(ballStartX, ballStartY, ballSize, ballSize);
		prevball = new Rectangle2D.Float(ballStartX, ballStartY, ballSize, ballSize);

		// setSoundParameters();
	}

	/**
	 * Set starting positions of ball and players for a new round.
	 */
	private void resetLevel() {
		ballStartY = padTop + blockRows * (blockHeight + padRow) + 10;
		playerStartX = (int) ((playerStartY - ballStartY) / 3.0f + ballStartX - playerW / 2);

		resetBall();

		blockCnt = blockRows * blockCols;
		Color color = Color.pink;
		int maxHits = 1;
		for (int r = 0; r < blockRows; r++) {
			switch (r) {
				case 0:
					color = Color.RED;
					maxHits = 1;
					break;
				case 1:
					color = Color.YELLOW;
					maxHits = 1;
					break;
				case 2:
					color = Color.ORANGE;
					maxHits = 2;
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
			}
			// no blocks for debugging wall/paddle bounces
			// for (int c = 0; c < blockCols; c++) {
			// blocks[r][c].alive = false;
			// }
		}
		System.out.println("Level: " + level + ", lives: " + lives);
	}

	/**
	 * Enter fullscreen if it is supported.
	 */
	public void enterFullScreen() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			device.setFullScreenWindow(frame);
			// device.getDisplayModes();
			frame.validate();
		}
	}

	/**
	 * Exit fullscreen if it is supported.
	 */
	public void exitFullScreen() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		if (device.isFullScreenSupported()) {
			device.setFullScreenWindow(null);
			frame.validate();
		}
	}

	/**
	 * Find block column based on ball moving to the right.
	 * 
	 * @param x ball position
	 * @return block column
	 */
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

	/**
	 * Find block column based on ball moving to the left.
	 * 
	 * @param x ball position
	 * @return block column
	 */
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

	/**
	 * Find block row based on ball moving to the right.
	 * 
	 * @param x ball position
	 * @return block row
	 */
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

	/**
	 * Find block row based on ball moving to the left.
	 * 
	 * @param x ball position
	 * @return block row
	 */
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

	/**
	 * Print debug calculations for ball collisions.
	 */
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

	/**
	 * Calculate new position of ball taking into account collisions. Requires
	 * <code>ball</code> and <code>newBall</code> to be set.
	 */
	public boolean nextHit() { // assumes ball and newBall are set
		boolean retLose = false; // return true if game over
		boolean foundHit; // found collision in next ball step
		int cnt = 0;

		ret: do { // bounces
			cnt++;
			if (cnt > 5) {
				System.out.println("######################## error cnt: " + cnt);
				paused = true;
			}

			// NOTE: Setup variables

			float m = vel.y / vel.x;
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
				blockEdgeX = blockWidth;
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
				blockEdgeY = blockHeight;
				rowBeg = blockRowNeg(ball.y + edgeY);
				rowEnd = blockRowNeg(newBall.y + edgeY);
			}

			// NOTE: horizontal wall hit

			if (newBall.y < 0 || newBall.y > maxHeight) {
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

			// NOTE: vertical wall hit

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

			// NOTE: hit horizontal block check

			int r = rowBeg;
			while (r != rowEnd && r < blockRows && r != -1) {
				// System.out.println("&&&&&&&&&&&&&&&&&&&&&&& checking r: " + r);

				float hitY = blocks[r][0].point.y + blockEdgeY;
				float hitX = (hitY - (ball.y + edgeY)) / m + (ball.x + edgeX);
				// debug("1. horiz block check", hitX, hitY, r, -1, -1, -1, edgeX, edgeY);
				int bc = blockColPos(hitX);
				float d = -1;
				boolean hit = false;
				if (signX > 0 && hitX >= ball.x + edgeX && hitX <= newBall.x + edgeX ||
						signX < 0 && hitX <= ball.x + edgeX && hitX >= newBall.x + edgeX) {
					if (bc > -1 && hitX >= blocks[r][bc].point.x
							&& hitX < blocks[r][bc].point.x + blockWidth
							&& blocks[r][bc].alive) {
						float dx = hitX - (ball.x + edgeX);
						float dy = hitY - (ball.y + edgeY);
						d = dx * dx + dy * dy;
						if (d > vel.x * vel.x + vel.y * vel.y) {
							System.out.println("######################## error d too big: " + d);
							debug("1. horiz block check", hitX, hitY, r, bc, dx, dy, edgeX, edgeY);
							paused = true;
						}
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;

							bd = signX > 0 ? dists[horzBlockRight] : dists[horzBlockLeft];
							bd.dist = d;
							bd.blockRow = r;
							bd.blockCol = bc;
							bd.ballX = hitX - (edgeX == leftEdge ? 0 : otherEdge);
							bd.ballY = hitY - (edgeY == upperEdge ? 0 : otherEdge) - signY;
						}
					}
				}

				hitX -= signX * otherEdge;
				if (hitX > padCol && (signX > 0 && hitX >= ball.x + revEdgeX && hitX <= newBall.x + revEdgeX ||
						signX < 0 && hitX <= ball.x + revEdgeX && hitX >= newBall.x + revEdgeX)) {
					int bc2 = blockColPos(hitX - 0);
					if (!(hit && bc2 == bc) && bc2 > -1 && hitX >= blocks[r][bc2].point.x
							&& hitX < blocks[r][bc2].point.x + blockWidth
							&& blocks[r][bc2].alive) {
						if (d == -1) {
							float dx = hitX - (ball.x + revEdgeX);
							float dy = hitY - (ball.y + edgeY);
							d = dx * dx + dy * dy;
							if (d > vel.x * vel.x + vel.y * vel.y) {
								System.out.println("######################## error d too big: " + d);
								debug("2. horiz block check", hitX, hitY, r, bc2, dx, dy, edgeX, edgeY);
								paused = true;
							}
						}
						if (d <= min) {
							foundHit = true;
							min = d;
							bd = signX > 0 ? dists[horzBlockLeft] : dists[horzBlockRight]; // reversed
							bd.dist = d;
							bd.blockRow = r;
							bd.blockCol = bc2;
							bd.ballX = hitX - (revEdgeX == leftEdge ? 0 : otherEdge);
							bd.ballY = hitY - (edgeY == upperEdge ? 0 : otherEdge) - signY;
						}
					}
				}
				r += signY;
			} // while (r != rowEnd)

			// NOTE: hit vertical block check

			int c = colBeg;
			while (c != colEnd && c < blockCols && c != -1) {
				// System.out.println("&&&&&&&&&&&&&&&&&&&&&&& checking c: " + c);
				float hitX = blocks[0][c].point.x + blockEdgeX;
				// debug("1. vert block check", hitX, -1, -1, c, -1, -1, edgeX, edgeY);
				float hitY = (hitX - (ball.x + edgeX)) * m + (ball.y + edgeY);
				float d = -1;
				boolean hit = false;
				int br = blockRowPos(hitY);
				if (signY > 0 && hitY >= ball.y + edgeY && hitY <= newBall.y + edgeY ||
						signY < 0 && hitY <= ball.y + edgeY && hitY >= newBall.y + edgeY) {
					if (br > -1 && hitY > blocks[br][c].point.y
							&& hitY < blocks[br][c].point.y + blockHeight
							&& blocks[br][c].alive) {
						float dx = hitX - (ball.x + edgeX);
						float dy = hitY - (ball.y + edgeY);
						d = dx * dx + dy * dy;
						if (d > vel.x * vel.x + vel.y * vel.y) {
							System.out.println("######################## error d too big: " + d);
							debug("1. vert block check", hitX, hitY, br, c, dx, dy, edgeX, edgeY);
							paused = true;
							// return false;
						}
						if (d <= min) {
							foundHit = true;
							hit = true;
							min = d;
							bd = signY > 0 ? dists[vertBlockBottom] : dists[vertBlockTop];
							bd.dist = d;
							bd.blockRow = br;
							bd.blockCol = c;
							bd.ballX = hitX - (edgeX == leftEdge ? 0 : otherEdge);
							bd.ballY = hitY - (edgeY == upperEdge ? 0 : otherEdge);
						}
					}
				}

				hitY -= signY * otherEdge;
				if (hitY - 0 > padTop && (signY > 0 && hitY >= ball.y + revEdgeY && hitY <= newBall.y + revEdgeY ||
						signY < 0 && hitY <= ball.y + revEdgeY && hitY >= newBall.y + revEdgeY)) {
					int br2 = blockRowPos(hitY);
					if (!(hit && br2 == br) && br2 > -1 && hitY >= blocks[br2][c].point.y
							&& hitY < blocks[br2][c].point.y + blockHeight
							&& blocks[br2][c].alive) { // br != br2 &&
						if (d == -1) {
							float dx = hitX - (ball.x + edgeX);
							float dy = hitY - (ball.y + revEdgeY);
							d = dx * dx + dy * dy;
							if (d > vel.x * vel.x + vel.y * vel.y) {
								System.out.println("######################## error d too big: " + d);
								debug("2. vert block check", hitX, hitY, br2, c, dx, dy, edgeX, edgeY);
								paused = true;
								// return false;
							}
						}
						if (d <= min) {
							foundHit = true;
							min = d;
							bd = signY > 0 ? dists[vertBlockTop] : dists[vertBlockBottom]; // reversed
							bd.dist = d;
							bd.blockRow = br2;
							bd.blockCol = c;
							bd.ballX = hitX - (edgeX == leftEdge ? 0 : otherEdge);
							bd.ballY = hitY - (revEdgeY == upperEdge ? 0 : otherEdge);
						}
					}
				}
				c += signX;
			} // while (c != colEnd)

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
				if (dists[(signX > 0 ? horzBlockLeft : horzBlockRight)].dist == min) {
					blockHit = true;
					System.out.println("    hit horzBlock");
					bd = dists[(signX > 0 ? horzBlockLeft : horzBlockRight)];
					blockRemove(bd.blockRow, bd.blockCol);
					vel.y *= -1;
					newBall.y = 2 * ball.y - newBall.y;
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
					bd = dists[vertBlockTop];
					blockRemove(bd.blockRow, bd.blockCol);
					// done below so only once
					// ball.x = bd.ballX;
					// ball.y = bd.ballY;
				} else {
					blockHit = true;
					System.out.println("    hit vertBlockBottom");
					bd = dists[vertBlockBottom];
					blockRemove(bd.blockRow, bd.blockCol);
					// done below so only once
					// ball.x = bd.ballX;
					// ball.y = bd.ballY;
				}
				if (dists[horzBlockLeft].dist == min) {
					blockHit = true;
					System.out.println("    hit horzBlockLeft");
					bd = dists[horzBlockLeft];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				} else {
					blockHit = true;
					System.out.println("    hit horzBlockRight");
					bd = dists[horzBlockLeft];
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
					System.out.println("    hit vertBlockBottom");
					bd = dists[vertBlockBottom];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				if (dists[vertBlockTop].dist == min) {
					blockHit = true;
					System.out.println("    hit vertBlockTop");
					bd = dists[vertBlockTop];
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
					bd = dists[horzBlockLeft];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				if (dists[horzBlockRight].dist == min) {
					blockHit = true;
					System.out.println("    hit horzBlockRight");
					bd = dists[horzBlockRight];
					blockRemove(bd.blockRow, bd.blockCol);
					ball.x = bd.ballX;
					ball.y = bd.ballY;
				}
				vel.y *= -1;
				newBall.y = 2 * ball.y - newBall.y;
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
				if (wallHit) {
					// playSound(wallMsg, -1); //
					// playSound(brickMsg, (int) (frameTimeuSec * currDist / frameDist));
					playHit(hitType.wall);
					// System.out.println("frame time: " + frameTimeuSec);
					// System.out.println("pcnt: " + (currDist / frameDist));
				}
				if (blockHit) {
					// playSound(brickMsg, -1); // (int) (currDist / frameDist * frameTimeuSec));
					playHit(hitType.brick);
				}
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
			// playSound(loseMsg, (int) (currDist / frameDist * frameTimeuSec));
			playHit(hitType.lose);
		}
		return retLose;
	}

	/**
	 * Process paddle and ball movements for each frame.
	 */
	public void update() {
		currDist = 0;

		if (paused)
			return;
		if (left) {
			player.x -= playerVelocity;
		}
		if (right) {
			player.x += playerVelocity;
		}

		if (player.x < 0) {
			player.x = 0;
		} else if (player.x + player.width >= gameWidth) {
			player.x = gameWidth - player.width;
		}

		newBall.x = ball.x + vel.x;
		newBall.y = ball.y + vel.y;

		// Check for player paddle hit ball
		if (ball.y + lowerEdge < player.y && newBall.y + lowerEdge >= player.y) {
			int hitX = (int) (ball.x + (float) vel.x / vel.y * (player.y - (ball.y + lowerEdge)) + ballMiddle);
			if (hitX >= player.x - (ballMiddle) && hitX <= player.x + playerW - 1 + ballMiddle) {
				int hit = (hitX - player.x) * playerSegments / playerW;
				if (hit < 0) {
					hit = 0;
				}
				if (hit >= playerSegments) {
					hit = playerSegments - 1;
				}

				float dx = bounces[hit].x;
				float dy = bounces[hit].y;

				vel.x = dx * ballVelocity;
				vel.y = dy * ballVelocity;

				ball.x = hitX - ballMiddle;
				ball.y = player.y - ballSize;

				float dist = (float) Math.sqrt(Math.pow(newBall.x - ball.x, 2) + Math.pow(newBall.y - ball.y, 2));
				newBall.x = ball.x + dx * dist;
				newBall.y = ball.y + dy * dist;
				// newBall.y = player.y - (newBall.y + lowerEdge - player.y) - lowerEdge;

				// playSound(paddleMsg, -1);
				playHit(hitType.paddle);
				System.out
						.println("hit segment: " + hit + "/" + playerSegments + " vel: (" + vel.x + "," + vel.y + ")");
			}
		}

		if (nextHit()) { // return true when level lost
			return;
		}
		// ball.x = newBall.x;
		// ball.y = newBall.y;
		if (blockCnt <= 0) {
			onWin();
		}
	}

	/**
	 * Draw the GUI elements.
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;

		g2.scale(scale, scale);

		// set background
		g2.setBackground(Color.darkGray);
		// g2.clearRect(0, 0, (int) scale * gameWidth, (int) scale * gameHeight);
		g2.clearRect(0, 0, (int) gameWidth, (int) gameHeight);

		// g2.setColor(Color.darkGray);
		// g2.fillRect(0, 0, (int) scale * gameWidth, (int) scale * gameHeight);

		// upper left level
		g2.setFont(font);
		g2.setColor(Color.white);
		g2.drawString("Level: " + level + "/" + highScore, 5, 15);
		// g.drawString("Lives: " + lives, gameWidth - 70, 15);

		// player paddle
		g2.setColor(Color.blue);
		g2.fillRect(player.x, player.y, player.width, player.height);

		// blocks
		for (int r = 0; r < blockRows; r++) {
			for (int c = 0; c < blockCols; c++) {
				g2.setColor(blocks[r][0].color);
				if (blocks[r][c].alive) {
					g2.fillRect(blocks[r][c].point.x, blocks[r][c].point.y, blockWidth, blockHeight);
					if (blocks[r][c].hits > 1) {
						g2.setColor(Color.black);
						g2.drawString("" + blocks[r][c].hits, blocks[r][c].point.x + 5, blocks[r][c].point.y + 12);
					}
				}
			}
		}

		// previous ball hit
		// g.setColor(Color.magenta);
		// g2.fill(prevball); // ball.x, ball.y, ball.width, ball.height);

		// ball
		g2.setColor(Color.green);
		g2.fill(ball); // ball.x, ball.y, ball.width, ball.height);

		// Extra lives
		for (int i = 0; i < lives; i++) {
			g2.fillRect(gameWidth - (i + 1) * (ballSize / 2 + 5), 5, (int) ball.width / 2, (int) ball.height / 2);
		}

		g2.setColor(Color.white);
		// g.drawString(
		// " blocks: " + blockCnt,
		// 5, gameHeight);
		g2.drawString("fps: " + frameRate + " vel: " + ballVelocity + " paddle: " + playerVelocity
				+ " blocks: " + blockCnt, 5, gameHeight - 2);
		if (pauseTimerActive) {
			long currTime = System.currentTimeMillis();
			if (pauseTimerActive && currTime - pauseTimer > 2000) {
				// paused = false;
				pauseTimerActive = false;
			} else {
				g2.drawString(message, gameWidth / 2 - 80, gameHeight - (padMiddle + padBottom) / 2);
			}

		} else if (paused) {
			if (!help) {
				g2.drawString("Press space to start (h for help)", 20,
						gameHeight - (padMiddle + padBottom) / 2);
			} else {
				g2.setColor(Color.white);
				int startY = padTop + blockRows * (blockHeight + padRow) + 10;
				int height = 20;
				g2.drawString("Left/Right Arrows or A/D: move paddle", 20, startY);
				startY += height;
				g2.drawString("R: Reset Level", 20, startY);
				startY += height;
				g2.drawString("Q: Quit", 20, startY);
				startY += height;
				g2.drawString("7/8: fps, 9/0: vel, -/+: paddle", 20, startY);

				if (soundPossible) {
					startY += height;
					g2.drawString("M: Mute", 20, startY);
				}

				startY += height;
				g2.drawString("K: toggle Mouse/Keyboard", 20, startY);
				startY += height;
				g2.drawString("P or Space: toggle pause", 20, startY);
			}
		}
	}

	/**
	 * Process a level completion.
	 */
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

	/**
	 * Inform user that the game is over and reset the level.
	 */
	private void gameOver() {
		level = 1;
		startMessage("Game Over");
		lives = startLives;
		resetLevel();
	}

	/**
	 * Process a life lost and reset the ball.
	 */
	public void onLose() {
		lives--;
		if (lives < 0) {
			gameOver();
			return;
		}
		startMessage("Lives: " + lives);
		resetBall();
	}

	/**
	 * Allow for a message to stay on the screen and pause the game.
	 * 
	 * @param m message to display.
	 */
	private void startMessage(String m) {
		message = m;
		pauseTimerActive = true;
		pauseTimer = System.currentTimeMillis();
		paused = true;
	}

	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Store the current mouse pointer position.
	 */
	public void mouseMoved(MouseEvent e) {
		player.x = mouseWidth * e.getX() / screenWidth;
	}

	/**
	 * Print distance calculations for ball collisions.
	 */
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

	/**
	 * Add delay.
	 * 
	 * @param m delay in milliseconds.
	 */
	public void delay(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception e) {
		}
	}
}
