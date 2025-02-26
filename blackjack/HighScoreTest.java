class HighScoreTest {
	public static void main(String[] args) {
		int maxScores = 3;
		String dbFilename = "highscore_test.db";
		HighScore hs = new HighScore(dbFilename, maxScores);
		// Add 4 high scores
		hs.set("p1",80);
		hs.set("p2",50);
		hs.set("p3",100);
		hs.set("p4",80);
		// Print
		System.out.println(hs);
		// Delete dbFilename file
		hs.purge();
		// Print
		System.out.println("This should be empty now after purging: " + hs);
	}
}
