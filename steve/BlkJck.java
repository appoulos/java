import static java.lang.System.out;

import java.util.Scanner;
import java.util.Random;

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
		String str = rank;
		switch (suit) {
			case "Clubs":
				str += "\u2663";
				break;
			case "Diamonds":
				str += "\u2666";
				break;
			case "Hearts":
				str += "\uf004";
				break;
			case "Spades":
				str += "\u2660";
		}
		return str;
	}
}

class BlkJck {
	static Scanner scan = new Scanner(System.in);
	private static String[] ranks = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King" };
	private static String[] suits = { "Clubs", "Diamonds", "Hearts", "Spades" };

	static int handValue(Card[] arr, int size) {
		int sum = 0;
		int aces = 0;
		for (int i = 0; i < size; i++) {
			int value = arr[i].getValue();
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

	static void showHand(Card[] arr, int size) {
		for (int i = 0; i < size; i++) {
			out.print(arr[i] + " ");
		}
		int dealerSum = handValue(arr, size);
		out.println("(" + dealerSum + ")");
	}

	static void quit(int balance) {
		out.println("Thank you for playing");
		out.println("Final balance " + balance);
		System.exit(0);
	}

	public static void main(String[] args) {
		Roster roster = new Roster("roster.db", 100);
		// roster.setBalance("a", 100);
		// roster.save();

		HighScore highscore = new HighScore("highscore.db", 3);
		// highscore.set(player, roster.getBalance(player));

		final int maxDecks = 8;

		Card[] shoe;
		shoe = new Card[maxDecks * 52];
		int shoeSize = 0;

		Card[] playerHand;
		playerHand = new Card[22]; // Max size of single hand (all aces) is 22
		int playerHandSize = 0;

		Card[] dealerHand;
		dealerHand = new Card[22];
		int dealerHandSize = 0;

		int shoeCurr = 0; // Next card index in array to draw from shoe
		int bet = 0;

		// Configuration
		int numDecks = Math.min(4, maxDecks); // Max decks == maxDecks
		// int balance = 100; // Starting player balance

		// Fill shoe with numDecks
		for (String rank : ranks) {
			for (int deck = 0; deck < numDecks; deck++) {
				for (String suit : suits) {
					shoe[shoeSize++] = new Card(rank, suit);
				}
			}
		}

		out.println("Welcome to Blackjack (q to quit)");
		while (true) {
			out.println(roster);
			out.println(highscore);
			out.print("Name (blank to quit): ");
			String player = scan.nextLine().trim();
			if (player.length() == 0) {
				System.exit(0);
			}
			int balance = roster.getBalance(player);
			roster: while (true) {
				while (true) {
					// Get player bet
					out.print("Bet (default 10, max " + balance + ")? ");
					String input = scan.nextLine();
					try {
						if (input.equals("q")) {
							quit(balance);
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

				if (shoeCurr == 0 || shoeCurr > shoeSize * 3 / 4) {
					// Shuffle
					out.println("\n***** Shuffling deck(s) *****\n");
					Random rnd = new Random();
					for (int i = shoeSize - 1; i > 0; i--) {
						int index = rnd.nextInt(i + 1);
						// Simple swap
						Card a = shoe[index];
						shoe[index] = shoe[i];
						shoe[i] = a;
					}
					shoeCurr = 0;
				}

				// Reset arrays (bump allocator)
				playerHandSize = 0;
				dealerHandSize = 0;

				// Player gets two cards
				playerHand[playerHandSize++] = shoe[shoeCurr++];
				playerHand[playerHandSize++] = shoe[shoeCurr++];
				int playerSum = handValue(playerHand, playerHandSize);

				// Dealer gets two cards
				dealerHand[dealerHandSize++] = shoe[shoeCurr++];
				dealerHand[dealerHandSize++] = shoe[shoeCurr++];
				int dealerSum = handValue(dealerHand, dealerHandSize);

				boolean playerBust = false;
				boolean dealerBust = false;
				boolean playerBlackjack = playerSum == 21;
				boolean dealerBlackjack = dealerSum == 21;

				if (!dealerBlackjack) {
					out.println("Dealer shows " + dealerHand[1]);

					playerBust = false;
					dealerBust = false;

					boolean continue_ = true;
					while (continue_) {
						showHand(playerHand, playerHandSize);
						out.print("(h)it, (s)tay");

						if (playerHandSize == 2) {
							out.print(", (d)ouble down");
						}
						out.print("? ");

						String input = scan.nextLine();
						if (input.equals("q")) {
							quit(balance);
						}

						switch (input) {
							case "d": // double down
								continue_ = false;
								balance -= bet;
								bet *= 2;
								playerHand[playerHandSize++] = shoe[shoeCurr++];
								playerSum = handValue(playerHand, playerHandSize);
								showHand(playerHand, playerHandSize);
								if (playerSum > 21) {
									out.println("Player busts");
									playerBust = true;
								}
								continue_ = false;
								break;
							case "h": // hit
								playerHand[playerHandSize++] = shoe[shoeCurr++];
								playerSum = handValue(playerHand, playerHandSize);
								if (playerSum > 21) {
									out.println("Player busts");
									showHand(playerHand, playerHandSize);
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
							showHand(dealerHand, dealerHandSize);

							if (dealerSum < 17) {
								out.println("Dealer hits");
								dealerHand[dealerHandSize++] = shoe[shoeCurr++];
								dealerSum = handValue(dealerHand, dealerHandSize);
								if (dealerSum > 21) {
									out.println("\n***** Dealer busts *****");
									showHand(dealerHand, dealerHandSize);
									continue_ = false;
									dealerBust = true;
								}
							} else {
								out.println("Dealer stays with " + dealerSum);
								continue_ = false;
							}
						}
					}
				}

				// Settle bets
				if (dealerBlackjack) {
					out.print("Dealer blackjack ");
					showHand(dealerHand, dealerHandSize);
					if (playerBlackjack) {
						out.println("push");
						balance += bet;
					} else {
						out.println("lose bet " + bet);
					}
				} else if (playerBlackjack) {
					out.println("win 3:2");
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
				if (balance < 10) {
					out.println("Not enough balance to continue.");
					roster.setBalance(player, balance);
					break roster;
				}

				out.println("Balance " + balance);
				roster.setBalance(player, balance);
				again: while (true) {
					out.print("(A)gain (r)oster (q)uit? ");
					switch (scan.nextLine().toLowerCase()) {
						case "q":
							highscore.set(player, balance);
							quit(balance);
							break;
						case "r":
							highscore.set(player, balance);
							break roster;
						case "":
						case "a":
							break again;
						default:
							out.println("Invalid input");
					}
				}
			}
		}
	}
}
