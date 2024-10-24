public class Hello extends ConsoleProgram {
	int min_if(int a, int b, int c) {
		if (a < b)
			if (a < c)
				return a;
			else
				return c;
		else if (b < c)
			return b;
		return c;
	}

	int min_ternary(int a, int b, int c) {
		// One liner:
		// return ((a < b) ? ((a < c) ? a : c) : (b < c) ? b : c);
		return (a < b)
				? (a < c)
						? a
						: c
				: (b < c)
						? b
						: c;
	}

	public String yell(String statement) {
		statement = statement.toUpperCase();
		return statement;
	}

	public String repeatString(String text, int numReps) {
		String output = "";
		for (; numReps > 0; numReps--) {
			output += text;
		}
		return output;
	}
	// public String repeatString(String text, int numReps) {
	// String output = "";
	// for (int i = 0; i < numReps; i++) {
	// output += text;
	// }
	// return output;
	// }

	public boolean isInteger(String str) {
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	// switch (brackets.charAt(i)) {
	// case '{' -> cnt++;
	// case '}' -> cnt--;
	// }

	// if (brackets.charAt(i) == '{') {
	// cnt++;
	// } else if (brackets.charAt(i) == '}') {
	// cnt--;
	// }

	boolean bracketsMatch(String brackets) {
		int cnt = 0;
		for (int i = 0; i < brackets.length(); ++i) {

			switch (brackets.charAt(i)) {
				case '{':
					cnt++;
					break;
				case '}':
					cnt--;
					break;
			}

			if (cnt < 0) {
				return false;
			}
		}
		if (cnt != 0) {
			return false;
		}
		return true;
	}

	boolean passwordValid(String str) {
		if (str.length() < 8) {
			return false;
		}
		for (int i = 0; i < str.length(); i++) {
			if (!Character.isLetterOrDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}

	boolean palinRev(String str) {
		String strCleaned = "";
		String rev = "";

		str = str.toLowerCase();

		// Make strCleaned with only letters and digits
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				strCleaned += c;
			}
		}

		// Empty string is not paindrome
		if (strCleaned.length() == 0)
			return false;
		// println("cleaned: " + strCleaned);

		// Reverse string
		for (int i = strCleaned.length() - 1; i >= 0; i--) {
			rev += strCleaned.charAt(i);
		}
		// println("rev: " + rev);

		return strCleaned.equals(rev);
	}

	boolean palin(String str) {
		int len = str.length();
		if (len == 0)
			return false;

		int p_end = len - 1;
		int p_b = 0;
		int p_e = p_end;

		str = str.toLowerCase();

		while (p_b < p_e) {
			// Find next char/letter from left
			while (p_b < p_e && !Character.isLetterOrDigit(str.charAt(p_b))) {
				p_b++;
			}
			// Find next char/letter from right
			while (p_e > p_b && !Character.isLetterOrDigit(str.charAt(p_e))) {
				p_e--;
			}
			if (p_b < p_e && str.charAt(p_b) != str.charAt(p_e)) {
				// println("!!!");
				return false;
			}
			p_b++;
			p_e--;
		}

		// No characters is not palindrome
		// if (len == 0 || !Character.isLetterOrDigit(str.charAt(p_b))) {
		// println("###");
		// return false;
		// }

		return true;
	}

	int countOcc(String str, String substr) {
		int substrLen = substr.length();
		int cnt = 0;
		int ndx = 0;
		while (true) {
			ndx = str.indexOf(substr, ndx);
			if (ndx == -1) {
				break;
			}
			cnt++;
			ndx += substrLen;
		}
		return cnt;
	}

	int countOc(String str, String substr) {
		int strLen = str.length();
		int substrLen = substr.length();
		int cnt = 0;
		int p = 0;
		for (int i = 0; i < strLen; i++) {
			if (str.charAt(i) == substr.charAt(p)) {
				if (p >= substrLen - 1) {
					cnt++;
					p = 0;
				} else {
					p++;
				}
			} else {
				p = 0;
			}
		}
		return cnt;
	}

	boolean equalIsNot(String str) {
		// int is = countOccurrences(str, "is");
		// int not = countOccurrences(str, "not");
		int is = countOc(str, "is");
		int not = countOc(str, "not");
		return is == not;
	}

	void test_palin() {
		String[] t = { "a", "bc", "aba", "$", "$a$", "$aa$b$" };
		for (int i = 0; i < t.length; i++) {
			// println("Testing `" + t[i] + "`:");
			println("palin(\"" + t[i] + "\"):" + palin(t[i]));
			println("palinRev(\"" + t[i] + "\"):" + palinRev(t[i]));
		}
		println();
	}

	public void run() {
		test_palin();
		String str;
		// println("palin " + str + ": " + palinRev(str));
		// str = "ab%$!1ba";
		// println("palin " + str + ": " + palinRev(str));
		// str = "1";
		// println("palin " + str + ": " + palinRev(str));
		// str = "%";
		// println("palin " + str + ": " + palinRev(str));
		// str = "";
		// println("palin " + str + ": " + palinRev(str));
		str = "is is not not";
		println("equalisnot: " + str + ": " + equalIsNot(str));
		str = "is s not not";
		println("equalisnot: " + str + ": " + equalIsNot(str));

		if (true)
			return;
		println("asdf" + (passwordValid("asdf") ? " is" : " is not"));
		println("asdf1234" + (passwordValid("asdf1234") ? " is" : " is not"));
		println("asdf4#21" + (passwordValid("asdf4#21") ? " is" : " is not"));
		if (bracketsMatch("{asdf}")) {
			println("matched");
		} else {
			println("not matched");
		}
		if (bracketsMatch("{a")) {
			println("matched");
		} else {
			println("not matched");
		}
		if (bracketsMatch("{{{{}}}}")) {
			println("matched");
		} else {
			println("not matched");
		}

		System.exit(0);
		if (isInteger("123")) {
			println("123 is integer");
		}
		if (!isInteger("asdf")) {
			println("asdf is not an integer");
		}
		println(repeatString("hey", 3));
		println(yell("hello"));
		String s = "hello";
		for (int i = 0; i < s.length(); ++i) {
			println(s.charAt(i));
		}
		println(min_if(3, 4, 2));
		println(min_ternary(3, 4, 2));
	}
}
