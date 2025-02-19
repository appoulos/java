import static java.lang.System.out;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;

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
				return rank+"\u2663 ";
			case "Diamonds":
				return "\u001B[1;31m"+rank+"\u2666\u001B[0m ";
			case "Hearts":
				return "\u001B[1;31m"+rank+"\uf004\u001B[0m ";
			default: // case "Spades":
				return rank+"\u2660 ";
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

	static void showHand(List<Card> cards, String prompt) {
		out.print(prompt);
		for (Card card : cards)
			out.print(card);
		int dealerSum = handValue(cards);
		out.println("(" + dealerSum + ")");
	}

	static void quit(String name, int balance) {
		out.println("Thank you for playing");
		out.println("Final balance " + balance);
		updatePlayersDb(name, balance);
		System.exit(0);
	}

	public static void writeListToFile(List<String> list, String filePath) {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
			for (String str : list) {
				writer.write(str);
				writer.newLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<String> readListFromFile(String filePath) {
		List<String> stringList = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				stringList.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return stringList;
	}

	public static String playersDb = "players.txt";

	public static void printPlayers() {
		int cnt = 0;

		for (String line : players) {
			if (cnt == 0) {
				out.println("Active players:");
				out.println("Balance Name");
			}
			if (cnt++ % 2 == 0) {
				out.printf("%7s ", line);
			} else {
				out.println(line);
			}
		}
	}

	public static int getBalance(String name, int balance) {
		int cnt = 0;
		int prevBal = 0;
		for (String line : players) {
			if (cnt++ % 2 == 0) {
				prevBal = Integer.valueOf(line);
			} else {
				if (line.equals(name)) {
					balance = prevBal;
				}
			}
		}
		return balance;
	}

	public static void updatePlayersDb(String name, int balance) {
		int cnt = 0;
		boolean found = false;
		if (balance <= 0) { // Remove zero balance from playersDb if present
			for (String line : players) {
				if (cnt % 2 == 1 && line.equals(name)) {
					players.remove(cnt);
					players.remove(cnt - 1);
					found = true;
					break;
				}
				cnt++;
			}
			if (found) {
				writeListToFile(players, playersDb);
			}
			return;
		}

		// Attempt to update player balance
		for (String line : players) {
			if (cnt % 2 == 0) {
				// skip balance
			} else {
				// name
				if (line.equals(name)) {
					// out.println("Updating " + balance + " " + name);
					players.set(cnt - 1, "" + balance);
					found = true;
				}
			}
			cnt++;
		}

		// Add new player if not already in players
		if (!found) {
			// out.println("Adding " + balance + " " + name);
			players.add("" + balance);
			players.add(name);
		}

		// Save players to file
		writeListToFile(players, playersDb);
	}

	public static List<String> players = new ArrayList<>();

	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		List<PlayerRow> tableData = new ArrayList<>();
		try (FileInputStream fis = new FileInputStream("table_data.ser");
				ObjectInputStream ois = new ObjectInputStream(fis)) {
			tableData = (ArrayList<PlayerRow>) ois.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		for (PlayerRow row : tableData) {
			out.println("name: " + row.name + ", balance: " + row.balance);
		}
		// tableData.add(new PlayerRow("asdf", 5));
		// tableData.add(new PlayerRow("uiop", 7));
		// // Example using ObjectOutputStream
		// try (FileOutputStream fileOut = new FileOutputStream("table_data.ser");
		// ObjectOutputStream objectOut = new ObjectOutputStream(fileOut)) {
		// objectOut.writeObject(tableData); // tableData is a List<Object> or
		// Object[][]
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		List<Card> shoe = new ArrayList<>();
		List<Card> playerHand = new ArrayList<>();
		List<Card> dealerHand = new ArrayList<>();
		String name = "";
		int bet = 0;

		// Configuration
		int numDecks = 4; // Decks in shoe
		int balance = 100; // Starting player balance

		// Fill shoe with numDecks
		for (String rank : ranks) {
			for (int deck = 0; deck < numDecks; deck++) {
				for (String suit : suits) {
					shoe.add(new Card(rank, suit));
				}
			}
		}

		players = readListFromFile(playersDb);
		printPlayers();
		while (true) {
			out.print("Enter your name: ");
			name = scan.nextLine();
			name = name.replace(',', '-');
			if (name.length() == 0) {
				out.println("Invalid input. Try again");
			} else {
				break;
			}
		}
		out.println("Welcome to Blackjack " + name + " (q to quit)");
		balance = getBalance(name, balance);

		while (true) {
			while (true) {
				if (balance < 10) {
					out.println("Not enough balance to continue.");
					quit(name, balance);
				}

				// Get player bet
				out.print("Bet (default 10, max " + balance + ")? ");
				String input = scan.nextLine();
				try {
					if (input.equals("q")) {
						quit(name, balance);
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
					balance -= bet;
					break;
				} catch (NumberFormatException e) {
					out.println("Invalid input. Please enter a number");
				}
			}

			// Shuffle when new game or less than 25% left in shoe
			if (shoe.size() == 52 * numDecks || shoe.size() < 52 * numDecks / 4) {
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
				out.println("Dealer shows " + dealerHand.get(1));

				playerBust = false;
				dealerBust = false;

				boolean continue_ = true;
				while (continue_) {
					showHand(playerHand, "Player hand ");
					out.print("(h)it, (s)tay");

					if (playerHand.size() == 2 && balance >= bet) {
						out.print(", (d)ouble down");
					}
					out.print("? ");

					String input = scan.nextLine();
					if (input.equals("q")) {
						quit(name, balance);
					}

					switch (input) {
						case "d": // double down
							continue_ = false;
							balance -= bet;
							bet *= 2;

							playerHand.add(shoe.remove(0));
							playerSum = handValue(playerHand);
							showHand(playerHand, "Player hand ");

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
								showHand(playerHand, "Player has 21 ");
								continue_ = false;
							} else if (playerSum > 21) {
								showHand(playerHand, "Player busts ");
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

				// Dealer draws cards until 17
				continue_ = true;
				if (!playerBust) {
					out.println("\n***** Dealers turn *****");
					while (continue_) {
						showHand(dealerHand, "Dealer hand ");

						if (dealerSum < 17) {
							out.println("Dealer hits");
							dealerHand.add(shoe.remove(0));
							dealerSum = handValue(dealerHand);
							if (dealerSum > 21) {
								out.println("\n***** Dealer busts *****");
								showHand(dealerHand, "Dealer hand ");
								continue_ = false;
								dealerBust = true;
							}
						} else if (dealerSum == 21) {
							out.println("Dealer has 21");
						} else {
							out.println("Dealer stays with " + dealerSum);
							continue_ = false;
						}
					}
				}
			}

			// Settle bets
			if (dealerBlackjack) {
				showHand(playerHand, "Player hand ");
				showHand(dealerHand, "Dealer blackjack ");
				if (playerBlackjack) {
					out.println("push");
					balance += bet;
				} else {
					out.println("lose bet " + bet);
				}
			} else if (playerBlackjack) {
				showHand(playerHand, "Player hand ");
				showHand(dealerHand, "Dealer hand ");
				out.println("Player blackjack win 3:2");
				balance += 2.5 * bet;
			} else if (playerBust) {
				out.println("lose bet " + bet);
			} else if (dealerBust || playerSum > dealerSum) {
				out.println("win 1:1");
				balance += 2 * bet;
			} else if (dealerSum == playerSum) {
				out.println("push");
				balance += bet;
			} else {
				out.println("lose bet " + bet);
			}

			// Round completed
			out.println("Balance " + balance);
			out.print("(q)uit? ");
			if (scan.nextLine().equals("q")) {
				quit(name, balance);
			}
		}
	}
}

class PlayerRow implements Serializable {
	String name;
	int balance;

	PlayerRow(String name, int balance) {
		this.name = name;
		this.balance = balance;
	}
}
