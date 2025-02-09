import java.io.*;
import java.awt.Color;
import javax.swing.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.GraphicsEnvironment;
import java.awt.Font;

public class StopwatchGui {
	static long start = System.nanoTime();
	static boolean running = false; // running

	public static void main(String[] argv) throws Exception {

		// Child thread
		new Thread(() -> { // Lambda Expression
			Console cnsl = System.console();
			if (cnsl == null) {
				System.out.println("No console available");
				System.exit(1);
			}

			String str;
			while (true) {
				str = cnsl.readLine("Enter command (h for help): ");

				if (str == null) {
					System.out.println("EOF Exit.");
					System.exit(1);
				}
				switch (str) {
					case "q":
						System.out.println("Exit.");
						System.exit(0);
						break;
					default:
						System.out.println("---------");
						System.out.println("h,?: help");
						System.out.println("q: quit");
						break;
				}
			}
			// for (int i = 1; i <= 5; i++) {
			// System.out.println("Child Thread: " + i);
			// try {
			// Thread.sleep(500);
			// } catch (Exception e) {
			// e.printStackTrace();
			// }
			// }
		}).start();
		if (GraphicsEnvironment.isHeadless()) {
			System.err.println("This program needs graphics capabilities. Exiting...");
			System.exit(1);
		}
		System.out.println("Switch to the GUI window to see the program...");
		runStopwatch();
	}

	static JLabel setLabel(String str) {
		final int lineSpace = 40;
		JLabel jl = new JLabel(str);
		// jl.setFont(new Font("Verdana", Font.PLAIN, 20));
		jl.setFont(new Font("Monospace", Font.PLAIN, 20));
		jl.setBounds(20, lineSpace * line++ + 10, 700, 40);
		jl.setForeground(Color.white);
		frame.add(jl);
		return jl;
	}

	static JFrame frame = new JFrame();
	static int line = 0;
	static boolean spaceStart = true;

	private static synchronized void runStopwatch() {
		final int delay = 10; // delay to update running timer so cpu is not spinning
		frame.getContentPane().setBackground(Color.black);
		// System.setProperty("awt.useSystemAAFontSettings", "lcd");
		frame.setSize(700, 800);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		// Create a Font object with desired font family, style, and size
		Font customFont = new Font("Monospace", Font.BOLD, 30);

		// Set the font for the JFrame (will affect components without their own font
		// set)
		frame.setFont(customFont);

		final JLabel lTime = setLabel("");
		final JLabel lHelp1 = setLabel("Cube timer: Release space-bar to start and press space-bar to stop");
		final JLabel lHelp2 = setLabel(
				"G to start and S to stop, Q to quit \u0041 \u25c6 " + new String(Character.toChars(0x1f0c1)));
		// Set the font for the JLabel
		lHelp2.setFont(customFont);

		int aceSpades = 0x1f0a1;
		int aceHearts = 0x1f0b1;
		int aceDiamonds = 0x1f0c1;
		int aceClubs = 0x1f0d1;
		int[] aces = { aceClubs, aceDiamonds, aceHearts, aceSpades };
		String cards = "";
		for (int j = 0; j < 4; j++) {
			// System.out.println("suit: " + suit);
			for (int i = 0; i < 14; i++) {
				if (i == 11) // skip C
					continue;
				cards += new String(Character.toChars(aces[j] + i));
			}
			cards += "\n";
		}

		final int lineHeight = 40;
		JLabel lHelp3 = new JLabel(cards);
		// jl.setFont(new Font("Verdana", Font.PLAIN, 20));
		lHelp3.setFont(new Font("Monospace", Font.PLAIN, 20));
		lHelp3.setBounds(20, lineHeight * line++ + 10, 700, 400);
		lHelp3.setForeground(Color.white);
		frame.add(lHelp3);

		lHelp1.setIcon(null); // to hide lsp warning about unused variable
		lHelp2.setIcon(null); // to hide lsp warning about unused variable

		frame.addKeyListener(new KeyAdapter() {
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_SPACE) {
					if (!running && !spaceStart) {
						spaceStart = true;
						return;
					}
					if (!running) {
						start = System.nanoTime();
						running = true;
						spaceStart = true;
					} else {
					}
				}
			}

			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (keyCode == KeyEvent.VK_SPACE) {
					if (!running && !spaceStart) {
						return;
					}
					if (running && spaceStart) {
						running = false;
						spaceStart = false;
					}
				} else if (keyCode == KeyEvent.VK_G) {
					running = true;
					spaceStart = false;
					start = System.nanoTime();
				} else if (keyCode == KeyEvent.VK_S) {
					if (running) {
						running = false;
						updateTime(lTime);
						spaceStart = true;
					}
				} else if (keyCode == KeyEvent.VK_Q) {
					System.out.println("q-Key pressed. Exit.");
					System.exit(0);
				}
			}
		});
		frame.setVisible(true);

		while (true) {
			if (running) {
				updateTime(lTime);
			}
			sleep(delay);
		}
	}

	static void updateTime(JLabel jl) {
		long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
		jl.setText(String.format("%,d Second %03d Millisecond\n",
				elapsedMs / 1_000L,
				elapsedMs % 1000L));
	}

	static void sleep(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
		}
	}
}
