import java.util.HashMap;
import java.util.Arrays;
import java.util.Scanner;
import java.util.ArrayList;

@SuppressWarnings("deprecation")

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

	int someMethod(int x, int y) {
		int sum = 0;
		while (x < 10) {
			sum += x % y;
			x++;
			y++;
		}
		return sum;
	}

	void dist() {
		double l1la = Math.toRadians(48.8567);
		double l1lo = Math.toRadians(2.3508);
		double l2la = Math.toRadians(51.5072);
		double l2lo = Math.toRadians(0.1275);
		double radius = 3963.1676;
		println("dist: " + radius
				* (Math.acos(
						Math.sin(l1la) * Math.sin(l2la) + Math.cos(l1la) * Math.cos(l2la) * Math.cos(l1lo - l2lo))));
	}

	class Horse {
		String name;
		int a;
		int b;

		Horse() {
			a = 0;
			b = 0;
		}

		Horse(String name) {
			this.name = name;
		}

		Horse(int x, int y) {
			a = x;
			b = y;
		}

		public int add(int x, int y) {
			a += x;
			b += y;
			return a + b;
		}

		@Override
		public String toString() {
			return "name: " + name + ", a: " + a + ", b: " + b + ", ptr: " + super.toString();
		}

		// @Override
		// protected void finalize() throws Throwable {
		// try {
		// // Cleanup code here (e.g., closing resources)
		// println("cleaning up: " + this);
		// } finally {
		// super.finalize();
		// }
		// }
	}

	public Horse makeHorse() {
		return new Horse();
	}

	int hget(Horse h) {
		h.a++;
		return h.a + h.b;
	}

	double round(double d, int n) {
		return Math.round(d * Math.pow(10, n)) / Math.pow(10, n);
	}

	public int sum13b(int[] nums) {
		int count = 0;
		boolean found13 = false;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == 13)
				found13 = true;
			else if (found13)
				found13 = false;
			else
				count += nums[i];
		}
		return count;
	}

	public int sum13(int[] nums) {
		int count = 0;
		boolean found13 = false;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] == 13) {
				found13 = true;
				continue;
			}
			if (found13) {
				found13 = false;
				continue;
			}
			count += nums[i];
		}
		return count;
	}

	public int sum67a(int[] nums) {
		int count = 0;
		boolean found6 = false;
		for (int i = 0; i < nums.length; i++) {
			// start ignoring
			if (nums[i] == 6) {
				found6 = true;
				continue;
			}
			// stop ignoring
			if (found6 && nums[i] == 7) {
				found6 = false;
				continue;
			}
			if (!found6) {
				count += nums[i];
			}
		}
		return count;
	}

	public int sum67(int[] nums) {
		int count = 0;
		boolean found6 = false;
		for (int i = 0; i < nums.length; i++) {
			if (found6) {
				if (nums[i] == 7)
					// stop ignoring
					found6 = false;
			} else {
				if (nums[i] == 6) {
					// start ignoring
					found6 = true;
				} else {
					count += nums[i];
				}
			}
		}
		return count;
	}

	public int[] makeArray(int num) {
		return new int[num];
	}

	public int digitOfPi(int digit) {
		if (digit == 0)
			return 3;
		return Integer.parseInt(
				Double.toString(Math.PI).substring(
						digit + 1, digit + 2));
	}

	public class Span {
		int first;
		int last;

		Span(int f, int l) {
			first = f;
			last = l;
		}
	}

	public int maxSpanHash(int[] nums) {
		HashMap<Integer, Span> spans = new HashMap<Integer, Span>();
		for (int i = 0; i < nums.length; i++) {
			if (!spans.containsKey(nums[i])) {
				spans.put(nums[i], new Span(i, -1));
				continue;
			}
			Span s = spans.get(nums[i]);
			s.last = i;
			spans.put(nums[i], s);
		}

		int max = 0;
		for (Span s : spans.values()) {
			if (s.last == -1) {
				if (max < 1) {
					max = 1;
				}
				continue;
			}
			int diff = s.last - s.first + 1;
			if (max < diff)
				max = diff;
		}

		return max;
	}

	public int maxSpan2(int[] nums) {
		if (nums.length == 0)
			return 0;
		// find largest integer in nums array and enforce nums values >= 0
		int maxValue = -1;
		for (int i = 0; i < nums.length; i++) {
			if (nums[i] < 0)
				return 0;
			if (nums[i] > maxValue)
				maxValue = nums[i];
		}

		// create arrays for first and last locations of integers in nums
		int[] first = new int[maxValue + 1];
		for (int i = 0; i < first.length; i++)
			first[i] = -1;

		int[] last = new int[maxValue + 1];
		for (int i = 0; i < last.length; i++)
			last[i] = -1;

		// process nums array
		for (int i = 0; i < nums.length; i++) {
			int j = nums[i];
			if (first[j] == -1) {
				first[j] = i;
			} else {
				last[j] = i;
			}
		}

		// find largest span
		int maxSpan = 0;
		for (int i = 0; i < first.length; i++) {
			if (first[i] == -1) {
				continue;
			}
			if (last[i] == -1) {
				if (maxSpan == 0) {
					maxSpan = 1;
				}
			} else {
				int diff = last[i] - first[i] + 1;
				if (diff > maxSpan) {
					maxSpan = diff;
				}
			}
		}

		return maxSpan;
	}

	public int maxSpan(int[] nums) {
		int max = 0;
		for (int i = 0; i < nums.length; i++) {
			// for (int j = nums.length - 1; j >= i && max < j - i + 1; j--) {
			for (int j = nums.length - 1; j >= i; j--) {
				if (nums[i] == nums[j]) {
					if (max < j - i + 1) {
						max = j - i + 1;
					}
					break;
				}
				if (max >= j - i + 1) {
					println("optimized: j: " + j + ", i: " + i + ", max: " + max + ", len: " + nums.length);
					break;
				}
			}
			// ***ooooo
			// i_____jX
			// 01234567
			if (max > nums.length - i) {
				println("optimized: i: " + i + ", max: " + max + ", len: " + nums.length);
				break;
			}
		}
		return max;
	}

	public int chances(int... args) {
		int tot = 0;
		for (int num : args) {
			tot += num;
		}
		int rnd = (int) (Math.random() * tot + 1);
		// println("tot: " + tot + ", rnd: " + rnd);
		int sum = 0;
		int cnt = 0;
		for (int num : args) {
			cnt++;
			sum += num;
			if (rnd <= sum)
				return cnt;
		}
		return 0;
	}

	boolean chance(int num, int denom) {
		int ran = (int) (Math.random() * denom + 1);
		// println("ran: " + ran);
		if (ran <= num)
			return true;
		return false;
	}

	static class Hi {
		Hi() {
			System.out.println("hi");
		}
	}

	int count11(String str) {
		if (str.length() < 2) {
			return 0;
		}
		int found = (str.charAt(0) == '1' && str.charAt(1) == '1') ? 1 : 0;
		if (str.length() == 2) {
			return found;
		}
		return found + count11(str.substring(1 + found));
	}

	int count11s(String str) {
		// static int count = 0;
		if (str.length() < 2) {
			return 0;
		}
		int found = (str.charAt(0) == '1' && str.charAt(1) == '1') ? 1 : 0;
		if (str.length() == 2) {
			return found;
		}
		return found + count11(str.substring(1 + found));
	}

	int count11b(String str) {
		if (str.length() <= 2) {
			return str.equals("11") ? 1 : 0;
		}
		if (str.substring(0, 2).equals("11")) {
			return 1 + count11(str.substring(2));
		} else {
			return count11(str.substring(1));
		}
	}

	int count7(int n) {
		println("n=" + n);
		// base case
		if (n < 10) {
			println("found 7");
			return (n == 7) ? 1 : 0;
		}
		// found 7
		if ((n % 10) == 7) {
			println("found 7b");
			return 1 + count7(n / 10);
		}
		// not currently looking at a 7
		return count7(n / 10);
	}

	int sum(int n) {
		if (n < 10)
			return n;
		return n % 10 + sum(n / 10);
	}

	int modify(int n) {
		n = n / 10;
		return n;
	}

	class MyInt {
		int i = 123;

		MyInt(int i) {
			this.i = i;
		}
	}

	MyInt modify2(MyInt n) {
		n.i = n.i / 10;
		return n;
		// if (n < 10)
		// return n;
		// return n % 10 + sum(n / 10);
	}

	int fib(int n) {
		if (n <= 1) {
			// for (StackTraceElement ste : Thread.currentThread().getStackTrace()) {
			// System.out.println(ste + "\n");
			// }
			return n;
		}
		return fib(n - 1) + fib(n - 2);
	}

	boolean groupSum6(int start, int[] nums, int target) {
		// base case no numbers left
		if (nums.length - start == 0)
			return target == 0;
		// use all 6's
		if (nums[start] == 6)
			return groupSum6(start + 1, nums, target - 6);
		return /* skip number */ groupSum6(start + 1, nums, target) ||
		/* use number */ groupSum6(start + 1, nums, target - nums[start]);
	}

	static int cnt = 0;

	boolean groupSum(int start, int[] nums, int target) {
		cnt++;
		// base case no numbers left
		if (nums.length - start == 0)
			return target == 0;
		return /* skip number */ groupSum(start + 1, nums, target) ||
		/* use number */ groupSum(start + 1, nums, target - nums[start]);
	}

	boolean groupSumIter(int start, int[] nums, int target) {
		if (target == 0)
			return true;
		int sum;
		int max = (int) Math.pow(2, nums.length);
		int j;
		int n;
		for (int i = 0; i < max; i++) {
			sum = 0;
			j = 0;
			for (n = i; n > 0; n /= 2) {
				if (n % 2 == 1) {
					sum += nums[j];
					if (sum == target) {
						return true;
					}
				}
				j++;
			}
		}
		return false;
	}

	public void run() {
		for (int i = 10; i < 30; i++) {
			int[] nums = new int[i]; // { 1, 2, 6 };
			for (int j = 0; j < i; j++) {
				nums[j] = j;
			}
			int target = 300;
			// println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " +
			// groupSum6(0, nums, target));
			cnt = 0;
			println("groupSum(0, " + i + ", " + target + ") == " + groupSum(0, nums,
					target) + ", cnt: " + cnt);
			// println("groupSumIter(0, " + i + ", " + target + ") == " + groupSumIter(0,
			// nums, target));
		}

		System.exit(0);
		int[] nums = { 1, 2, 6 };
		int target = 6;
		println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " + groupSum6(0, nums, target));
		target = 8;
		println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " + groupSum6(0, nums, target));
		target = 10;
		println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " + groupSum6(0, nums, target));
		target = 0;
		println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " + groupSum6(0, nums, target));
		nums = new int[] {};
		target = 0;
		println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " + groupSum6(0, nums, target));
		target = 1;
		println("groupSum6(0, " + Arrays.toString(nums) + ", " + target + ") == " + groupSum6(0, nums, target));
		int nFib = 3;
		println("fib(" + nFib + ") == " + fib(nFib));
		int n7 = 707;
		MyInt nInteger = new MyInt(123);
		println("sum(" + n7 + ") == " + sum(n7));
		println("modify(" + n7 + ") == " + modify(n7));
		println("n7: " + n7);
		println("modify2(" + nInteger.i + ") == " + modify2(nInteger).i);
		println("nInteger: " + nInteger.i);
		println("count7(" + n7 + ") == " + count7(n7));
		String str = "110111011111";
		println(" count11(\"" + str + "\") == " + count11(str));
		println("count11b(\"" + str + "\") == " + count11b(str));
		new Hi();
		println("Enter any character and press return");
		Scanner scan = new Scanner(System.in);
		char char_ = scan.findWithinHorizon(".", 0).charAt(0);
		// char char_ = scan.nextLine().charAt(0);
		println("got: " + char_);
		println("2. Enter any character and press return");
		char_ = scan.findWithinHorizon(".", 0).charAt(0);
		// char char_ = scan.nextLine().charAt(0);
		println("got: " + char_);
		scan.close();
		ArrayList<Integer> al = new ArrayList<>();
		al.add(7);
		al.add(8);
		// concurrent abort
		// int j=0;
		// for (int k: al) {
		// if (j==1) al.add(9);
		// println("arr("+j+"): "+k); //al.get(j));
		// j++;
		// }

		int codePoint = 0x1F600; // Example: U+1F600 GRINNING FACE EMOJI
		int cp = 0x1f0c1;
		println(
				"a: " + new String(Character.toChars(codePoint)) + new String(Character.toChars(0x1f0c1))
						+ (char) codePoint
						+ (char) cp + "\u2663\u25c6\uf004\u2660");
		for (int i = 0; i < 3; i++)
			println(chance(1, 10));
		// chance(1, 2);
		if (true)
			return;
		int[] arr = { 1, 2, 3, 4, 5, 1, 6, 2, 7, 8, 9, 10, 11 };
		println("maxSpan    : " + maxSpan(arr));
		println("maxSpan2   : " + maxSpan2(arr));
		println("maxSpanHash: " + maxSpanHash(arr));
		for (int i = 0; i < 3; i++)
			println("pi(" + i + "): " + digitOfPi(i));
		println(Double.toString(Math.PI).substring(1, 2));
		int[] a = new int[1];
		println("a: " + a);
		a = makeArray(5);
		a[4] = 5;
		println("a: " + a);
		println("a[4]: " + a[4]);
		{
			int[] n = { 1, 6, 2, 13, 7, 1 };
			// println(sum13(n));
			// println(sum13b(n));
			println(sum67a(n));
			println(sum67(n));
		}
		// {
		// int[] n = { 5, 13, 13, 1, 13, 1, 1 };
		// println(sum13(n));
		// println(sum13b(n));
		// }
		String string;
		if (false) {
			string = null;
		} else {
			string = "as";
		}
		if (string == null)
			println("s: is set to NULL");
		else if (string == "")
			println("s: is empty");
		else
			println("s:" + string);

		println("Done.");
		System.exit(0);
		// dist();
		println("-------------------------");
		double qpi = Math.PI / 4.0f;
		for (int i = 0; i < 8; i++) {
			println(Math.round(i * qpi * 100.0f) / 100.0f + ": "
					+ Math.round(Math.cos(i * qpi) * 100.0f) / 100.0f + ", "
					+ Math.round(Math.sin(i * qpi) * 100.0f) / 100.0f);
		}
		println("-------------------------");

		for (int i = 0; i < 8; i++) {
			println(round(i * qpi, 2) + ": "
					+ round(Math.cos(i * qpi), 2) + ", "
					+ round(Math.sin(i * qpi), 2));
		}
		println("-------------------------");
		{
			Object m;
			m = makeHorse();
			println(m);
		}

		int num = 5;
		Object os[];
		os = new Object[num];
		for (int i = 0; i < os.length; i++) {
			os[i] = new Horse(Integer.toString(i)); // i, i);
			println("os[" + i + "]: " + os[i]);
		}
		Horse hs[];
		hs = new Horse[2];
		for (int i = 0; i < hs.length; i++) {
			hs[i] = new Horse(i, i);
			println("hs[" + i + "]: " + hs[i]);
		}
		Horse h;
		h = new Horse();
		println(h.add(3, 4));
		println(h.add(3, 4));
		println(hget(h));
		println(hget(h));
		System.exit(0);
		// dist();
		// println("someMethod: " + someMethod(3, 1));
		// test_palin();
		// String str;
		// str = "is is not not";
		// println("equalisnot: " + str + ": " + equalIsNot(str));
		// str = "is s not not";
		// println("equalisnot: " + str + ": " + equalIsNot(str));

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
