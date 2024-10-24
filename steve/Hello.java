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

	boolean palin(String str) {
		str = str.toLowerCase();
		String forward = "";
		String rev = "";
		char c;
		for (int i = 0; i < str.length(); i++) {
			c = str.charAt(i);
			if (Character.isLetterOrDigit(c)) {
				forward += c;
			}
			c = str.charAt(str.length() - i - 1);
			if (Character.isLetterOrDigit(c)) {
				rev += c;
			}
		}
		return forward.equals(rev);
	}

	public static int countOccurrences(String str, String substr) {
		int count = 0;
		int index = 0;

		while (index != -1) {
			index = str.indexOf(substr, index);
			if (index != -1) {
				count++;
				index += substr.length();
			}
		}

		return count;
	}

	boolean equalIsNot(String str) {
		int is = countOccurrences(str, "is");
		int not = countOccurrences(str, "not");
		return is == not;
	}

	public void run() {
		String str = "H .elleh";
		println("palin " + str + ": " + palin(str));
		str = "ab%$!1ba";
		println("palin " + str + ": " + palin(str));
		str = "is is not nt";
		println("equalisnot: " + equalIsNot(str));
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
