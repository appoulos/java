import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;

import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Games is the main menu for the final project. The corresponding program is
 * loaded when a button is selected.
 */
public class Games extends JPanel implements ActionListener, KeyListener, MouseMotionListener, MouseListener {

	// gui
	private static double scale = 1.0; // scale frame to fill screen
	private static int frameRate = 20; // roughly frame rate per second
	private Timer timer; // the update timer
	private static JFrame frame;
	private static int screenWidth;
	private static int screenHeight;
	private static final int gameWidth = 600; // the width of the game area
	private final int gameHeight = 400; // the height of the game area
	private static Font font;

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
	int mouseX = 0;
	int mouseY = 0;
	int mouseClickedX = 0;
	int mouseClickedY = 0;
	int selection = 0;
	String selectionMax = "";
	int rectWidth = 0;
	int rectHeight = 0;
	String[] selections = {
			"1. Pong",
			"2. Pong2",
			"3. Breakout",
			"4. Arrays",
			"5. Wordle",
			"6. Quit"
	};

	/**
	 * @param msg  MIDI message to pass to the <code>Synthesizer</code>.
	 * @param time hint to the <code>Synthesizer</code> as to when in microseconds
	 *             from now to play the note. <code>-1</code> means asap.
	 */
	void playSound(ShortMessage msg, int time) {
		// NOTE: currently playing sounds immediately as update/paint is done right away
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

	/**
	 * Instantiate a new <code>Games</code> object which starts a new
	 * <code>JFrame</code>.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		new Games();
	}

	/**
	 * Setup the GUI and prepare the MIDI.
	 */
	public Games() {
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		screenWidth = graphicsEnvironment.getMaximumWindowBounds().width;
		screenHeight = graphicsEnvironment.getMaximumWindowBounds().height;

		if ((double) gameWidth / gameHeight >= (double) screenWidth / screenHeight) {
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
		// Box box = new Box(BoxLayout.Y_AXIS);
		//
		// box.add(Box.createVerticalGlue());
		// box.add(this);
		// frame.add(box);
		frame.add(this);

		// this.addKeyListener(this);
		frame.addKeyListener(this);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		// enterFullScreen();
		frame.setVisible(true);

		this.addMouseMotionListener(this);
		this.addMouseListener(this);

		this.setUpGame();
	}

	/**
	 * Method that is called by the timer framerate times per second (roughly)
	 */
	public void actionPerformed(ActionEvent e) {
		repaint();
	}

	/**
	 * Close the current menu frame and bring a new game to life.
	 */
	private void startGame() {
		frame.removeMouseMotionListener(this);
		frame.removeMouseListener(this);
		frame.dispose();
		switch (selection) {
			case 0:
				Pong pong = new Pong();
				pong.setVisible(true);
				break;
			case 1:
				Pong4 pong4 = new Pong4();
				pong4.setVisible(true);
				break;
			case 2:
				Breakout breakout = new Breakout();
				breakout.setVisible(true);
				break;
			case 3:
				MyProgram myprogram = new MyProgram();
				myprogram.setVisible(true);
				break;
			case 4:
				TonyWordle wordle = new TonyWordle();
				wordle.setVisible(true);
				// TonyWordle.main(new String[0]);
				break;
			case 5:
				System.exit(0);
		}
	}

	/**
	 * Called when a key is pressed and performs the requested action.
	 */
	public void keyPressed(KeyEvent e) {
		int keyCode = e.getKeyCode();
		if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
			if (selection > 0) {
				selection--;
				mouseX = -1;
			}
		} else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
			if (selection < selections.length - 1) {
				selection++;
				mouseX = -1;
			}
		} else if (keyCode == KeyEvent.VK_ENTER) {
			startGame();
		} else if (keyCode == KeyEvent.VK_P || keyCode == KeyEvent.VK_1) {
			selection = 0;
			startGame();
		} else if (keyCode == KeyEvent.VK_O || keyCode == KeyEvent.VK_2) {
			selection = 1;
			startGame();
		} else if (keyCode == KeyEvent.VK_B || keyCode == KeyEvent.VK_3) {
			selection = 2;
			startGame();
		} else if (keyCode == KeyEvent.VK_A || keyCode == KeyEvent.VK_4) {
			selection = 3;
			startGame();
		} else if (keyCode == KeyEvent.VK_W || keyCode == KeyEvent.VK_5) {
			selection = 4;
			startGame();
		} else if (keyCode == KeyEvent.VK_Q || keyCode == KeyEvent.VK_6) {
			selection = 5;
			startGame();
		} else if (keyCode == KeyEvent.VK_M) {
			if (soundPossible)
				mute = !mute;
		}
	}

