import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;

/**
 * Play a wordle game.
 */
class TonyWordle extends JPanel {

	Rectangle bounds;
	Rectangle totalBounds;
	JScrollPane scrollPane;
	StringBuffer sb = new StringBuffer();
	JTextPane textPane;
	String word = getRandomWord();
	String guess = "";
	int guesses = 1;

	/**
	 * Set the <code>word</code> to a random word from a list in the
	 * <code>wordBankFile</code>
	 */
	public static String getRandomWord() {
		if (wordBank.size() == 0) {
			String word = "";

			// Open the words_file for reading by line
			try (BufferedReader reader = new BufferedReader(new FileReader(wordBankFile))) {

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

	/**
	 * Verify a word from a list in the
	 * <code>validWordsFile</code>
	 *
	 * @param s word to validate.
	 */
	public static boolean isValidWord(String s) {
		if (validWords.size() == 0) {
			String word = "";

			// Open the words_file for reading by line
			try (BufferedReader reader = new BufferedReader(new FileReader(validWordsFile))) {

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

	/**
	 * validWordsFile (possible guesses) and wordBankFile (possible solutions) store
	 * the word lists obtained from a github repo.
	 * "valid-words.csv" and "word-bank.csv" are from:
	 * https://github.com/seanpatlan/wordle-words These lists are accurate as of
	 * 2/7/22, but probably won't be once NYT officially takes over the game.
	 */
	public static final String validWordsFile = "valid-words.csv"; // Possible guesses
	public static final String wordBankFile = "word-bank.csv"; // Possible solutions

	public static Set<String> validWords = new HashSet<>();
	public static List<String> wordBank = new ArrayList<>();

	static JFrame frame;

	/**
	 * Color for terminal a green character.
	 * 
	 * @param c character to be colored.
	 * @return green character.
	 */
	public static String green(char c) {
		return "\u001b[1;102m" + c + "\u001b[m";
	}

	/**
	 * Color for terminal a yellow character.
	 * 
	 * @param c character to be colored.
	 * @return yellow character.
	 */
	public static String yellow(char c) {
		return "\u001B[0;43m" + c + "\u001b[m";
	}

	/**
	 * Instantiate a new <code>TonyWordle</code> object which starts a new
	 * <code>JFrame</code>.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		new TonyWordle();
	}

	/**
	 * Close the current GUI and open the main menu.
	 */
	void quit() {
		frame.dispose();
		Games game = new Games();
		game.setVisible(true);
	}

	/**
	 * Setup the GUI.
	 */
	TonyWordle() {
		Dimension d = new Dimension(800, 600); // ((int) scale * gameWidth), (int) (scale * gameHeight));

		Font font = new Font("Arial", Font.BOLD, 18);

		frame = new JFrame();
		frame.setPreferredSize(d);
		frame.setMinimumSize(d);
		frame.setMaximumSize(d);

		frame.setTitle("Wordle");
		frame.setLayout(new BorderLayout());
		textPane = new JTextPane();

		// output pane
		textPane.setFont(font);
		textPane.setEditable(false);
		bounds = new Rectangle(0, 0, 600, 400);
		totalBounds = new Rectangle(0, 0, d.width, d.height);
		textPane.setBounds(bounds);
		textPane.setContentType("text/html");
		textPane.setPreferredSize(new Dimension(bounds.width, bounds.height));
		// textPane.setMinimumSize(new Dimension(bounds.width, bounds.height));
		// textPane.setMaximumSize(new Dimension(bounds.width, bounds.height));
		scrollPane = new JScrollPane(textPane);
		frame.add(scrollPane, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel();
		// label for input
		JLabel jl = new JLabel();
		Dimension jld = new Dimension(200, 20);
		jl.setPreferredSize(jld);
		jl.setFont(font);
		jl.setText("Guess (q to quit):");
		inputPanel.add(jl);

		// user input field
		JTextField jt = new JTextField();
		Dimension jtd = new Dimension(300, 20);
		jt.setPreferredSize(jtd);
		// jt.setMaximumSize(jtd);
		// jt.setMinimumSize(jtd);
		jt.setFont(font);
		// jt.setText(word); // Show word for debugging
		jt.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent event) {
				guess(jt.getText());
				jt.setText("");
			}
		});
		inputPanel.add(jt);

		// Exit button
		JButton button = new JButton("Quit");
		button.setFont(font);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				quit();
			}
		});
		inputPanel.add(button);
		// inputPanel.setPreferredSize(jtd);
		// inputPanel.setMaximumSize(jtd);
		// inputPanel.setMinimumSize(jtd);

		frame.add(inputPanel, BorderLayout.SOUTH);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
		// enterFullScreen();
		// exitFullScreen();
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
		System.out.println("Starting wordle...");
	}

	/**
	 * Process the users guess and update the output.
	 * 
	 * @param guess
	 */
	void guess(String guess) {
		if (guesses == 1) {
			sb.setLength(0);
			sb.append(
					"<html><head><style>body { font-family: 'Arial'; font-size: 24px; } td { text-align: center; width: 40px; height: 40px; border: 1.5px solid; } </style></head>"
							+
							"<body>");
		}

		if (guess.equals("q")) {
			quit();
		}

		if (guess.length() != word.length()) {
			System.out.println("Guess must be " + word.length() + " letters");
			addEnd("Guess must be " + word.length() + " letters");
			return;
		}
		if (!isValidWord(guess)) {
			System.out.println("Invalid word");
			addEnd("Invalid word");
			return;
		}

		// User wins!
		if (guess.equals(word)) {
			System.out.println("Hooray! You got it in " + guesses + " tries");
			addSolution();
			addEnd("Hooray! You got it in " + guesses + " tries");
			guesses = 1;
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

		add("<table><tr>");
		// Loop through each char in guess
		nextCh: for (int i = 0; i < guess.length(); i++) {
			char c = guess.charAt(i);

			// Mark correct char in correct place green
			if (c == word.charAt(i)) {
				System.out.print(green(c));
				addChar(c, "green");
				continue;
			}

			// Mark correct char in incorrect place yellow
			for (int j = 0; j < word.length(); j++) {
				if (word.charAt(j) == c && !used[j]) {
					used[j] = true;
					System.out.print(yellow(c));

					addChar(c, "yellow");

					continue nextCh;
				}
			}

			// Mark incorrect char
			addChar(c, "");

			System.out.print(c);
		}

		System.out.println();
		add("</tr></table>");

		if (guesses > 6) {
			add("Too many guesses... the word was<br>");
			addSolution();
			System.out.println("Too many guesses... the word was " + word);
			guesses = 1;
			word = getRandomWord();
			addEnd("");
			return;
		}

		addEnd("");
		// System.out.println(sb.toString());
		// System.out.println(textPane.getText()); // formatted html
	}

	/**
	 * Create a delay in code execution.
	 * 
	 * @param m delay in milliseconds.
	 */
	public void delay(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception e) {
		}
	}

	/**
	 * Display the correct word to the output.
	 */
	void addSolution() {
		add("<table><tr>");
		// Loop through each char in guess
		for (int i = 0; i < word.length(); i++) {
			char c = word.charAt(i);
			addChar(c, "green");
			System.out.print(c);
		}
		add("</tr></table>");
		System.out.println();
	}

	/**
	 * Add text to the string buffer in charge of the output text area.
	 * 
	 * @param str string to append.
	 */
	void add(String str) {
		sb.append(str);
	}

	/**
	 * Close all the html tags with a table.
	 * 
	 * @param msg a message to display at the end of the html document.
	 */
	void addTableEnd(String msg) {
		textPane.setText(sb.toString() + "</tr></table><br>" + msg + "</body></html>");
		textPane.setCaretPosition(textPane.getDocument().getLength());
		textPane.paintImmediately(totalBounds);
	}

	/**
	 * Close all the html tags.
	 * 
	 * @param msg a message to display at the end of the html document.
	 */
	void addEnd(String msg) {
		textPane.setText(sb.toString() + "<br>" + msg + "</body></html>");
		textPane.setCaretPosition(textPane.getDocument().getLength());
		textPane.paintImmediately(totalBounds);
	}

	/**
	 * Add another character to the output with a dramatic delay at the end.
	 *
	 * @param c     char to add.
	 * @param color color of character to add.
	 */
	void addChar(char c, String color) {
		if (color.length() > 0) {
			add("<td style=background-color:" + color + ">" + c + "</td>");
		} else {
			add("<td>" + c + "</td>");
		}
		addTableEnd("");
		delay(100);
	}
}
