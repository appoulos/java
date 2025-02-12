import static java.lang.System.out;
import java.util.Scanner;
import java.util.ArrayList;

class Card {
	String rank;
	String suit;

	Card(String rank, String suit) {
		this.rank = rank;
		this.suit = suit;
	}

	int getValue() {
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
		return str; // + " of " + suit;
	}
}

class Deck {
	ArrayList<Card> cards;
	int numDecks;
	static String[] ranks = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King" };
	static String[] suits = { "Clubs", "Diamonds", "Hearts", "Spades" };

	Deck(int decks) {
		numDecks = decks;
		cards = new ArrayList<>();
		newDeck();
	}

	void newDeck() {
		// Fill cards with deck(s)
		for (String rank : ranks) {
			for (int deck = 0; deck < numDecks; deck++) {
				for (String suit : suits) {
					cards.add(new Card(rank, suit));
				}
			}
		}

		// Shuffle
		int deckSize = cards.size();
		for (int i = 0; i < deckSize; i++) {
			Card temp = cards.get(i);
			cards.set((int) (Math.random() * deckSize), cards.get(i));
			cards.set(i, temp);
		}
	}

	Card getCard() {
		if (cards.size() < 52 * numDecks / 4) { // cards left less than 25% restock shoe
			out.println("New decks filled in shoe");
			newDeck();
		}
		return cards.remove(0);
	}

	@Override
	public String toString() {
		String str = "";
		String sep = "";
		for (Card card : cards) {
			str = sep + card;
			sep = ", ";
		}
		return str;
	}
}

class Shoe {
	Deck deck;

	Shoe(int decks) {
		deck = new Deck(decks);
	}

	Card getCard() {
		return deck.getCard();
	}
}

class Hand {
	ArrayList<Card> cards;
	boolean split;

	Hand() {
		cards = new ArrayList<>();
	}

	void setSplit() {
		split = true;
	}

	boolean getSplit() {
		return split;
	}

	void clear() {
		cards.clear();
	}

	boolean splitOption() {
		if (cards.size() != 2) {
			return false;
		}
		return cards.get(0).getValue() == cards.get(1).getValue();
	}

	int value() {
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

	Card removeSecond() {
		if (cards.size() > 1) {
			return cards.remove(1);
		}
		return null;
	}

	Card getFirst() {
		if (cards.size() > 0) {
			return cards.get(0);
		}
		return null;
	}

	void addCard(Card c) {
		cards.add(c);
	}

	@Override
	public String toString() {
		String str = "";
		String sep = "";
		for (Card card : cards) {
			str += sep + card;
			sep = ", ";
		}
		return str;
	}
}

class Dealer {
	String name;
	ArrayList<Player> players;
	Hand hand;
	Shoe shoe;
	int minBet;
	int maxBet;
	boolean showValues;

	Dealer(String name, ArrayList<Player> players, int numDecks, int minBet, int maxBet, boolean showValues) {
		this.name = name;
		this.players = players;
		hand = new Hand();
		shoe = new Shoe(numDecks);
		this.minBet = minBet;
		this.maxBet = maxBet;
		this.showValues = showValues;
	}

