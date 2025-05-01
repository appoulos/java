import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

public class Breakout extends JPanel implements ActionListener, KeyListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	private Rectangle ball = new Rectangle(); // a rectangle that represents the ball
	private int level = 1;
	private Point vel = new Point(5, 20); // velocity of ball
	private Point newBall = new Point(ball.x + vel.x, ball.y + vel.y);
	private int highScore = 0;

	private boolean left, right; // booleans that track which keys are currently pressed
	private Timer timer; // the update timer

	private final int dialogDelay = 1000;

	private final int size = 10; // ball size
	private final int blockRows = 4;
	private final int blockCols = 10;
	private final int blockWidth = 30;
	private final int blockHeight = 10;
	private final int padCol = size - 1;
	private final int padRow = size - 1;
	private final int padTop = 20;

	private final int ballStartX = 10;
	private final int ballStartY = padTop + blockRows * (blockHeight + padRow) + 10;

	private final int gameWidth = padCol + blockCols * (blockWidth + padCol); // 500; // the width of the game area
	private final int gameHeight = 330; // the height of the game area

	private final int maxWidth = gameWidth - 1 - size;
	private final int maxHeight = gameHeight - 1 - size;

	private final int playerStartX = gameHeight - 30;
	private final int playerStartY = 250;
	private final int playerW = 50;
	private final int playerH = 10;

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
		level = 1;

		if (timer != null) {
			timer.stop();
		}

		timer = new Timer(1000 / 30, this); // roughly 30 frames per second
		timer.start();

		left = right = false;

		player = new Rectangle(playerStartX, playerStartY, playerW, playerH);
		ball = new Rectangle(ballStartX, ballStartY, size, size);
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

	public boolean hitBlockUL() {
		return false;
	}

	// The update method does 5 things
	// 1 - it has the player move based on what key is currently being pressed
	// 2 - it prevents the player from leaving the screen
	// 3 - it checks if the player has reached the goal, and if so congratualtes
	// them and restarts the game
	public void update() {
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

		newBall = new Point(ball.x + vel.x, ball.y + vel.y);

		if (vel.y > 0
				&& Line2D.linesIntersect(ball.x, ball.y, newBall.x, newBall.y, player.x, player.y, player.x + playerW,
						player.y)) {
			vel.y *= -1;
			newBall.y = 2 * player.y - newBall.y;
		}

		// bounce off walls
		while (true) {
			if (vel.x < 0 && vel.y < 0) {
				// bounce off blocks going up and to the left
				// if (hitBlockUL()) {
				// continue;
				// }
				if (newBall.x < 0 && newBall.y < 0) {
					if (newBall.x < newBall.y) {
						ball.y -= ball.x * vel.y / vel.x;
						ball.x = 0;
						vel.x *= -1;
						newBall.x *= -1;
					} else {
						ball.x -= ball.y * vel.x / vel.y;
						ball.y = 0;
						vel.y *= -1;
						newBall.y *= -1;
					}
					continue;
				}
				if (newBall.x < 0) {
					ball.y -= ball.x * vel.y / vel.x;
					ball.x = 0;
					vel.x *= -1;
					newBall.x *= -1;
					break;
				}
				if (newBall.y < 0) {
					ball.x -= ball.y * vel.x / vel.y;
					ball.y = 0;
					vel.y *= -1;
					newBall.y *= -1;
					break;
				}
			} else if (vel.x > 0 && vel.y < 0) {
				if (newBall.x > maxWidth) {
					vel.x *= -1;
					newBall.x = 2 * (maxWidth) - newBall.x;
					continue;
				}
				if (newBall.y < 0) {
					vel.y *= -1;
					newBall.y *= -1;
					continue;
				}
			} else if (vel.x < 0 && vel.y > 0) {
				if (newBall.x < 0) {
					vel.x *= -1;
					newBall.x *= -1;
					continue;
				}
				if (newBall.y > maxHeight) {
					vel.y *= -1;
					newBall.y = 2 * (maxHeight) - newBall.y;
					continue;
				}
			} else { // (vel.x > 0 && vel.y > 0)
				if (newBall.x > maxWidth) {
					vel.x *= -1;
					newBall.x = 2 * (maxWidth) - newBall.x;
					continue;
				}
				if (newBall.y > maxHeight) {
					vel.y *= -1;
					newBall.y = 2 * (maxHeight) - newBall.y;
					System.out.println("gameHeight-1: " + (maxHeight) + ", newBall.y: " + newBall.y);
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

	}

	// The paint method does 3 things
	// 1 - it draws a white background
	// 2 - it draws the player in blue
	// 3 - it draws the ball in green
	// 4 - it draws all the blocks
	public void paint(Graphics g) {

		g.setColor(Color.WHITE);
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

		// final int blockX = 5;
		// final int blockY = 50;
		// final int padW = 5;
		// final int padH = 5;
		// final int blockW = 30;
		// final int blockH = 10;
		for (int i = 0; i < blockRows; i++) {
			switch (i) {
				case 0:
					g.setColor(Color.RED);
					break;
				case 1:
					g.setColor(Color.YELLOW);
					break;
				case 2:
					g.setColor(Color.ORANGE);
					break;
				case 3:
					g.setColor(Color.BLUE);
					break;
			}
			for (int j = 0; j < blockCols; j++) {
				g.fillRect(padCol * (j + 1) + blockWidth * j, padTop + padRow * (i + 1) + blockHeight * i,
						blockWidth, blockHeight);
			}
		}
	}

	public int getGameHeight() {
		return gameHeight;
	}

	public void onWin() {
		player.setRect(new Rectangle(50, 50, size, size));

		level++;
		if (level > highScore) {
			highScore = level;
			// System.out.println("HighScore: " + highScore);
		}

		// System.out.println("Level: " + level);
		createDialog("You Won!", 1000);
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
