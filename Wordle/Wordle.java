import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

class Wordle {
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
		return "\u001b[1;32m" + c + "\u001b[m";
	}

	public static String yellow(char c) {
		return "\u001b[1;33m" + c + "\u001b[m";
	}

	public static void main(String[] args) {
		// Demo begin
		// Print wordle word
		System.out.println("Random word: " + getRandomWord());
		// Print wordle word
		System.out.println("Random word: " + getRandomWord());
		// Check for valid word
		System.out.println("Is \"hello\" valid guess: " + isValidWord("hello"));
		System.out.println("Is \"zzzzz\" valid guess: " + isValidWord("zzzzz"));
		// Info about both word groups
		System.out.println("Total solution words   : " + wordBank.size());
		System.out.println("Total valid guess words: " + validWords.size());
		// Demo end

		// Game begin
		System.out.println("\nBegin of wordle game");
		Scanner scan = new Scanner(System.in);

		// Wordle word
		String word = getRandomWord();
		// word = "abbey";
		System.out.println("Word: " + word);

		// User guess
		String guess = "";

		int guesses = 0; // number of guesses
		while (true) {
			guesses++;
			while (true) {
				System.out.print("Enter guess " + guesses + ": ");
				guess = scan.nextLine();
				if (guess.length() != word.length()) {
					System.out.println("Guess must be " + word.length() + " letters, try again...");
					continue;
				}
				if (!isValidWord(guess)) {
					System.out.println("Invalid word, try again...");
					continue;
				}
				break;
			}

			// User wins!
			if (guess.equals(word)) {
				System.out.println("Hooray! You got it in " + guesses + " tries");
				break;
			}

			// Array to mark used characters in word from guess chars
			boolean[] used = new boolean[word.length()];

			// Loop through each char in guess
			nextCh: for (int i = 0; i < guess.length(); i++) {
				char c = guess.charAt(i);

				// Mark correct char in correct place green
				if (c == word.charAt(i)) {
					used[i] = true;
					System.out.print(green(c));
					continue;
				}

				// Mark correct char in incorrect place yellow
				for (int j = 0; j < word.length(); j++) {
					if (word.charAt(j) == c && !used[j]) {
						used[j] = true;
						System.out.print(yellow(c));
						continue nextCh;
					}
				}

				// Mark incorrect char
				System.out.print(c);
			}
			System.out.println();

			if (guesses >= 100) {
				System.out.println("Too many guesses");
				break;
			}
		}
		scan.close();
	}
}
