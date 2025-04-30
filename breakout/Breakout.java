import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Line2D;

public class Breakout extends JPanel implements ActionListener, KeyListener {

	private int count = 0;
	private final Object countMutex = new Object();

	private Rectangle player = new Rectangle(); // a rectangle that represents the player
	private Rectangle goal = new Rectangle(); // a rectangle that represents the goal
	// private BadGuy[] badguys = new BadGuy[4]; //the array of Enemy objects
	// public static <BadGuy> Collection<BadGuy>
	// synchronizedCollection(Collection<BadGuy> badguys);
	// private ArrayList<BadGuy> badguys = new ArrayList<BadGuy>();
	private int level = 1;
	private int velX = 5;
	private int velY = 20;
	private int highScore = 0;

	private boolean left, right;// up, down, // booleans that track which keys are currently pressed
	private Timer timer; // the update timer

	private final int dialogDelay = 1000;
	private final int size = 10;
	private final int playerStartX = 200;
	private final int playerStartY = 250;
	private final int playerW = 50;
	private final int playerH = 10;
	private final int goalStartX = 100;
	private final int goalStartY = 100;
	// private final int pad = 10;
	// private final int radius = 100;

	private final int gameWidth = 500; // the width of the game area
	private final int gameHeight = 330; // the height of the game area
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
		/*
		 * if (e.getKeyCode() == KeyEvent.VK_UP) {
		 * up = true;
		 * } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
		 * down = true;
		 * } else
		 */ if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			left = true;
		} else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			right = true;
		} else /*
				 * if (e.getKeyCode() == KeyEvent.VK_W) {
				 * up = true;
				 * } else if (e.getKeyCode() == KeyEvent.VK_S) {
				 * down = true;
				 * } else
				 */
		if (e.getKeyCode() == KeyEvent.VK_A) {
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
			// } else if (e.getKeyCode() == KeyEvent.VK_W) {
			// up = false;
			// } else if (e.getKeyCode() == KeyEvent.VK_S) {
			// down = false;
			// } else if (e.getKeyCode() == KeyEvent.VK_UP) {
			// up = false;
			// } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			// down = false;
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

		// up = down = false;
		left = right = false;

		player = new Rectangle(playerStartX, playerStartY, playerW, playerH);
		goal = new Rectangle(goalStartX, goalStartY, size, size);
		// badguys.clear();
		// int randomx;
		// int randomy;
		// randomx = (int) (Math.random() * (gameWidth - 2 * radius - pad) + radius);
		// randomy = (int) (Math.random() * (gameHeight - 2 * radius - pad) + radius);
		// badguys.add(new SpinningEnemy(randomx, randomy, size, size, radius));
		// randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// randomy = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// badguys.add(new VerticalEnemy(randomx, randomy, size, size, gameHeight, 5));
		// randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// randomy = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// badguys.add(new DiagonalEnemy(randomx, randomy, size, size, gameHeight, 5,
		// gameWidth, 6));
		// randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// randomy = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// badguys.add(new StalkerEnemy(randomx, randomy, size, size, player));
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

	public static double calculateDistance(double x1, double y1, double x2, double y2) {
		return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
	}

	public Point calc(Rectangle ball, Rectangle ball2, Rectangle block) {
		if (Line2D.linesIntersect(ball.x, ball.y, ball2.x, ball2.y, block.x, block.x + block.width, block.y, block.y)) {
		}
		return new Point(0, 0);
	}

	// The update method does 5 things
	// 1 - it has the player move based on what key is currently being pressed
	// 2 - it prevents the player from leaving the screen
	// 3 - it checks if the player has reached the goal, and if so congratualtes
	// them and restarts the game
	// 4 - it checks if any of the Enemy objects are touching the player, and if so
	// notifies the player of their defeat and restarts the game
	// 5 - it tells each of the Enemy objects to update()
	public void update() {
		// if (up) {
		// player.y -= 3;
		// }
		// if (down) {
		// player.y += 3;
		// }
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

		// if (player.y < 0) {
		// player.y = 0;
		// } else if (player.y + player.height >= gameHeight) {
		// player.y = gameHeight - player.height;
		// }

		int newY = goal.y + velY;
		int newX = goal.x + velX;

		// if (player.intersects(goal)) { // check for win
		// int crossPt = (goal.x + newX) / 2;
		// if (newY >= player.y && crossPt >= player.x && crossPt <= player.x + playerW)
		// {
		if (Line2D.linesIntersect(goal.x, goal.y, newX, newY, player.x, player.y, player.x + playerW, player.y)) {
			velY *= -1;
			newY = 2 * player.y - newY;
			// synchronized (countMutex) {
			// if (count == 0) {
			// count++;
			// onWin();
			// }
			// }
		}

		// bounce off walls
		while (true) {
			if (velX < 0 && velY < 0) {
				if (newX < 0 && newY < 0) {
					if (newX < newY) {
						goal.y -= goal.x * velY / velX;
						goal.x = 0;
						velX *= -1;
						newX *= -1;
					} else {
						goal.x -= goal.y * velX / velY;
						goal.y = 0;
						velY *= -1;
						newY *= -1;
					}
					continue;
				}
				if (newX < 0) {
					goal.y -= goal.x * velY / velX;
					goal.x = 0;
					velX *= -1;
					newX *= -1;
					break;
				}
				if (newY < 0) {
					goal.x -= goal.y * velX / velY;
					goal.y = 0;
					velY *= -1;
					newY *= -1;
					break;
				}
			} else if (velX > 0 && velY < 0) {
				if (newX > maxWidth) {
					velX *= -1;
					newX = 2 * (maxWidth) - newX;
					continue;
				}
				if (newY < 0) {
					velY *= -1;
					newY *= -1;
					continue;
				}
			} else if (velX < 0 && velY > 0) {
				if (newX < 0) {
					velX *= -1;
					newX *= -1;
					continue;
				}
				if (newY > maxHeight) {
					velY *= -1;
					newY = 2 * (maxHeight) - newY;
					continue;
				}
			} else { // (velX > 0 && velY > 0)
				if (newX > maxWidth) {
					velX *= -1;
					newX = 2 * (maxWidth) - newX;
					continue;
				}
				if (newY > maxHeight) {
					velY *= -1;
					newY = 2 * (maxHeight) - newY;
					System.out.println("gameHeight-1: " + (maxHeight) + ", newY: " + newY);
					continue;
				}
				// if (newY >= gameHeight - 1 - size) {
				// newY = gameHeight - 1 - size;
				// velY *= -1;
				// }
				// // synchronized (countMutex) {
				// // if (count == 0) {
				// // count++;
				// // onLose();
				// // }
				// // }
				// if (newY <= 0) {
				// newY = 0;
				// velY *= -1;
				// }
				//
				// if (newX >= gameWidth - 1 - size) {
				// newX = gameWidth - 1 - size;
				// velX *= -1;
				// }
				// if (newX <= 0) {
				// newX = 0;
				// velX *= -1;
				// }
				// break;
			}
			break;
		}

		goal.x = newX;
		goal.y = newY;

		// else { // check for lose
		// for (int i = 0; i < badguys.size(); i++) {
		// BadGuy badguy;
		//
		// try { // badguy may be removed in other thread onLose()
		// badguy = badguys.get(i);
		// } catch (Exception e) {
		// break;
		// }
		//
		// if (badguy == null)
		// continue;
		//
		// badguy.move();
		//
		// if (badguy.intersects(player)) {
		// synchronized (countMutex) {
		// if (count == 0) {
		// count++;
		// onLose();
		// }
		// }
		// }
		// }
		// }
	}

	// The paint method does 3 things
	// 1 - it draws a white background
	// 2 - it draws the player in blue
	// 3 - it draws the goal in green
	// 4 - it draws all the Enemy objects
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
		g.fillRect(goal.x, goal.y, goal.width, goal.height);

		// for (int i = 0; i < badguys.size(); i++) {
		// try {
		// badguys.get(i).draw(g);
		// } catch (Exception e) {
		// System.out.println("Info: badguy " + i + " already deleted");
		// }
		// }

		final int blockX = 5;
		final int blockY = 50;
		final int padW = 5;
		final int padH = 5;
		final int blockW = 30;
		final int blockH = 10;
		for (int i = 0; i < 4; i++) {
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
			for (int j = 0; j < 7; j++) {
				g.fillRect(blockX + padW * (j + 1) + blockW * j, blockY + padH * (i + 1) + blockH * i, blockW, blockH);
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

		// int randomx;
		// int randomy;
		//
		// // add new badguy
		// if (level % 4 == 1) {
		// randomx = (int) (Math.random() * (gameWidth - 2 * radius - pad) + radius);
		// randomy = (int) (Math.random() * (gameHeight - 2 * radius - pad) + radius);
		// badguys.add(new SpinningEnemy(randomx, randomy, size, size, radius));
		// } else {
		// randomx = (int) (Math.random() * (gameWidth - size - pad) + pad / 2);
		// randomy = (int) (Math.random() * (gameHeight - size - pad) + pad / 2);
		// if (level % 4 == 0) {
		// badguys.add(new DiagonalEnemy(randomx, randomy, size, size, gameHeight, 5,
		// gameWidth, 6));
		// } else if (level % 4 == 2) {
		// badguys.add(new VerticalEnemy(randomx, randomy, size, size, gameHeight, 5));
		// } else if (level % 4 == 3) {
		// badguys.add(new StalkerEnemy(randomx, randomy, size, size, player));
		// }
		// }

		// System.out.println("Level: " + level);
		createDialog("You Won!", 1000);
	}

	private void onLose() {
		// player.setRect(new Rectangle(playerStartX, playerStartY, playerW, playerH));

		if (level > 1) {
			// remove badguys greater than 4
			// if (badguys.size() > 4) {
			// try {
			// badguys.remove(badguys.size() - 1);
			// } catch (Exception e) {
			// System.out.println("Error: removing badguy. badguys.size(): " +
			// badguys.size());
			// }
			// }
			level--;
		}

		// System.out.println("Level: " + level);
		createDialog("You Lost", dialogDelay);
		goal = new Rectangle(goalStartX, goalStartY, size, size);
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
