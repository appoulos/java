import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
// import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
// import java.util.Scanner;
import java.util.Set;

// import javax.swing.Box;
// import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
// import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

class TonyWordle extends JPanel {

	// JLabel jlMsg;
	Rectangle bounds;
	JScrollPane scrollPane;
	StringBuffer sb = new StringBuffer();
	JTextPane textPane;
	String word = getRandomWord();
	String guess = "";
	int guesses = 1; // number of guesses

	public static String getRandomWord() {
		if (wordBank.size() == 0) {
			String word = "";

			// Open the words_file for reading by line
			try (BufferedReader reader = new BufferedReader(new FileReader(word_bank_file))) {

				// fill the dictionary with words
				while ((word = reader.readLine()) != null) {
					wordBank.add(word);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		int n = (int) (Math.random() * wordBank.size());
		return wordBank.get(n);
	}

	public static boolean isValidWord(String s) {
		if (validWords.size() == 0) {
			String word = "";

			// Open the words_file for reading by line
			try (BufferedReader reader = new BufferedReader(new FileReader(valid_words_file))) {

				// fill the dictionary with words
				while ((word = reader.readLine()) != null) {
					validWords.add(word);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		return validWords.contains(s);
	}

	// "valid-words.csv" and "word-bank.csv" are from:
	// https://github.com/seanpatlan/wordle-words
	// These lists are accurate as of 2/7/22, but probably won't be once NYT
	// officially takes over the game.
	public static final String valid_words_file = "valid-words.csv"; // Possible guesses
	public static final String word_bank_file = "word-bank.csv"; // Possible solutions

	public static Set<String> validWords = new HashSet<>();
	public static List<String> wordBank = new ArrayList<>();

	public static String green(char c) {
		return "\u001b[1;102m" + c + "\u001b[m";
	}

	public static String yellow(char c) {
		return "\u001B[0;43m" + c + "\u001b[m";
	}

	static JFrame frame;

	public static void main(String[] args) {
		new TonyWordle();
	}

	void quit() {
		System.exit(0);
		frame.dispose();
		Games game = new Games();
		game.setVisible(true);
	}

	TonyWordle() {
		// GraphicsEnvironment graphicsEnvironment =
		// GraphicsEnvironment.getLocalGraphicsEnvironment();
		// int screenWidth = graphicsEnvironment.getMaximumWindowBounds().width;
		// int screenHeight = graphicsEnvironment.getMaximumWindowBounds().height;

		// GraphicsDevice device = graphicsEnvironment.getDefaultScreenDevice();
		// origFrameRate = device.getDisplayMode().getRefreshRate();
		// System.out.println("refresh rate: " + origFrameRate);
		// frameRate = origFrameRate;

		// int ignoreDeadCode = 0;
		//
		// if ((double) gameWidth / gameHeight >= (double) screenWidth / screenHeight +
		// ignoreDeadCode) {
		// scale = (double) screenWidth / gameWidth;
		// } else {
		// scale = (double) screenHeight / gameHeight;
		// }

		Dimension d = new Dimension(800, 600); // ((int) scale * gameWidth), (int) (scale * gameHeight));

		setPreferredSize(d);
		setMinimumSize(d);
		setMaximumSize(d);

		Font font = new Font("Arial", Font.BOLD, 18);

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

		// this.addKeyListener(this);
		// frame.addKeyListener(this);

		textPane = new JTextPane();
		textPane.setFont(font);
		textPane.setEditable(false);
		bounds = new Rectangle(0, 0, 600, 400);
		textPane.setBounds(0, 0, 600, 400);
		textPane.setContentType("text/html");
		// box.add(textArea);
		textPane.setPreferredSize(new Dimension(600, 400));
		textPane.setMinimumSize(new Dimension(600, 400));
		scrollPane = new JScrollPane(textPane);
		frame.add(scrollPane, BorderLayout.NORTH);

		JLabel jl = new JLabel();
		Dimension jld = new Dimension(200, 20);
		jl.setPreferredSize(jld);
		jl.setFont(font);
		jl.setText("Guess (q to quit):");
		// box.add(jl);
		frame.add(jl, BorderLayout.WEST);

		// jlMsg = new JLabel();
		// Dimension jldMsg = new Dimension(200, 20);
		// jlMsg.setPreferredSize(jldMsg);
		// jlMsg.setFont(font);
		// message("hi");
		// // box.add(jl);
		// frame.add(jl, BorderLayout.SOUTH);

		JTextField jt = new JTextField();
		Dimension jtd = new Dimension(300, 20);
		jt.setPreferredSize(jtd);
		jt.setMaximumSize(jtd);
		jt.setMinimumSize(jtd);
		jt.setFont(font);
		jt.setText("abbey");
		jt.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent event) {
				// guess = jt.getText();
				guess(jt.getText());
				// jt.setText("");
			}
		});

		// box.add(jt);
		frame.add(jt, BorderLayout.CENTER);

		JButton button = new JButton("Back to main menu...");
		button.setFont(font);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// System.exit(0);
				quit();
			}
		});
		// box.add(button);
		frame.add(button, BorderLayout.SOUTH);

		// frame.add(box);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		jt.requestFocusInWindow();
		// Demo begin
		// Print wordle word
		// System.out.println("Random word: " + getRandomWord());
		// Print wordle word
		// System.out.println("Random word: " + getRandomWord());
		// Check for valid word
		// System.out.println("Is \"hello\" valid guess: " + isValidWord("hello"));
		// System.out.println("Is \"zzzzz\" valid guess: " + isValidWord("zzzzz"));
		// Info about both word groups
		// System.out.println("Total solution words : " + wordBank.size());
		// System.out.println("Total valid guess words: " + validWords.size());
		// Demo end

		// Game begin
		// textArea.setText("Starting wordle...");
		System.out.println("Starting wordle...");
		// add("Enter guess " + guesses + " (q to quit): ");
	}

