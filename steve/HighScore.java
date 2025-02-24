import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.Map;
import java.util.TreeMap;

class HighScore {
	private String dbFilename;
	private int maxScores;
	private TreeMap<Integer, String> roster;

	@SuppressWarnings("unchecked")
	public HighScore(String dbFilename, int maxScores) {
		this.dbFilename = dbFilename;
		this.maxScores = maxScores;
		roster = new TreeMap<>(); // Collections.reverseOrder());
		// Get roster
		try (FileInputStream fis = new FileInputStream(dbFilename);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			roster = (TreeMap<Integer, String>) ois.readObject();
		} catch (Exception e) {
			// Create new file
			save();
		}
		// } catch (FileNotFoundException | StreamCorruptedException e) {
		// 	// Create new file
		// 	save();
		// } catch (IOException e) {
		// 	e.printStackTrace();
		// } catch (ClassNotFoundException e) {
		// 	e.printStackTrace();
		// }
	}

	public void save() {
		try (FileOutputStream fos = new FileOutputStream(dbFilename);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(roster);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void set(String player, int balance) {
		if (roster.get(balance) == null) {
			roster.put(balance, player);
			// Trim roster to maxScores size
			while (roster.size() > maxScores) {
				roster.remove(roster.firstKey());
			}
			// Complicated way to trim roster to maxScores size
			// Map<Integer, String> reverseMap = roster.descendingMap();
			// roster = reverseMap.entrySet().stream()
			// .limit(maxScores)
			// .collect(TreeMap::new, (m, e) -> m.put(e.getKey(), e.getValue()),
			// Map::putAll);
			save();
		}
	}

	@Override
	public String toString() {
		if (roster.size() > 0) {
			int cnt = 0;
			String str = "High Scores:\nNum Balance Name\n";
			// Print roster in reverse order by balance
			// Map<Integer, String> reverseMap = roster.descendingMap();
			for (Map.Entry<Integer, String> entry : roster.descendingMap().entrySet()) {
				str += String.format("%3d %7s %s\n", ++cnt, "$" + entry.getKey(), entry.getValue());
			}
			return str;
		}
		return "";
	}
}
