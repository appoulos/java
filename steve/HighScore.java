import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * HighScore has the ability to store names and scores to keep track of in a file and print.
 */
class HighScore {
	private String dbFilename;
	private int maxScores;
	private TreeMap<Integer, String> roster;

	/**
	 * Class constructor specifying filename and maximum number of scores to keep track of.
	 * @param dbFilename filename to store the data to disk
	 * @param maxScores	only keep this number of high scores
	 */
	@SuppressWarnings("unchecked")
	public HighScore(String dbFilename, int maxScores) {
		this.dbFilename = dbFilename;
		this.maxScores = maxScores;
		roster = new TreeMap<>();
		// Get roster
		try (FileInputStream fis = new FileInputStream(dbFilename);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			roster = (TreeMap<Integer, String>) ois.readObject();
		} catch (Exception e) {
			// FileNotFoundException | StreamCorruptedException | IOException | ClassNotFoundException
			// Create new file
			save();
		}
	}

	/**
	 * Remove the file from disk and clear the high scores.
	 */
	public void purge() {
		File f = new File(dbFilename);
		try {
			if (!f.delete()) {
				System.out.println("Error: could not delete file " + dbFilename);
			}
		} catch (Exception e) {
			System.out.println("Error: " + e);
		}
		roster = new TreeMap<>();
	}

	/**
	 * Set the player/associated high score and limit number of scores to maximum set in constructor.
	 * @param player player name
	 * @param balance player total balance
	 */
	public void set(String player, int balance) {
		if (roster.get(balance) == null) {
			roster.put(balance, player);
			// Trim roster to maxScores size
			while (roster.size() > maxScores) {
				roster.remove(roster.firstKey());
			}
			save();
		}
	}

	@Override
	public String toString() {
		if (roster.size() > 0) {
			int cnt = 0;
			String str = "\n***** High Scores *****\nNum Balance Name\n";
			// Print roster in reverse order by balance
			for (Map.Entry<Integer, String> entry : roster.descendingMap().entrySet()) {
				str += String.format("%3d %7s %s\n", ++cnt, "$" + entry.getKey(), entry.getValue());
			}
			str += "\n";
			return str;
		}
		return "";
	}

	/**
	 * Create/save the high scores to the filename specified in the constructor.
	 */
	private void save() {
		try (FileOutputStream fos = new FileOutputStream(dbFilename);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(roster);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