	void guess(String guess) {
		// System.out.print("Enter guess " + guesses + " (q to quit): ");
		if (guesses == 1) {
			sb.setLength(0);
			sb.append(
					"<html><head><style>body { font-family: 'Arial'; font-size: 24px; } td { text-align: center; width: 40px; height: 40px; border: 1.5px solid; } </style></head>"
							+
							"<body>");
		}

		if (guess.equals("q")) {
			quit();
			// System.exit(0);
			// Games games = new Games();
			// games.setVisible(true);
			// System.exit(0);
		}

		if (guess.length() != word.length()) {
			// add("Guess must be " + word.length() + " letters" + "<br>");
			System.out.println("Guess must be " + word.length() + " letters");
			addEnd("Guess must be " + word.length() + " letters");
			return;
		}
		if (!isValidWord(guess)) {
			// add("Invalid word" + "<br>");
			System.out.println("Invalid word");
			addEnd("Invalid word");
			return;
		}

		// User wins!
		if (guess.equals(word)) {
			// add("Hooray! You got it in " + guesses + " tries" + "<br>");
			System.out.println("Hooray! You got it in " + guesses + " tries");
			guesses = 1;
			addEnd("Hooray! You got it in " + guesses + " tries");
			return;
		}

		guesses++;
		// Array to mark used characters in word from guess chars
		boolean[] used = new boolean[word.length()];

		// Mark used array with correct letters
		for (int i = 0; i < guess.length(); i++) {
			char c = guess.charAt(i);

			// Mark correct char in correct places as used
			if (c == word.charAt(i)) {
				used[i] = true;
				continue;
			}
		}

		// String str = "";
		add("<table><tr>");
		// Loop through each char in guess
		nextCh: for (int i = 0; i < guess.length(); i++) {
			char c = guess.charAt(i);

			// Mark correct char in correct place green
			if (c == word.charAt(i)) {
				System.out.print(green(c));
				// str += "<span style=background-color:green>" + c + "</span>";
				addChar(c, "green");
				continue;
			}

			// Mark correct char in incorrect place yellow
			for (int j = 0; j < word.length(); j++) {
				if (word.charAt(j) == c && !used[j]) {
					used[j] = true;
					System.out.print(yellow(c));

					// str += "<span style=background-color:yellow>" + c + "</span>";
					addChar(c, "yellow");

					continue nextCh;
				}
			}

			// Mark incorrect char
			addChar(c, "");

			// str += c;
			System.out.print(c);
		}

		System.out.println();
		add("</tr></table>");

		if (guesses >= 3) {
			add("Too many guesses... the word was<br>");
			addSolution(word);
			System.out.println("Too many guesses... the word was " + word);
			guesses = 1;
			word = getRandomWord();
			addEnd("");
			// System.out.println(textPane.getText());
			return;
		}

		addEnd("");
		// System.out.println(sb.toString());
		// System.out.println(textPane.getText());
	}

	public void delay(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception e) {
		}
	}

	void addSolution(String guess) {
		add("<table><tr>");
		// Loop through each char in guess
		for (int i = 0; i < guess.length(); i++) {
			char c = guess.charAt(i);
			addChar(c, "green");
			System.out.print(c);
		}
		add("</tr></table>");
		System.out.println();
	}

	void add(String str) {
		sb.append(str);
		// textPane.setText(sb.toString());
		// textPane.setCaretPosition(textPane.getDocument().getLength());
	}

	void addTableEnd(String msg) {
		textPane.setText(sb.toString() + "</tr></table><br>" + msg + "</body></html>");
		textPane.setCaretPosition(textPane.getDocument().getLength());
		textPane.paintImmediately(bounds);
	}

	void addEnd(String msg) {
		textPane.setText(sb.toString() + "<br>" + msg + "</body></html>");
		textPane.setCaretPosition(textPane.getDocument().getLength());
		textPane.paintImmediately(bounds);
	}

	void addChar(char c, String color) {
		if (color.length() > 0) {
			add("<td style=background-color:" + color + ">" + c + "</td>");
		} else {
			add("<td>" + c + "</td>");
		}
		addTableEnd("");
		delay(100);
	}

	// void message(String msg) {
	// jlMsg.setText(msg);
	// }
}
