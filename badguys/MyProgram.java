import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class MyProgram extends JPanel implements ActionListener, KeyListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	private Rectangle goal = new Rectangle(); // a rectangle that represents the goal
	// private BadGuy[] badguys = new BadGuy[4]; //the array of Enemy objects
	// public static <BadGuy> Collection<BadGuy>
	// synchronizedCollection(Collection<BadGuy> badguys);
	private ArrayList<BadGuy> badguys = new ArrayList<BadGuy>();
	private int level = 1;
	private int highScore = 0;

	private boolean up, down, left, right; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer

	private final int dialogDelay = 1000;
	private final int size = 20;
	private final int pad = 10;
	private final int radius = 100;

	private int gameWidth = 500; // the width of the game area
	private int gameHeight = 330; // the height of the game area

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

		MyProgram game = new MyProgram();
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
	public MyProgram() {
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
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = true;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = true;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_W) {
			up = true;
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			down = true;
		} else if (e.getKeyCode() == KeyEvent.VK_D) {
			right = true;
		} else if (e.getKeyCode() == KeyEvent.VK_Q) {
			System.exit(0);
		} else if (e.getKeyCode() == KeyEvent.VK_R) {
			setUpGame();
		}
	}

	// Called every time a key is released
	// Stores the down state for use in the update method
	public void keyReleased(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_UP) {
			up = false;
		} else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			down = false;
		} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = false;
		} else if (e.getKeyCode() == KeyEvent.VK_W) {
			up = false;
		} else if (e.getKeyCode() == KeyEvent.VK_A) {
			left = false;
		} else if (e.getKeyCode() == KeyEvent.VK_S) {
			down = false;
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
		level = 1;

		if (timer != null) {
			timer.stop();
		}

		timer = new Timer(1000 / 30, this); // roughly 30 frames per second
		timer.start();

		up = down = left = right = false;

		player = new Rectangle(50, 50, size, size);
		goal = new Rectangle(400, 300, size, size);
		badguys.clear();
		int randomx;
		int randomy;
		randomx = (int) (Math.random() * (gameWidth - 2 * radius - pad) + radius);
		randomy = (int) (Math.random() * (gameHeight - 2 * radius - pad) + radius);
		badguys.add(new SpinningEnemy(randomx, randomy, size, size, radius));
		randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		randomy = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		badguys.add(new VerticalEnemy(randomx, randomy, size, size, gameHeight, 5));
		randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		randomy = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		badguys.add(new DiagonalEnemy(randomx, randomy, size, size, gameHeight, 5, gameWidth, 6));
		randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		randomy = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		badguys.add(new StalkerEnemy(randomx, randomy, size, size, player));
		// System.out.println("Level: " + level);
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

	// The update method does 5 things
	// 1 - it has the player move based on what key is currently being pressed
	// 2 - it prevents the player from leaving the screen
	// 3 - it checks if the player has reached the goal, and if so congratualtes
	// them and restarts the game
	// 4 - it checks if any of the Enemy objects are touching the player, and if so
	// notifies the player of their defeat and restarts the game
	// 5 - it tells each of the Enemy objects to update()
	public void update() {
		if (up) {
			player.y -= 3;
		}
		if (down) {
			player.y += 3;
		}
		if (left) {
			player.x -= 3;
		}
		if (right) {
			player.x += 3;
		}

		if (player.x < 0) {
			player.x = 0;
		} else if (player.x + player.width >= gameWidth) {
			player.x = gameWidth - player.width;
		}

		if (player.y < 0) {
			player.y = 0;
		} else if (player.y + player.height >= gameHeight) {
			player.y = gameHeight - player.height;
		}

		if (player.intersects(goal)) { // check for win
			synchronized (countMutex) {
				if (count == 0) {
					count++;
					onWin();
				}
			}
		} else { // check for lose
			for (int i = 0; i < badguys.size(); i++) {
				BadGuy badguy;

				try { // badguy may be removed in other thread onLose()
					badguy = badguys.get(i);
				} catch (Exception e) {
					break;
				}

				if (badguy == null)
					continue;

				badguy.move();

				if (badguy.intersects(player)) {
					synchronized (countMutex) {
						if (count == 0) {
							count++;
							onLose();
						}
					}
				}
			}
		}
	}

	// The paint method does 3 things
	// 1 - it draws a white background
	// 2 - it draws the player in blue
	// 3 - it draws the goal in green
	// 4 - it draws all the Enemy objects
	public void paint(Graphics g) {

		g.setColor(Color.WHITE);
		g.fillRect(0, 0, gameWidth, gameHeight);

		g.setFont(new Font("Algerian", Font.BOLD, 15));
		g.setColor(Color.BLACK);
		g.drawString("Level: " + level + " Highscore: " + highScore, 5, 15);

		g.setColor(Color.BLUE);
		g.fillOval(player.x, player.y, player.width, player.height);

		g.setColor(Color.GREEN);
		g.fillRect(goal.x, goal.y, goal.width, goal.height);

		for (int i = 0; i < badguys.size(); i++) {
			try {
				badguys.get(i).draw(g);
			} catch (Exception e) {
				System.out.println("Info: badguy " + i + " already deleted");
			}
		}
	}

	public int getGameHeight() {
		return gameHeight;
	}

	private void onWin() {
		player.setRect(new Rectangle(50, 50, size, size));

		level++;
		if (level > highScore) {
			highScore = level;
			// System.out.println("HighScore: " + highScore);
		}

		int randomx;
		int randomy;

		// add new badguy
		if (level % 4 == 1) {
			randomx = (int) (Math.random() * (gameWidth - 2 * radius - pad) + radius);
			randomy = (int) (Math.random() * (gameHeight - 2 * radius - pad) + radius);
			badguys.add(new SpinningEnemy(randomx, randomy, size, size, radius));
		} else {
			randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
			randomy = (int) (Math.random() * (gameHeight - size - pad) + pad / 2);
			if (level % 4 == 0) {
				badguys.add(new DiagonalEnemy(randomx, randomy, size, size, gameHeight, 5, gameWidth, 6));
			} else if (level % 4 == 2) {
				badguys.add(new VerticalEnemy(randomx, randomy, size, size, gameHeight, 5));
			} else if (level % 4 == 3) {
				badguys.add(new StalkerEnemy(randomx, randomy, size, size, player));
			}
		}

		// System.out.println("Level: " + level);
		createDialog("You Won!", 1000);
	}

	private void onLose() {
		player.setRect(new Rectangle(50, 50, size, size));

		if (level > 1) {
			// remove badguys greater than 4
			if (badguys.size() > 4) {
				try {
					badguys.remove(badguys.size() - 1);
				} catch (Exception e) {
					System.out.println("Error: removing badguy. badguys.size(): " + badguys.size());
				}
			}
			level--;
		}

		// System.out.println("Level: " + level);
		createDialog("You Lost", dialogDelay);
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