	/**
	 * Called every time a key is released.
	 */
	public void keyReleased(KeyEvent e) {
	}

	/**
	 * Called every time a key is typed.
	 */
	public void keyTyped(KeyEvent e) {
	}

	/**
	 * Sets the initial state of the menu.
	 */
	public void setUpGame() {

		Graphics g = frame.getGraphics();
		FontMetrics metrics = getFontMetrics(font);
		// String str = "asdf";
		// Rectangle2D rect = metrics.getStringBounds(str, 0, str.length(), g);
		// g.getFontMetrics(font).getStringBounds("abc", g);

		// int max = 0;
		for (int i = 0; i < selections.length; i++) {
			Rectangle2D rect = metrics.getStringBounds(selections[i], 0, selections[i].length(), g);
			if (rect.getWidth() > rectWidth) {
				rectWidth = (int) rect.getWidth();
				rectHeight = (int) rect.getHeight();
				selectionMax = selections[i];
			}

			// if (selections[i].length() > max) {
			// max = selections[i].length();
			// selectionMax = selections[i];
			// }
		}

		// start update loop
		if (timer != null) {
			timer.stop();
		}
		timer = new Timer(1000 / frameRate, this); // roughly frameRate frames per second
		timer.start();
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
	 * Draw the GUI elements.
	 */
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.scale(scale, scale);
		g2.setColor(Color.darkGray);
		g2.fillRect(0, 0, (int) scale * gameWidth, (int) scale * gameHeight);

		g.setFont(font);
		g.setColor(Color.white);
		int posY = gameHeight / 4;
		int dy = (int) (rectHeight * 2);
		int posX = gameWidth / 2 - rectWidth;
		g.drawString("Main Menu", posX, posY);
		posY += dy;
		// g.drawString(
		// "clicked: " + mouseClickedX + "," + mouseClickedY + " mouse: " + mouseX + ","
		// + mouseY + " pos: " + posX
		// + "," + posY + " rect: " + rectWidth + "," + rectHeight,
		// 25, 25);
		for (int i = 0; i < selections.length; i++) {
			posY += dy;
			g.drawString(selections[i], posX, posY);
			if (mouseX >= 0) {
				if (// mouseX >= posX && mouseX <= posX + rectWidth &&
				mouseY >= posY - rectHeight
						&& mouseY <= posY + rectHeight / 2) {
					selection = i;
					g.drawRect(posX, posY - rectHeight, (int) rectWidth, (int) (rectHeight * 1.5));
					if (mouseClickedX >= 0) {
						mouseClickedX = -1;
						if (mouseClickedY == mouseY) {
							startGame();
						}
					}
				}
			} else {
				if (selection == i) {
					g.drawRect(posX, posY - rectHeight, (int) rectWidth, (int) (rectHeight * 1.5));
				}
			}
			// int max = g.getFontMetrics().stringWidth(selectionMax);
			// if (selection == i) {
			// g.drawRect(height, startY - g.getFontMetrics().getHeight(), max, height);
			// }
		}

		// startY += 2 * height;
		// if (soundPossible) {
		// startY += height;
		// g.drawString("M: Mute", 20, startY);
		// }
	}

	public void mouseDragged(MouseEvent e) {
	}

	/**
	 * Store the current mouse pointer position.
	 */
	public void mouseMoved(MouseEvent e) {
		// label2.setText("mouse is moved to point "
		// + e.getX() + " " + e.getY());
		// player.x = mouseWidth * e.getX() / screenWidth;
		mouseX = (int) (e.getX() / scale);
		mouseY = (int) (e.getY() / scale);
	}

	/**
	 * Store the pointer click location.
	 */
	public void mouseClicked(MouseEvent e) {
		mouseClickedX = (int) (e.getX() / scale);
		mouseClickedY = (int) (e.getY() / scale);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
	}
}
