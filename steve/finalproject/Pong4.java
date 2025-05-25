import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Pong4 extends JPanel implements ActionListener, KeyListener {
	static JFrame frame = new JFrame();
	private int paddleWidth = 10, paddleHeight = 100;
	private int player1Y = 250, player2Y = 250;
	private int ballX = 390, ballY = 290, ballSize = 20;
	private int ballXSpeed = 4, ballYSpeed = 4;
	private Timer timer;
	private int player1Score = 0, player2Score = 0;

	private boolean upPressed = false, downPressed = false;
	private boolean wPressed = false, sPressed = false;

	private boolean gameOver = false;
	private boolean twoPlayerMode = false; // Change this to false for AI opponent
	private boolean paused = true;

	double scale;

	public Pong4() {
		int gameWidth = 800, gameHeight = 600;
		frame = new JFrame("Welcome to a game of Pong - First to 10");
		frame.setSize(gameWidth, gameHeight);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(this);
		frame.addKeyListener(this);
		frame.setVisible(true);
		// enterFullScreen();

		timer = new Timer(10, this);
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

	public void paintComponent(Graphics g) {
		super.paintComponent(g);

		setBackground(Color.BLACK);

		// Draw center line
		g.setColor(Color.WHITE);
		g.drawLine(400, 0, 400, 600);

		// Draw paddles
		g.fillRect(10, player1Y, paddleWidth, paddleHeight); // Player 1 (left)
		g.fillRect(780, player2Y, paddleWidth, paddleHeight); // Player 2 (right or AI)

		// Draw ball
		g.fillOval(ballX, ballY, ballSize, ballSize);

		// Draw scores
		g.setFont(new Font("Arial", Font.BOLD, 36));
		g.drawString(String.valueOf(player1Score), 350, 50);
		g.drawString(String.valueOf(player2Score), 420, 50);

		// Game over message
		if (gameOver) {
			g.setFont(new Font("Arial", Font.BOLD, 50));
			String winner = player1Score == 10 ? "Player 1 Wins!" : "Player 2 Wins!";
			g.drawString(winner, 230, 300);
			g.setFont(new Font("Arial", Font.PLAIN, 24));
			g.drawString("Press r to restart", 270, 350);
		}

		// Pause message
		if (paused && !gameOver) {
			g.setFont(new Font("Arial", Font.BOLD, 50));
			g.drawString("PAUSED", 300, 350);
			g.drawString("space to start", 300, 400);
			g.drawString("q to quit", 300, 450);
		}
	}

	public void actionPerformed(ActionEvent e) {

		if (gameOver || paused)
			return;

		// Player movement
		if (wPressed && player1Y > 0)
			player1Y -= 5;
		if (sPressed && player1Y < getHeight() - paddleHeight)
			player1Y += 5;
		if (twoPlayerMode && upPressed && player2Y > 0)
			player2Y -= 5;
		if (twoPlayerMode && downPressed && player2Y < getHeight() - paddleHeight)
			player2Y += 5;

		// AI movement if not two-player
		if (!twoPlayerMode) {
			if (player2Y + paddleHeight / 2 < ballY)
				player2Y += 4;
			else if (player2Y + paddleHeight / 2 > ballY)
				player2Y -= 4;
		}

		// Ball movement
		ballX += ballXSpeed;
		ballY += ballYSpeed;

		// Ball collision with top/bottom
		if (ballY <= 0 || ballY >= getHeight() - ballSize) {
			ballYSpeed *= -1;
		}

		// Ball collision with paddles
		if (ballX <= 20 && ballY + ballSize >= player1Y && ballY <= player1Y + paddleHeight) {
			ballXSpeed *= -1;
			paddleHeight--;
		}
		if (ballX + ballSize >= 780 && ballY + ballSize >= player2Y && ballY <= player2Y + paddleHeight) {
			ballXSpeed *= -1;
			paddleHeight--;
		}

		// Ball out of bounds
		if (ballX < 0) {
			player2Score++;
			resetBall();
		} else if (ballX > getWidth()) {
			player1Score++;
			resetBall();
		}

		if (player1Score == 10 || player2Score == 10) {
			gameOver = true;
		}

		repaint();
	}

	private void resetBall() {
		ballX = 390;
		ballY = 290;
		ballXSpeed *= -1;
		ballYSpeed = 4;
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_Q) {
			if (timer != null) {
				timer.stop();
			}
			frame.dispose();
			Games game = new Games();
			game.setVisible(true);
		}
		if (key == KeyEvent.VK_T) {
			twoPlayerMode = !twoPlayerMode;
		}
		if (key == KeyEvent.VK_UP)
			upPressed = true;
		if (key == KeyEvent.VK_DOWN)
			downPressed = true;
		if (key == KeyEvent.VK_W)
			wPressed = true;
		if (key == KeyEvent.VK_S)
			sPressed = true;

		// Toggle pause
		if (key == KeyEvent.VK_SPACE || key == KeyEvent.VK_P) {
			paused = !paused;
			repaint();
		}

		// Restart game
		if (gameOver && key == KeyEvent.VK_R) {
			paddleHeight = 100;
			player1Score = 0;
			player2Score = 0;
			gameOver = false;
			paused = false;
			timer.start();
		}
	}

	public void keyReleased(KeyEvent e) {
		int key = e.getKeyCode();

		if (key == KeyEvent.VK_UP)
			upPressed = false;
		if (key == KeyEvent.VK_DOWN)
			downPressed = false;
		if (key == KeyEvent.VK_W)
			wPressed = false;
		if (key == KeyEvent.VK_S)
			sPressed = false;
	}

	public void keyTyped(KeyEvent e) {
	}

	public static void main(String[] args) {
		new Pong4();
	}
}
