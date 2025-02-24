import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.TreeMap;

class Roster {
	private int startingBalance;
	private String dbFilename;
	private Map<String, Integer> roster;

	@SuppressWarnings("unchecked")
	public Roster(String dbFilename, int startingBalance) {
		this.startingBalance = startingBalance;
		this.dbFilename = dbFilename;
		roster = new TreeMap<>();
		// Get roster
		try (FileInputStream fis = new FileInputStream(dbFilename);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			roster = (TreeMap<String, Integer>) ois.readObject();
		} catch (FileNotFoundException e) {
			// Create new db file
			save();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void save() {
		try (FileOutputStream fos = new FileOutputStream(dbFilename);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(roster);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void setBalance(String player, int balance) {
		if (balance < 10) {
			roster.remove(player);
		} else {
			roster.put(player, balance);
		}
		save();
	}

	public int getBalance(String player) {
		if (roster.get(player) != null) {
			return roster.get(player);
		}
		roster.put(player, startingBalance);
		save();
		return startingBalance;
	}

	@Override
	public String toString() {
		if (roster.size() > 0) {
			String str = "Roster:\nBalance Name\n";
			for (Map.Entry<String, Integer> entry : roster.entrySet()) {
				str += String.format("%7s %s\n", "$" + entry.getValue(), entry.getKey());
			}
			return str;
		}
		return "";
	}
}
