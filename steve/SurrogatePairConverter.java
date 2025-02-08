public class SurrogatePairConverter {

	public static void main(String[] args) {
		// Example supplemental character (U+1F600 - GRINNING FACE)
		int codePoint = 0x1F600;

		// Convert code point to surrogate pair
		char highSurrogate = Character.highSurrogate(codePoint);
		char lowSurrogate = Character.lowSurrogate(codePoint);

		// Output the surrogate pair
		System.out.println("Code point: " + codePoint);
		System.out.println("High surrogate: " + (int) highSurrogate);
		System.out.println("Low surrogate: " + (int) lowSurrogate);

		// Combine surrogate pair back to code point
		int reconstructedCodePoint = Character.toCodePoint(highSurrogate, lowSurrogate);
		System.out.println("Reconstructed code point: " + reconstructedCodePoint);
		System.out.println("Reconstructed character: " + (char) reconstructedCodePoint);
	}
}