	boolean checkBalances() {
		for (Player player : players) {
			if (player.getBalance() >= minBet) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Start new round.
	 * 
	 * @return true if any player has more than ante balance to start another round
	 * @return false if no player has more than ante balance
	 */
	boolean newRound() {
		// bounce players with insufficient funds
		for (int i = players.size() - 1; i >= 0; i--) {
			Player player = players.get(i);
			if (player.getBalance() < minBet) {
				out.println("Player " + player.getName()
						+ " has insufficient balance. Removed from table with balance " + player.getBalance());
				players.remove(i);
			} else {
				int bet = Scan.readBet("" + player.getName() + " bet (" + minBet + ")? ", minBet, maxBet);
				player.addBalance(-bet);
				player.setBet(bet);
			}
		}

		if (players.size() == 0) {
			return false;
		}

		// deal two cards to every player
		for (Player player : players) {
			player.clearHand();
			player.giveCard(shoe.getCard(), 0);
			player.giveCard(shoe.getCard(), 0);
		}
		// dealer gets two cards
		hand.addCard(shoe.getCard());
		hand.addCard(shoe.getCard());

		// show player hands
		out.println("\n***** At the table *****");
		for (Player player : players) {
			// out.println(player); // + (showValues ? ", Value: " + player.getHandValues()
			// : ""));
			out.println(player.print(showValues)); // + (showValues ? ", Value: " + player.getHandValue() : ""));
		}

		// dealer hand hide one card
		// todo: show both cards if dealer has blackjack
		out.println("Dealer: " + hand.getFirst() + ", \uf656");

		// start hit/stand for each player
		char choice;
		for (Player player : players) {
			out.println("\n***** Player " + player.getName() + "'s turn *****");

			int turn = 0;
			for (int handNum = 0; handNum < player.getHands().size(); handNum++) {
				turn++;
				if (handNum > 0) {
					out.println("Hand number: " + (handNum + 1));
				}

				out.println(player.getHand(handNum, showValues));

				if (player.getHandValue(handNum) == 21) {
					out.println("Player " + player.name + " blackjack");
					break;
				}

				// split ace cannot be hit
				String hitPrompt = "(h)it, ";
				String hitkey = "h";
				if (player.getHand(handNum).getSplit()
						&& player.getHand(handNum).getFirst().rank == "Ace") {
					hitPrompt = "";
					hitkey = "";
				}

				// Balance required for split or double down
				boolean canSplitOrDD = false;
				if (player.getBalance() >= player.getBet()) {
					canSplitOrDD = true;
				}

				// Double down option
				String doubleDownPrompt = "";
				String doubleDownkey = "";
				if (canSplitOrDD && turn == 1) {
					doubleDownPrompt = "(d)ouble down, ";
					doubleDownkey = "d";
				}

				// Split option
				String splitPrompt = "";
				String splitKey = "";
				if (canSplitOrDD && player.numHands() < 4 && player.getHand(handNum).splitOption()) {
					splitPrompt = "sp(l)it, ";
					splitKey = "l";
				}

				choice = Scan.readChoice(
						hitPrompt + "(s)tand, " + doubleDownPrompt + splitPrompt + "s(u)rrender",
						hitkey + "s" + doubleDownkey + splitKey + "u");

				switch (choice) {
					case 'h':
						player.giveCard(shoe.getCard(), handNum);
						if (player.getHandValue(handNum) > 21) {
							out.println(player.print(showValues)); // + (showValues ? ", Value: " + player.getHandValue() : ""));
							out.println("Player " + player.name + " busts");
							break;
						}
						handNum--;
						break;
					case 's':
						break;
					case 'd':
						player.addBalance(-player.getBet());
						player.setBet(player.getBet() * 2);
						handNum--;
						break;
					case 'l':
						player.split(handNum);
						player.giveCard(shoe.getCard(), handNum);
						player.giveCard(shoe.getCard(), handNum + 1);
						handNum--;
						break;
					case 'u': // todo don't allow after splitting
						player.setSurrender(true);
						player.setBet(player.getBet() / 2);
						break;
					default:
						out.println("Error: unreachable switch: " + choice);
						System.exit(1);
				}

			}
		}

		// Find max score of players
		// ArrayList<Player> winners = new ArrayList<>();
		// int maxScore = 0;
		// for (Player player : players) {
		// for (Hand hand : player.getHands()) {
		// int score = hand.value();
		// if (score <= 21 && score > maxScore) {
		// maxScore = score;
		// winners.clear();
		// winners.add(player);
		// } else if (score == maxScore) {
		// if (!winners.contains(player)) {
		// winners.add(player);
		//
		// }
		// }
		// }
		boolean possibleWinners = false;
		for (Player player : players) {
			for (Hand playerHand : player.getHands()) {
				int score = playerHand.value();
				if (score <= 21 && !player.getSurrender()) {
					possibleWinners = true;
				}
			}
		}

		// if (maxScore < hand.value()) {
		if (!possibleWinners) {
			return true;
		}

		// Dealers turn
		out.println("\n***** Dealers turn *****");
		out.println(this + (showValues ? ", Value: " + hand.value() : ""));
		// Dealer rule must hit below 17
		while (hand.value() < 17) {
			out.println("Dealer hits");
			hand.addCard(shoe.getCard());
			out.println(this + (showValues ? ", Value: " + hand.value() : ""));
			if (hand.value() > 21) {
				out.println("Dealer busts");
			}
		}

		out.println("\n***** Winnings *****");
		if (hand.value() <= 21) {
			for (Player player : players) {
				for (Hand playerHand : player.getHands()) {
					int score = playerHand.value();
					if (score > hand.value()) {
						out.println("Awarding " + player.getBet() + " to player " + player.getName());
						player.addBalance(player.getBet());
					}
				}
			}
		} else {
			for (Player player : players) {
				for (Hand playerHand : player.getHands()) {
					int score = playerHand.value();
					if (score <= 21) {
						out.println("Awarding " + player.getBet() + " to player " + player.getName());
						player.addBalance(player.getBet());
					}
				}
			}
		}

		// Show winners
		// int numWinners = 0;
		// if (hand.value() > maxScore && hand.value() <= 21) {
		// out.println("\n***** Dealer wins *****");
		// } else {
		// out.println("\n***** Winner(s) *****");
		// for (Player winner : winners) {
		// out.println(winner.getName());
		// numWinners++;
		// }
		// // dealer
		// if (hand.value() >= maxScore && hand.value() <= 21) {
		// out.println("Dealer " + this.name);
		// numWinners++;
		// }
		//
		// if (numWinners == 0) {
		// out.println("Error: nobody won this round. This should not happen");
		// return true;
		// }
		//
		// // Give winnings
		// for (Player winner : winners) {
		// winner.addBalance(winner.getBet());
		// out.println("New balance for " + winner.getName() + " is " +
		// winner.getBalance());
		// }
		// }

		return true;
	}

	void showResults(int round) {
		if (players.size() == 0) {
			return;
		}
		out.println("\n***** Round " + round + " results *****");
		for (Player player : players) {
			// out.println(player);
			out.println(player.print(showValues)); // + (showValues ? ", Value: " + player.getHandValue() : ""));
		}
		out.println(this + ", value: " + hand.value());
	}

	// void String holeHand() {
	// // String str="";
	// return "Dealer: "+name+", hand: "+hand;
	// }

	@Override
	public String toString() {
		return "Dealer: " + name + ", hand: " + hand;
	}
}

class Player {
	String name;
	ArrayList<Hand> hands;
	int balance;
	int bet;
	boolean surrender;

	Player(String name, int balance) {
		this.name = name;
		this.balance = balance;
		hands = new ArrayList<Hand>();
		balance = 1;
		surrender = false;
	}

	boolean getSurrender() {
		return surrender;
	}

	void setSurrender(boolean b) {
		surrender = b;
	}

	String getName() {
		return name;
	}

	int getBet() {
		return bet;
	}

	void setBet(int bet) {
		this.bet = bet;
	}

	int getBalance() {
		return balance;
	}

	int numHands() {
		return hands.size();
	}

	void split(int numHand) {
		Hand newHand = new Hand();
		newHand.setSplit();
		newHand.addCard(hands.get(numHand).removeSecond());
		hands.add(numHand + 1, newHand);
		hands.get(numHand).setSplit();
	}

	ArrayList<Hand> getHands() {
		return hands;
	}

	String getHand(int numHand, boolean showValues) {
		String str = "";
		Hand hand = hands.get(numHand);
		str += hand;

		if (showValues) {
			str += ", value: " + hand.value();
		}
		return str;
	}

	Hand getHand(int numHand) {
		return hands.get(numHand);
	}

	int getHandValue(int numHand) {
		return hands.get(numHand).value();
	}

	void giveCard(Card card, int numHand) {
		hands.get(numHand).addCard(card);
	}

	void clearHand() {
		hands.clear();
		hands.add(new Hand());
	}

	void addBalance(int add) {
		balance += add;
	}

	String print(boolean showValues) {
		String str;
		str = "Player: " + name + ", balance: " + balance;
		for (Hand hand : hands) {
			str += ", hand: " + hand;
			if (showValues) {
				str += ", value: " + hand.value();
			}
		}
		return str;
	}

	@Override
	public String toString() {
		String str;
		str = "Player: " + name + ", balance: " + balance;
		for (Hand hand : hands) {
			str += ", hand: " + hand;
		}
		return str;
	}
}

class Scan {
	static Scanner scan = new Scanner(System.in);
	private static boolean useDefaults = false;

	public static void useDefaults() {
		out.println("Using defaults for prompts...");
		useDefaults = true;
	}

	public static String readLine(String prompt) {
		System.out.print(prompt);
		return scan.nextLine();
	}

	public static int readInt(String prompt) {
		while (true) {
			String input = readLine(prompt);
			try {
				int n = Integer.parseInt(input);
				return n;
			} catch (NumberFormatException e) {
				out.println("Invalid input. Please enter a number");
			}
		}
	}

	public static int readBet(String prompt, int min, int max) {
		if (useDefaults) {
			out.println(prompt + min);
			return min;
		}
		int n;
		while (true) {
			String input = readLine(prompt);
			input = input.trim();
			if (input.length() == 0) {
				return min;
			}
			try {
				n = Integer.parseInt(input);
				if (n > max) {
					out.println("Max bet is " + max + ". Try again");
					continue;
				} else if (n < min) {
					out.println("Min bet is " + min + ". Try again");
					continue;
				}
				return n;
			} catch (NumberFormatException e) {
				out.println("Invalid input. Please enter a number");
			}
		}
	}

	public static int readInt(String prompt, int default_) {
		if (useDefaults) {
			out.println(prompt + " (" + default_ + ")? " + default_);
			return default_;
		}
		while (true) {
			String input = readLine(prompt + " (" + default_ + ")? ");
			input = input.trim();
			if (input.length() == 0) {
				return default_;
			}
			try {
				int n = Integer.parseInt(input);
				return n;
			} catch (NumberFormatException e) {
				out.println("Invalid input. Please enter a number");
			}
		}
	}

	public static String readName(String prompt, ArrayList<Player> players, String default_) {
		if (useDefaults) {
			out.println(prompt + default_);
			return default_;
		}
		String input;
		prompt_: while (true) {
			input = readLine(prompt);
			input = input.trim();
			if (input.length() == 0) {
				input = default_;
			}
			for (Player player : players) {
				if (input.equals(player.getName())) {
					out.println("Player name already taken. Please choose another name");
					continue prompt_;
				}
			}
			if (input.length() > 0 && input.length() <= 10) {
				break;
			}
			out.println("Invalid input. Name length must be between one and ten characters");
		}
		return input;
	}

	public static boolean readBoolean(String prompt, boolean default_) {
		char yes = default_ ? 'Y' : 'y';
		char no = default_ ? 'N' : 'n';
		prompt += " (" + yes + "/" + no + ")? ";
		if (useDefaults) {
			out.println(prompt + (default_ ? 'Y' : 'N'));
			return default_;
		}
		while (true) {
			String input = readLine(prompt);
			input = input.trim();

			if (input.length() == 0) {
				return default_;
			}

			if (input.equalsIgnoreCase("y")) {
				return true;
			}

			if (input.equalsIgnoreCase("n")) {
				return false;
			}
			out.println("Invalid input. Please enter 'y' or 'n'");
		}
	}

	public static char readChoice(String prompt, String choices) {
		String input;
		char[] charArray = choices.toCharArray();
		while (true) {
			input = readLine(prompt + "? ").trim();
			switch (input.length()) {
				// case 0:
				// return default_;
				case 1:
					for (char choice : charArray) {
						if (input.charAt(0) == choice) {
							return choice;
						}
					}
					break;
				default:
					out.println("Invalid input. Please enter one letter from \"" + choices + "\"");
			}
		}
	}

}

public class BlackJack {

	public static void main(String[] args) {
		ArrayList<Player> players = new ArrayList<>();
		out.println("Welcome to Blackjack\n");
		out.println("Rules:");
		out.println(" - Resplit to 4");
		out.println(" - After splitting aces, only one card will be dealt to each ace");

		Scan.useDefaults();
		int numDecks = Scan.readInt("How many decks in the shoe", 4);
		int minBet = Scan.readInt("Min bet", 1);
		int maxBet = Scan.readInt("Max bet", minBet * 2);
		boolean showValues = Scan.readBoolean("Show hand values", true);
		int numPlayers = Scan.readInt("How many players", 1);
		int defBalance = Math.max(minBet * 2, 1);
		int numBalance = Scan.readInt("Starting balance", defBalance);

		for (int i = 1; i <= numPlayers; i++) {
			String name = Scan.readName("Player " + i + " name (player" + i + ")? ", players, "player" + i);
			players.add(new Player(name, numBalance));
		}

		Dealer dealer;
		int round = 0;
		while (true) {
			round++;
			dealer = new Dealer("Bob", players, numDecks, minBet, maxBet, showValues);
			if (!dealer.newRound()) {
				break;
			}

			dealer.showResults(round);

			if (!dealer.checkBalances()) {
				out.println("\nNo players with sufficient balance to play another round");
				break;
			}
			String again = Scan.readLine("\nAgain (Y/n)? ");
			if (again.equals("n")) {
				break;
			}
		}

		out.println("\nThank you for playing");
	}
}
