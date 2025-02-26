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
				return "\u001B[47m\u001B[1;31m" + rank + "\u2666\u001B[0m ";
			case "Hearts":
				return "\u001B[47m\u001B[1;31m" + rank + "\u2764\u001B[0m ";
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

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> getDb() {
		Map<String, Integer> tableData = null;
		try (FileInputStream fis = new FileInputStream("table_data.db");
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
		try (FileOutputStream fos = new FileOutputStream("table_data.ser");
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
	public static final int startingBalance = 105; // New player balance
	public static final int numDecks = 4; // Decks in shoe

	public static void main(String[] args) {
		out.print("\u001B[0;107m\u001B[1;30m");
		// Get roster
		roster = getDb();

		List<Card> shoe = new ArrayList<>();
		List<Card> dealerHand = new ArrayList<>();
		List<Card> playerHand = new ArrayList<>();

		// Fill shoe with numDecks
		for (String rank : ranks) {
			for (int deck = 0; deck < numDecks; deck++) {
				for (String suit : suits) {
					shoe.add(new Card(rank, suit));
				}
			}
		}

		while (true) {
			// Print roster
			if (roster.size() > 0) {
				out.println("Roster:");
				out.println("Balance Name");
				for (Map.Entry<String, Integer> entry : roster.entrySet()) {
					out.printf("%7s %s\n", "$" + entry.getValue(), entry.getKey());
				}
			}
			out.print("Enter your name (leave blank to exit): ");
			name = scan.nextLine();
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

						String input = scan.nextLine();
						if (input.equals("q")) {
							quit();
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
					break roster;
				}
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
