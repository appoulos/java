import java.awt.*;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.*;
import java.awt.event.*;

public class Games extends JPanel implements ActionListener, KeyListener, MouseMotionListener {

	// gui
	private static double scale; // scale frame to fill screen
	private static int origFrameRate = 60; // roughly frame rate per second
	private static int frameRate = 60; // roughly frame rate per second
	private Timer timer; // the update timer
	private static JFrame frame;
	private static int screenWidth;
	private static int screenHeight;
	// the width of the game area
	private static final int gameWidth = 600; // padCol + blockCols * (blockWidth + padCol);
	// the height of the game area
	private final int gameHeight = 400; // padTop + blockRows * (blockHeight + padRow) + padMiddle + playerH +
										// padBottom;
	private static Font font; // scale frame to fill screen

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

	// vars
	int selection = 0;
	String selectionMax = "";
	String[] selections = { "Pong", "Breakout", "Arrays", "Quit" };

	// NOTE: currently playing sounds immediately as update/paint is done right away
	void playSound(ShortMessage msg, int time) {
		if (!mute) {
			long t = synth.getMicrosecondPosition(); // time in microseconds
			if (time == -1) {
				rcvr.send(msg, -1);
				rcvr.send(paddleOffMsg, t + 5_000);
				rcvr.send(wallOffMsg, t + 5_000);
			} else {
				rcvr.send(msg, t + time);
				rcvr.send(paddleOffMsg, t + time + 5_000);
				rcvr.send(wallOffMsg, t + time + 5_000);
			}
		}
	}

	public static void main(String[] args) {
		new Games();
	}

	// Constructor for the game panel
	public Games() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		screenWidth = graphicsEnvironment.getMaximumWindowBounds().width;
		screenHeight = graphicsEnvironment.getMaximumWindowBounds().height;

		GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		origFrameRate = device.getDisplayMode().getRefreshRate();
		System.out.println("refresh rate: " + origFrameRate);
		frameRate = origFrameRate;

		int ignoreDeadCode = 0;

		if ((double) gameWidth / gameHeight >= (double) screenWidth / screenHeight + ignoreDeadCode) {
			scale = (double) screenWidth / gameWidth;
		} else {
			scale = (double) screenHeight / gameHeight;
		}

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
			brickMsg.setMessage(ShortMessage.NOTE_ON, 0, 100, noteVelocity);
			paddleOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 50, noteVelocity);
			wallOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 40, noteVelocity);
			// brickOffMsg.setMessage(ShortMessage.NOTE_OFF, 0, 100, noteVelocity);
			rcvr = MidiSystem.getReceiver();
			soundPossible = true;
			mute = false;
		} catch (Exception e) {
			System.out.println("Warning: cound not initialize the MIDI system for audio. Sound disabled");
		}

		frame = new JFrame();

		frame.setTitle("Games");
		frame.setLayout(new BorderLayout());
		// frame.setLayout(new BoxLayout(frame, BoxLayout.Y_AXIS));

		// Games game = new Games();

		// frame.add(game, BorderLayout.CENTER);
		// add box to keep game in center while resizing window
		// from:
		// https://stackoverflow.com/questions/7223530/how-can-i-properly-center-a-jpanel-fixed-size-inside-a-jframe
		Box box = new Box(BoxLayout.Y_AXIS);

		box.add(Box.createVerticalGlue());
		box.add(this);
		frame.add(box);

		this.addKeyListener(this);
		frame.addKeyListener(this);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();

		this.setUpGame();
	}

	// Method that is called by the timer framerate times per second (roughly)
	public void actionPerformed(ActionEvent e) {
		// long st = System.currentTimeMillis();
		// long st = System.nanoTime();
		// update();
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

	void startGame() {
		switch (selection) {
			case 0:
				Pong pong = new Pong();
				pong.setVisible(true);
				break;
			case 1:
				Breakout breakout = new Breakout();
				breakout.setVisible(true);
				break;
			case 2:
				MyProgram myprogram = new MyProgram();
				myprogram.setVisible(true);
				break;
			case 3:
				System.exit(0);
				break;
		}
		frame.dispose();
	}

	// Called every time a key is pressed
	// Stores the down state for use in the update method
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
			if (selection > 0)
				selection--;
		} else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			if (selection < selections.length - 1)
				selection++;
		} else if (keyCode == KeyEvent.VK_ENTER) {
			startGame();
		} else if (keyCode == KeyEvent.VK_P) {
			selection = 0;
			startGame();
		} else if (keyCode == KeyEvent.VK_B) {
			selection = 1;
			startGame();
		} else if (keyCode == KeyEvent.VK_M) {
			selection = 2;
			startGame();
		} else if (keyCode == KeyEvent.VK_Q) {
			selection = 3;
			startGame();
		} else if (keyCode == KeyEvent.VK_M) {
			if (soundPossible)
				mute = !mute;
		}
	}

	// Called every time a key is released
	// Stores the down state for use in the update method
	public void keyReleased(KeyEvent e) {
	}

	// Called every time a key is typed
	public void keyTyped(KeyEvent e) {
	}

	// Sets the initial state of the game
	public void setUpGame() {
		int max = 0;
		for (int i = 0; i < selections.length; i++) {
			if (selections[i].length() > max) {
				max = selections[i].length();
				selectionMax = selections[i];
			}
		}

		// start update loop
		if (timer != null) {
			timer.stop();
		}
		timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
		timer.start();
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
			frame.validate();
		}
	}

	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(scale, scale);
		g2.setColor(Color.darkGray);
		g2.fillRect(0, 0, (int) scale * gameWidth, (int) scale * gameHeight);

		g.setFont(font);
		g.setColor(Color.white);
		int startY = 40;
		int height = 20;
		for (int i = 0; i < selections.length; i++) {
			startY += height;
			g.drawString(selections[i], 20, startY);
			int max = g.getFontMetrics().stringWidth(selectionMax);
			if (selection == i) {
				g.drawRect(height, startY - g.getFontMetrics().getHeight(), max, height);
			}
		}

		// startY += 2 * height;
		// if (soundPossible) {
		// startY += height;
		// g.drawString("M: Mute", 20, startY);
		// }
	}

	public void mouseDragged(MouseEvent e) {
		// label1.setText("mouse is dragged through point "
		// + e.getX() + " " + e.getY());
	}

	// invoked when the cursor is moved from
	// one point to another within the component
	public void mouseMoved(MouseEvent e) {
		// label2.setText("mouse is moved to point "
		// + e.getX() + " " + e.getY());
		// player.x = mouseWidth * e.getX() / screenWidth;
	}

}
