import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
		return (String) wordBank.get(n);
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

	public static void main(String[] args) {
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
	}
}
