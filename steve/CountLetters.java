class CountLetters {
	public static void main(String[] args) {
		// Problem: print all the characters in the string s
		String s = "The quick brown fox";

		// Solution:
		// b c e f h i k n o q r t u w x
		int[] freq = new int[26];

		s = s.toLowerCase();
		for (int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if (Character.isLetter(c)) {
				freq[c - 'a']++;
			}
		}

		for (int i = 0; i < freq.length; i++) {
			if (freq[i] != 0) {
				// All on one line
				System.out.print((char) ('a' + i) + " ");

				// With frequency of the letter on each line:
				// System.out.println((char) ('a' + i) + ": " + freq[i]);
			}
		}
		System.out.println();

	}
}
