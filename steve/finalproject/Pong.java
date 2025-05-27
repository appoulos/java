import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Pong game.
 */
public class Pong extends JPanel implements ActionListener, KeyListener, MouseMotionListener {
	private int maxScore = 3;

	private static int screenWidth;
	private static int origFrameRate = 60; // roughly frame rate per second
	private static double scale; // scale frame to fill screen
	private static Font font; // scale frame to fill screen

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	private Rectangle player2 = new Rectangle(); // a rectangle that represents the player
	public Ellipse2D.Float ball = new Ellipse2D.Float(); // a rectangle that
	public Ellipse2D.Float prevball = new Ellipse2D.Float(); // a rectangle that

	private int level = 1;
	private int score1 = 0;
	private int score2 = 0;

	private boolean up, down; // booleans that track which keys are currently pressed
	private boolean up2, down2; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer
	private boolean paused; // the update timer
	private boolean help; // help menu on pause
	private boolean pauseTimerActive = false; // the update timer
	private long pauseTimer = 0;
	private String message = "";

	private static int frameRate = 60; // roughly frame rate per second

	private float ballVelocity = 1f; // start velocity roughly frame rate per second
	private static float startBallVelocity = 4f; // start velocity roughly frame rate per second
	private Point2D.Float vel = new Point2D.Float(); // velocity of ball

	private Point2D.Float newBall = new Point2D.Float(); // ball.x + vel.x, ball.y + vel.y);

	private final int ballSize = 7; // ODD ball size
	private final int leftEdge = 0; // ball size
	private final int rightEdge = ballSize - 1; // ball size

	private final int ballStartX = 90;

	private final int ballMiddle = ballSize / 2; // ballSize must be odd
	private final int playerSegments = 30; // must be even
	private final int playerH = 2 * (playerSegments - ballMiddle); // pick number divisible by playerSegments -
	// ballMiddle
	private final int playerW = 10;

	private final int gameWidth = 600; // the width of the game area
	private final int gameHeight = 400; // the height of the game area

	private Point2D.Float[] bounces = new Point2D.Float[playerSegments];

	private final int padEdge = 30;
	private final int playerStartX = gameWidth - padEdge;
	private final int playerStartY = gameHeight / 2;
	private static float playerVelocity = 10.0f;

	private final int player2StartX = 0 + padEdge;
	private final int player2StartY = gameHeight / 2;
	private static float player2Velocity = 10.0f;

	private final int maxWidth = gameWidth - 1 - ballSize;
	private final int maxHeight = gameHeight - 1 - ballSize;
	private final int mouseHeight = gameHeight - playerH; // max player.y position

