public class SurrogateConverter {

	public static char[] toSurrogatePair(int codePoint) {
		if (!Character.isSupplementaryCodePoint(codePoint)) {
			throw new IllegalArgumentException("Code point is not supplementary");
		}

		char highSurrogate = Character.highSurrogate(codePoint);
		char lowSurrogate = Character.lowSurrogate(codePoint);

		return new char[] { highSurrogate, lowSurrogate };
	}

	public static void main(String[] args) {
		int codePoint = 0x1F600; // Example: U+1F600 GRINNING FACE EMOJI

		char[] surrogatePair = toSurrogatePair(codePoint);
		System.out.println(
				"Surrogate Pair: \\u" + Integer.toHexString(surrogatePair[0]) + " \\u" + Integer.toHexString(surrogatePair[1]));

		// Combining the surrogate pair back to the original code point
		int originalCodePoint = Character.toCodePoint(surrogatePair[0], surrogatePair[1]);
		System.out.println("Original Code Point: 0x" + Integer.toHexString(originalCodePoint) + " " + (char) codePoint);
	}
}
