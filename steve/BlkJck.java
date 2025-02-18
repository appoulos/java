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

	public static void main(String[] args) {
		Card[] shoe, playerHand, dealerHand;
		int playerHandSize = 0;
		int dealerHandSize = 0;
		int shoeSize = 0;
		playerHand = new Card[22];
		dealerHand = new Card[22];
		shoe = new Card[8 * 52];
		int shoeCurr = 0;
		int bet = 0;

		// Configuration
		int numDecks = 4;
		int playerBalance = 100;

		for (String rank : ranks) {
			for (int deck = 0; deck < numDecks; deck++) {
				for (String suit : suits) {
					shoe[shoeSize++] = new Card(rank, suit);
				}
			}
		}

		out.println("Welcome to Blackjack (q to quit)");
		while (true) {
			while (true) {
				if (playerBalance < 10) {
					out.println("Not enough balance to continue. Thanks for playing.");
					System.exit(0);
				}
				out.print("Bet (" + playerBalance + ")? ");
				String input = scan.nextLine();
				try {
					if (input.equals("q")) {
						out.println("Thank you for playing");
						System.exit(0);
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
					if (bet > playerBalance) {
						out.println("Maximum bet is " + playerBalance);
						continue;
					}
					playerBalance -= bet;
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

			playerHandSize = 0;
			dealerHandSize = 0;

			playerHand[playerHandSize++] = shoe[shoeCurr++];
			playerHand[playerHandSize++] = shoe[shoeCurr++];
			int playerSum = handValue(playerHand, playerHandSize);

			dealerHand[dealerHandSize++] = shoe[shoeCurr++];
			dealerHand[dealerHandSize++] = shoe[shoeCurr++];
			int dealerSum = handValue(dealerHand, dealerHandSize);
			boolean playerBust = false;
			boolean dealerBust = false;
			boolean playerBlackjack = playerSum == 21;
			boolean dealerBlackjack = dealerSum == 21;

			if (!dealerBlackjack) {

				out.println("Dealer shows " + dealerHand[1]);

				boolean continue_ = true;
				playerBust = false;
				dealerBust = false;
				while (continue_) {
					showHand(playerHand, playerHandSize);
					out.print("(h)it, (s)tay");
					if (playerHandSize == 2) {
						out.print(", (d)ouble down");
					}
					out.print("? ");
					String input = scan.nextLine();
					if (input.equals("q")) {
						out.println("Thank you for playing");
						System.exit(0);
					}
					switch (input) {
						case "d": // double down
							continue_ = false;
							playerBalance -= bet;
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

			if (dealerBlackjack) {
				if (playerBlackjack) {
					out.println("push");
					playerBalance += bet;
				} else {
					out.println("lose bet " + bet);
				}
			} else if (playerBlackjack) {
				out.println("win 3:2");
				playerBalance += 2.5 * bet;
			} else if (playerBust) {
				out.println("lose bet " + bet);
			} else if (dealerBust || playerSum > dealerSum) {
				out.println("win 1:1");
				playerBalance += 2 * bet;
			} else if (dealerSum == playerSum) {
				out.println("push");
				playerBalance += bet;
			} else {
				out.println("lose bet " + bet);
			}
			out.println("Balance " + playerBalance);
			out.print("(q)uit? ");
			if (scan.nextLine().equals("q"))
				break;
		}
	}
}