	private static JFrame frame;
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
	};
	static Dist[] dists = new Dist[distNames.length];

	// MIDI
	static Receiver rcvr;
	static Synthesizer synth = null;
	static ShortMessage paddleMsg = new ShortMessage();
	static ShortMessage wallMsg = new ShortMessage();
	static ShortMessage loseMsg = new ShortMessage();
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

	enum hitType {
		paddle,
		wall,
		lose,
	}

	void shortenNote(int c) {
		new Thread(new Runnable() {
			public void run() {
				delay(100);
				chan[c].noteOff(50, 0);
			}
		}).start();
	}

	/**
	 * Play a note on the synthesizer and shorten paddle and wall notes.
	 * 
	 * @param hit type of hit sound to play
	 */
	void playHit(hitType hit) {
		final int noteVelocity = 83;
		switch (hit) {
			case paddle:
				chan[curChan].noteOn(50, noteVelocity);
				chan[curChan].noteOff(50, noteVelocity);
				// new Thread(new Runnable() {
				// public void run() {
				// delay(100);
				// chan[curChan].noteOff(50, noteVelocity);
				// }
				// }).start();
				break;
			case wall:
				chan[curChan].noteOn(40, noteVelocity);
				new Thread(new Runnable() {
					public void run() {
						delay(100);
						chan[curChan].noteOff(40, noteVelocity);
					}
				}).start();
				break;
			case lose:
				chan[curChan].noteOn(37, noteVelocity);
				break;
		}
		curChan = (curChan + 1) % maxChan;
	}

	/**
	 * Play a note on the synthesizer and shorten paddle and wall notes.
	 * 
	 * @param msg  MIDI message to pass to the <code>Synthesizer</code>.
	 * @param time hint to the <code>Synthesizer</code> as to when in microseconds
	 *             from now to play the note. <code>-1</code> means asap.
	 */
	void playSound(ShortMessage msg, int time) {
		if (!mute) {
			long t = synth.getMicrosecondPosition();
			if (time == -1) {
				rcvr.send(msg, -1);
				rcvr.send(paddleOffMsg, t + 5_000);
				rcvr.send(wallOffMsg, t + 5_000);
				System.out.println("sound: " + t);
			} else {
				rcvr.send(msg, t + time);
				rcvr.send(paddleOffMsg, t + time + 5_000);
				rcvr.send(wallOffMsg, t + time + 5_000);
			}
		}
	}

	/**
	 * Instantiate a new <code>Pong</code> object which starts a new
	 * <code>JFrame</code>.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		new Pong();
	}

	/**
	 * Setup the GUI and prepare the MIDI.
	 */
	public Pong() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		screenWidth = graphicsEnvironment.getMaximumWindowBounds().width;
		screenHeight = graphicsEnvironment.getMaximumWindowBounds().height;

		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		origFrameRate = device.getDisplayMode().getRefreshRate();
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
			// note Middle C = 60,
			// moderately loud (velocity = 93).
			final int noteVelocity = 83;
			paddleMsg.setMessage(ShortMessage.NOTE_ON, 0, 50, noteVelocity);
			wallMsg.setMessage(ShortMessage.NOTE_ON, 0, 40, noteVelocity);
			loseMsg.setMessage(ShortMessage.NOTE_ON, 0, 37, noteVelocity);
			paddleOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 50, noteVelocity);
			wallOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 40, noteVelocity);
			rcvr = MidiSystem.getReceiver();
			soundPossible = true;
			mute = false;
			maxChan = synth.getMaxPolyphony();
			chan = synth.getChannels();
			if (maxChan == 0) {
				System.out.println("No polyphony, disabling MIDI");
				soundPossible = false;
			} else {
				System.out.println("MIDI latency: " + synth.getLatency() + ", max polyphony: " + maxChan);
				// verify channels in chan
				for (int i = 0; i < chan.length; i++) {
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
		} catch (Exception e) {
			System.out.println("Warning: cound not initialize the MIDI system for audio. Sound disabled");
			// System.exit(1);
		}

		frame = new JFrame();

		frame.setTitle("Pong Game");
		frame.setLayout(new BorderLayout());
		// frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));

		// Pong game = new Pong();
		// frame.add(game, BorderLayout.CENTER);

		// add box to keep game in center while resizing window
		// from:
		// https://stackoverflow.com/questions/7223530/how-can-i-properly-center-a-jpanel-fixed-size-inside-a-jframe
		// Box box = new Box(BoxLayout.Y_AXIS);
		//
		// box.add(Box.createVerticalGlue());
		// box.add(this);
		// frame.add(box);
		frame.add(this);

		this.addKeyListener(this);
		frame.addKeyListener(this);
		// dialog.addKeyListener(game);
		// frame.addMouseMotionListener(game);
		// frame.removeMouseMotionListener(game);

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
		if (keyCode == KeyEvent.VK_UP) {
			up = true;
		} else if (keyCode == KeyEvent.VK_DOWN) {
			down = true;
		} else if (keyCode == KeyEvent.VK_W) {
			up2 = true;
		} else if (keyCode == KeyEvent.VK_S) {
			down2 = true;
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
		} else if (keyCode == KeyEvent.VK_1) {
			if (player2Velocity > 1)
				player2Velocity -= 1;
		} else if (keyCode == KeyEvent.VK_2) {
			player2Velocity += 1;
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
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
		} else if (keyCode == KeyEvent.VK_8) {
			if (frameRate <= 1000 / 2) {
				frameRate *= 2;
			}
			if (timer != null) {
				timer.stop();
			}
			timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
			timer.start();
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
		if (keyCode == KeyEvent.VK_UP) {
			up = false;
		} else if (keyCode == KeyEvent.VK_DOWN) {
			down = false;
		} else if (keyCode == KeyEvent.VK_W) {
			up2 = false;
		} else if (keyCode == KeyEvent.VK_S) {
			down2 = false;
		}
	}

	// Called every time a key is typed
	public void keyTyped(KeyEvent e) {
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
	 * Sets the initial state of the game.
	 */
	public void setUpGame() {
		for (int i = 0; i < dists.length; i++) {
			dists[i] = new Dist();
		}
		int ignoreDeadCode = 0;
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
			float dy = (float) Math.cos(i * dPhi + theta);
			float dx = (float) Math.sin(i * dPhi + theta);
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

		score1 = 0;
		score2 = 0;

		resetLevel();
	}

	/**
	 * Set starting positions of ball and players for a new round.
	 */
	private void resetLevel() {
		// fixed starting angle
		// vel.x = -bounces[playerSegments / 2].x;
		// vel.y = bounces[playerSegments / 2].y;

		// random starting angle
		int startAngleSegment = (int) (Math.random() * (playerSegments / 2));
		vel.x = -bounces[startAngleSegment].x;
		vel.y = bounces[startAngleSegment].y;
		ballVelocity = startBallVelocity * (1 + (level - 1) * 0.2f);
		vel.x *= ballVelocity;
		vel.y *= -ballVelocity;
		System.out.println("vel: " + vel.x + "," + vel.y + " ballVelocity: " + ballVelocity);

		paused = true;

		player = new Rectangle(playerStartX, playerStartY, playerW, playerH);
		player2 = new Rectangle(player2StartX, player2StartY, playerW, playerH);
		int ballx = ballStartX;
		int bally = (int) (Math.random() * gameHeight - ballSize);
		if ((int) (Math.random() * 2) == 1) {
			vel.y *= -1;
		}
		if (score1 + score2 % 2 == 1) {
			ballx = gameWidth / 2 + (gameWidth / 2 - ballx + rightEdge);
			vel.x *= -1;
		}
		ball = new Ellipse2D.Float(ballx, bally, ballSize, ballSize);
		prevball = new Ellipse2D.Float(ballx, bally, ballSize, ballSize);
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

			float m = vel.y / vel.x;// Math.abs(vel.y / vel.x); // ball velocity slope
			float min; // next hit minimum distance
			boolean wallHit = false;
			min = Float.POSITIVE_INFINITY;
			for (int i = 0; i < dists.length; i++) {
				dists[i].dist = Float.POSITIVE_INFINITY;
			}
			foundHit = false;
			float signX, signY;
			int boundaryX, boundaryY;
			if (vel.x > 0) {
				signX = 1;
				boundaryX = maxWidth;
			} else {
				signX = -1;
				boundaryX = 0;
			}
			if (vel.y > 0) {
				signY = 1;
				boundaryY = maxHeight;
			} else {
				signY = -1;
				boundaryY = 0;
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
			}

			if (foundHit) {
				currDist += min;
			}

			if (dists[vertWall].dist == min) {
				// playSound(loseMsg, (int) (currDist / frameDist * frameTimeuSec));
				playHit(hitType.lose);
				if (boundaryX == 0) {
					onLose(2);
					System.out.println("Left player lost round");
				} else {
					onLose(1);
					System.out.println("Right player lost round");
				}
				// checkWinner:
				if (Math.abs(score2 - score1) >= 2 && (score2 > maxScore || score1 >= maxScore)) {
					if (score2 > score1) {
						startMessage("Left player won");
						System.out.println("Left player won");
					} else {
						startMessage("Right player won");
						System.out.println("Right player won");
					}
					score1 = 0;
					score2 = 0;
				}
				return false;

			}
			if (wallHit) {
				// playSound(wallMsg, (int) (currDist / frameDist * frameTimeuSec));
				playHit(hitType.wall);
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

		if (up2) {
			player2.y -= player2Velocity;
		}
		if (down2) {
			player2.y += player2Velocity;
		}

		if (player2.y < 0) {
			player2.y = 0;
		} else if (player2.y + player2.height >= gameHeight) {
			player2.y = gameHeight - player2.height;
		}

		newBall.x = ball.x + vel.x;
		newBall.y = ball.y + vel.y;

		// Check for player paddle hit ball
		if (ball.x + rightEdge < player.x && newBall.x + rightEdge >= player.x) {
			int hitY = (int) (ball.y + (float) vel.y / vel.x * (player.x + leftEdge - (ball.x + rightEdge))
					+ ballMiddle);
			if (hitY >= player.y - ballMiddle && hitY <= player.y + playerH - 1 + ballMiddle) {
				int hit = (hitY - player.y) * playerSegments / playerH;
				if (hit < 0) {
					hit = 0;
				}
				if (hit >= playerSegments) {
					hit = playerSegments - 1;
				}

				float dx = bounces[hit].x;
				float dy = bounces[hit].y;

				ballVelocity *= 1.1f;

				vel.x = dx * ballVelocity;
				vel.y = dy * ballVelocity;

				ball.y = hitY - ballMiddle;
				ball.x = player.x - ballSize;

				float dist = (float) Math.sqrt(Math.pow(newBall.x - ball.x, 2) + Math.pow(newBall.y - ball.y, 2));
				newBall.x = ball.x + dx * dist;
				newBall.y = ball.y + dy * dist;

				// playSound(paddleMsg, -1);
				playHit(hitType.paddle);
				System.out
						.println("hit segment: " + hit + "/" + playerSegments + " vel: (" + vel.x + "," + vel.y + ")");
			}
		}

		if (ball.x + leftEdge > player2.x + playerW - 1 && newBall.x + leftEdge <= player2.x + playerW - 1) {
			int hitY = (int) (ball.y + (float) vel.y / vel.x * (player2.x + playerW - 1 - (ball.x + leftEdge))
					+ ballMiddle);
			if (hitY >= player2.y - ballMiddle && hitY <= player2.y + playerH - 1 + ballMiddle) {
				int hit = (hitY - player2.y) * playerSegments / playerH;
				if (hit < 0) {
					hit = 0;
				}
				if (hit >= playerSegments) {
					hit = playerSegments - 1;
				}

				float dx = -bounces[hit].x;
				float dy = bounces[hit].y;

				vel.x = dx * ballVelocity;
				vel.y = dy * ballVelocity;

				ball.y = hitY - ballMiddle;
				ball.x = player2.x + playerW;

				float dist = (float) Math.sqrt(Math.pow(newBall.x - ball.x, 2) + Math.pow(newBall.y - ball.y, 2));
				newBall.x = ball.x + dx * dist;
				newBall.y = ball.y + dy * dist;

				// playSound(paddleMsg, -1);
				playHit(hitType.paddle);
				System.out
						.println("hit segment: " + hit + "/" + playerSegments + " vel: (" + vel.x + "," + vel.y + ")");
			}
		}

		if (nextHit()) { // return true when level lost
			return;
		}
	}

	/**
	 * Draw the GUI elements.
	 */
	public void paint(Graphics g) {

		Graphics2D g2 = (Graphics2D) g;
		g2.scale(scale, scale);
		g2.setBackground(Color.darkGray);
		g2.clearRect(0, 0, (int) gameWidth, (int) gameHeight);

		// g2.setColor(Color.darkGray);
		// g2.fillRect(0, 0, (int) scale * gameWidth, (int) scale * gameHeight);

		g.setFont(font);
		g.setColor(Color.white);
		g.drawString("" + score1, gameWidth / 2 + 13, 15);
		g.drawString("" + score2, gameWidth / 2 - 25, 15);

		// Draw center line
		for (int i = 0; i < (int) gameHeight; i += 15)
			g.drawLine((int) gameWidth / 2, i, (int) gameWidth / 2, i + 10);

		g.setColor(Color.blue);
		g.fillRect(player.x, player.y, player.width, player.height);
		g.setColor(Color.red);
		g.fillRect(player2.x, player2.y, player2.width, player2.height);

		// previous ball hit
		// g.setColor(Color.magenta);
		// g2.fill(prevball); // ball.x, ball.y, ball.width, ball.height);

		// ball
		g.setColor(Color.green);
		g2.fill(ball); // ball.x, ball.y, ball.width, ball.height);

		g.setColor(Color.white);
		// g.drawString("fps: " + frameRate + " vel: " + ballVelocity + " paddle1: " +
		// playerVelocity + " paddle2: "
		// + player2Velocity, gameWidth/2-50, gameHeight);
		g.drawString(" speed: " + player2Velocity, gameWidth / 4 - 20, gameHeight - 10);
		g.drawString(" speed: "
				+ playerVelocity, gameWidth * 3 / 4 - 40, gameHeight - 10);
		if (pauseTimerActive) {
			long currTime = System.currentTimeMillis();
			if (pauseTimerActive && currTime - pauseTimer > 2000) {
				// paused = false;
				pauseTimerActive = false;
			} else {
				g.drawString(message, gameWidth / 2 - 80, gameHeight - 200);
			}

		} else if (paused) {
			g.setColor(Color.white);
			int startY = gameHeight / 2 + 60;
			final int leftPad = gameWidth / 2 - 160;

			int height = 20;

			if (!help) {
				startY = gameHeight / 2 + 60;
				g.drawString("Space to serve (h for help)", leftPad, startY);
			} else {
				startY = gameHeight / 2 - 60;
				g.drawString("Up/Down: move paddle1 up/down", leftPad, startY);
				startY += height;
				g.drawString("W/S: move paddle2 up/down", leftPad, startY);
				startY += height;
				g.drawString("R: Reset Level", leftPad, startY);
				startY += height;
				g.drawString("Q: Quit", leftPad, startY);
				startY += height;
				g.drawString("7/8: fps, 9/0: vel, -/+: paddle1", leftPad, startY);
				startY += height;
				g.drawString("-/+: paddle1, 1/2: paddle2", leftPad, startY);
				startY += height;
				g.drawString("K: toggle Mouse/Keyboard", leftPad, startY);
				startY += height;
				g.drawString("P or Space: toggle pause", leftPad, startY);
				if (soundPossible) {
					startY += height;
					g.drawString("M: Mute", leftPad, startY);
				}
			}

		}
	}

	/**
	 * Keep score when a player loses a point.
	 */
	public void onLose(int player) {
		if (player == 2) {
			score1++;
		} else {
			score2++;
		}

		resetLevel();
	}

	/**
	 * Allow for a message to stay on the screen and pause the game.
	 * 
	 * @param m message to display.
	 */
	private void startMessage(String m) {
		message = m; // "Lost level! Now at level: " + level + ", lives: " + lives;
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
		player.y = mouseHeight * e.getY() / screenHeight;
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
