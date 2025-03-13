import java.io.BufferedReader;
import java.io.FileReader;

class Wordle {
	public static final String words_file = "wordle.txt";

	public static String getWord() {
		int word_num = 0;
		String word = "";

		// Get wordle word at random from file
		try (BufferedReader reader = new BufferedReader(new FileReader(words_file))) {

			// First line of file contains total number of words in file
			if ((word = reader.readLine()) != null) {
				int tot_num_words = Integer.valueOf(word);
				// Get random word_num from tot_num_words in file
				word_num = (int) (Math.random() * tot_num_words);
			}

			// Skip word_num lines
			int n = 0;
			while ((word = reader.readLine()) != null && n < word_num) {
				n++;
			}

			if (word == null) {
				throw new RuntimeException("No word found");
			}

			// reader.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		return word;
	}

	public static void main(String[] args) {
		String word = "";
		word = getWord();
		// Print wordle word
		System.out.println(word);
	}
}
