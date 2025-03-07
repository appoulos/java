import static java.lang.System.out;

import java.util.Scanner;
import java.util.TreeMap;
import java.util.ArrayList;
import java.util.Collections;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.Map;

import java.io.File;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.IOException;
// import java.io.ObjectInputStream;
// import java.io.ObjectOutputStream;
// import java.util.Map;
// import java.util.TreeMap;

/**
 * HighScore has the ability to store names and scores to keep track of in a
 * file and print.
 */
class HighScore {
	private String dbFilename;
	private int maxScores;
	private TreeMap<Integer, String> roster;

	/**
	 * Class constructor specifying filename and maximum number of scores to keep
	 * track of.
	 * 
	 * @param dbFilename filename to store the data to disk
	 * @param maxScores  only keep this number of high scores
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
			// FileNotFoundException | StreamCorruptedException | IOException |
			// ClassNotFoundException
			// Create new file
			save();
		}
	}

	/**
	 * Remove dbFilename file from disk and clear the high scores in the roster.
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
	 * Get the number of high scores
	 */
	public int size() {
		return roster.size();
	}

	/**
	 * Set the player/associated high score and limit number of scores to maximum
	 * set in constructor.
	 * 
	 * @param player  player name
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

class Card {
	private String rank;
	private String suit;

	public Card(String rank, String suit) {
		this.rank = rank;
		this.suit = suit;
	}

	public int getValue() {
		switch (rank) {
			case "Ace":
				return 1;
			case "Jack":
			case "Queen":
			case "King":
				return 10;
			default:
				return Integer.valueOf(rank);
		}
	}

	@Override
	public String toString() {
		switch (suit) {
			case "Clubs":
				return rank + "\u2663 ";
			case "Diamonds":
				return "\u001B[1;31m" + rank + "\u2666\u001B[0m ";
			case "Hearts":
				return "\u001B[1;31m" + rank + "\u2764\u001B[0m ";
			default: // case "Spades":
				return rank + "\u2660 ";
		}
	}
}

class BlkJckArrayList {
	static Scanner scan = new Scanner(System.in);
	private static String[] ranks = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King" };
	private static String[] suits = { "Clubs", "Diamonds", "Hearts", "Spades" };

	static public int handValue(List<Card> cards) {
		int sum = 0;
		int aces = 0;
		for (Card card : cards) {
			int value = card.getValue();
			if (value == 1)
				aces++;
			sum += value;
		}
		while (aces > 0 && sum + 10 <= 21) {
			sum += 10;
			aces--;
		}
		return sum;
	}

	static void printHand(List<Card> cards, String prompt) {
		out.print(prompt);
		for (Card card : cards)
			out.print(card);
		int dealerSum = handValue(cards);
		out.println("(" + dealerSum + ")");
	}

	static void quit() {
		out.println("Thank you for playing " + name);
		if (name.length() > 0) {
			out.println("Final balance " + balance);
		}
		System.exit(0);
	}

	private static final String rosterFile = "table_data.db";

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getDb() {
		Map<String, Integer> tableData = null;
		try (FileInputStream fis = new FileInputStream(rosterFile);
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			tableData = (TreeMap<String, Integer>) ois.readObject();
		} catch (FileNotFoundException e) {
			// Create new db file
			saveDb(tableData);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (tableData == null) {
			return new TreeMap<>();
		}
		return tableData;
	}

	public static void saveDb(Map<String, Integer> map) {
		try (FileOutputStream fos = new FileOutputStream(rosterFile);
				ObjectOutputStream oos = new ObjectOutputStream(fos)) {
			oos.writeObject(map);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void settleBet(int bet) {
		balance += bet;
		if (balance < 10) {
			roster.remove(name);
		} else {
			roster.put(name, balance);
		}
		saveDb(roster);
	}

	public static int getBalance() {
		if (!roster.containsKey(name)) {
			roster.put(name, startingBalance);
			saveDb(roster);
			return startingBalance;
		}
		return roster.get(name);
	}

	public static boolean checkBalance() {
		if (balance < 10) {
			out.print("Not enough balance to continue, " + name + " removed from roster");
			if (balance > 0) {
				out.println(" and returned $" + balance);
			} else {
				out.println();
			}
			// Zero out balance
			settleBet(-balance);
			return false;
			// quit();
		}
		return true;
	}

	// On disk record of players saved after every balance change
	public static Map<String, Integer> roster;

	// Player info
	public static String name;
	public static int balance;

	// Configuration
	public static final int startingBalance = 1000; // New player balance
	public static final int numDecks = 4; // Decks in shoe

	public static void main(String[] args) {
		// out.print("\u001B[0;107m\u001B[1;30m");
		// High scores
		int maxScores = 3;
		String dbFilename = "highscore.db";
		HighScore hs = new HighScore(dbFilename, maxScores);

		// Get roster
		roster = getDb();
		int round = 1;

		List<Card> shoe = new ArrayList<>();
		List<Card> dealerHand = new ArrayList<>();
		List<Card> playerHand = new ArrayList<>();

		while (true) {
			// Print high scores
			System.out.print(hs);

			// Print roster
			if (roster.size() > 0) {
				out.println("Roster:");
				out.println("Balance Name");
				for (Map.Entry<String, Integer> entry : roster.entrySet()) {
					out.printf("%7s %s\n", "$" + entry.getValue(), entry.getKey());
				}
			}
			out.print("Enter your name (\"cpu\" for auto-player, leave blank to exit): ");
			name = scan.nextLine().trim();
			if (name.length() == 0) {
				quit();
			}

			out.println("Welcome to Blackjack " + name + " (q to quit)");
			balance = getBalance();

			// Start new hand
			roster: while (true) {
				int bet = 0;

				// Get player bet
				while (true) {
					if (!checkBalance()) {
						break roster;
					}
					out.print("Bet (default 10, max " + balance + ")? ");
					if (name.equals("cpu")) {
						bet = Math.min(balance, 10 + Math.abs(startingBalance - balance));
						out.println(bet);
						settleBet(-bet);
						break;
					} else {
						String input = scan.nextLine();
						try {
							if (input.equals("q")) {
								quit();
							}
							if (input.equals("")) {
								bet = 10;
							} else {
								bet = Integer.parseInt(input);
							}
							if (bet % 10 != 0) {
								out.println("Bets must be multiples of 10");
								continue;
							}
							if (bet > balance) {
								out.println("Maximum bet is " + balance);
								continue;
							}
							settleBet(-bet);

							break;
						} catch (NumberFormatException e) {
							out.println("Invalid input. Please enter a number");
						}
					}
				}

				// Shuffle when new game or less than 25% left in shoe
				if (shoe.size() < 52 * numDecks / 4) {
					// Fill shoe with numDecks
					for (String rank : ranks) {
						for (int deck = 0; deck < numDecks; deck++) {
							for (String suit : suits) {
								shoe.add(new Card(rank, suit));
							}
						}
					}

					out.println("\n***** Shuffling deck(s) *****\n");
					Collections.shuffle(shoe);
				}

				// Reset hands
				playerHand.clear();
				dealerHand.clear();

				// Player gets two cards
				playerHand.add(shoe.remove(0));
				playerHand.add(shoe.remove(0));
				int playerSum = handValue(playerHand);

				// Dealer gets two cards
				dealerHand.add(shoe.remove(0));
				dealerHand.add(shoe.remove(0));
				int dealerSum = handValue(dealerHand);

				boolean playerBust = false;
				boolean dealerBust = false;
				boolean playerBlackjack = playerSum == 21;
				boolean dealerBlackjack = dealerSum == 21;

				if (!dealerBlackjack && !playerBlackjack) {
					out.println("Dealer shows " + dealerHand.get(1)); // Second card shows, first hidden

					// Player's turn
					boolean continue_ = true;
					while (continue_) {
						printHand(playerHand, "Player hand ");
						out.print("(h)it, (s)tay");

						if (playerHand.size() == 2 && balance >= bet) {
							out.print(", (d)ouble down");
						}
						out.print("? ");

						String input;
						if (name.equals("cpu")) {
							if (handValue(playerHand) < 17) {
								input = "h";
								out.println("h");
							} else {
								input = "s";
								out.println("s");
							}
						} else {
							input = scan.nextLine();
							if (input.equals("q")) {
								quit();
							}
						}

						switch (input) {
							case "d": // double down
								continue_ = false;
								settleBet(-bet);
								bet *= 2;

								playerHand.add(shoe.remove(0));
								playerSum = handValue(playerHand);
								printHand(playerHand, "Player hand ");

								if (playerSum == 21) {
									out.println("Player has 21");
								} else if (playerSum > 21) {
									out.println("Player busts");
									playerBust = true;
								}
								continue_ = false;
								break;
							case "h": // hit
								playerHand.add(shoe.remove(0));
								playerSum = handValue(playerHand);
								if (playerSum == 21) {
									printHand(playerHand, "Player has 21 ");
									continue_ = false;
								} else if (playerSum > 21) {
									printHand(playerHand, "Player busts ");
									continue_ = false;
									playerBust = true;
								}
								break;
							case "s": // stay
								continue_ = false;
								break;
							default:
								out.println("Invalid input. Try again");
						}
					}

					// Dealer's turn; draws cards until 17
					continue_ = true;
					if (!playerBust) {
						out.println("\n***** Dealers turn *****");
						while (continue_) {
							printHand(dealerHand, "Dealer hand ");

							if (dealerSum < 17) {
								out.println("Dealer hits");
								dealerHand.add(shoe.remove(0));
								dealerSum = handValue(dealerHand);
								if (dealerSum > 21) {
									out.println("\n***** Dealer busts *****");
									printHand(dealerHand, "Dealer hand ");
									continue_ = false;
									dealerBust = true;
								}
							} else if (dealerSum == 21) {
								out.println("Dealer has 21");
								continue_ = false;
							} else {
								out.println("Dealer stays with " + dealerSum);
								continue_ = false;
							}
						}
					}
				}

				// Settle bets
				if (dealerBlackjack) {
					printHand(playerHand, "Player hand ");
					printHand(dealerHand, "Dealer blackjack ");
					if (playerBlackjack) {
						out.println("push $" + bet);
						settleBet(bet);
					} else {
						out.println("lose bet $" + bet);
					}
				} else if (playerBlackjack) {
					printHand(playerHand, "Player hand ");
					printHand(dealerHand, "Dealer hand ");
					out.println("Player blackjack win 3:2 $" + (int) (2.5 * bet));
					settleBet((int) (2.5 * bet));
				} else if (playerBust) {
					out.println("lose bet $" + bet);
				} else if (dealerBust || playerSum > dealerSum) {
					out.println("win 1:1 $" + 2 * bet);
					settleBet(2 * bet);
				} else if (dealerSum == playerSum) {
					out.println("push $" + bet);
					settleBet(bet);
				} else if (playerSum < dealerSum) {
					out.println("lose bet $" + bet);
				} else {
					System.err.println("oops. lose bet $" + bet);
				}

				// Round completed
				out.println("Balance $" + balance);
				if (!checkBalance()) {
					if (name.equals("cpu")) {
						out.println("Round " + round);
					}
					break roster;
				}
				hs.set(name, balance);
				if (name.equals("cpu")) {
					out.println("Round " + round);
					round++;
				} else {
					out.print("(P)lay again (s)witch players (q)uit? ");
					switch (scan.nextLine()) {
						case "q":
							quit();
							break;
						case "s":
							break roster;
						default:
					}
				}
			}
		}
	}
}
