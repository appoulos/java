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
		for (int deck = 0; deck < numDecks; deck++) {
			for (String rank : ranks) {
				for (String suit : suits) {
					cards.add(new Card(rank, suit));
				}
			}
		}
		int deckSize = cards.size();
		for (int i = 0; i < deckSize; i++) {
			Card temp = cards.get(i);
			cards.set((int) (Math.random() * deckSize), cards.get(i));
			cards.set(i, temp);
		}
	}

	Card getCard() {
		if (cards.size() < 52 * numDecks / 2) {
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

	Hand() {
		cards = new ArrayList<>();
	}

	void clear() {
		cards.clear();
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
	int ante;
	boolean showValues;

	Dealer(String name, ArrayList<Player> players, int numDecks, int ante, boolean showValues) {
		this.name = name;
		this.players = players;
		hand = new Hand();
		shoe = new Shoe(numDecks);
		this.ante = ante;
		this.showValues = showValues;
	}

	boolean checkBalances() {
		for (Player player : players) {
			if (player.getBalance() >= ante) {
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
		int pot = ante; // dealer puts in initial ante
		// bounce players with insufficient funds
		for (int i = players.size() - 1; i >= 0; i--) {
			Player player = players.get(i);
			if (player.getBalance() < ante) {
				out.println("Player " + player.getName()
						+ " has insufficient balance. Removed from table with balance " + player.getBalance());
				players.remove(i);
			} else {
				player.addBalance(-ante);
				pot += ante;
			}
		}

		if (players.size() == 0) {
			return false;
		}

		// deal two cards to every player
		for (Player player : players) {
			player.clearHand();
			player.giveCard(shoe.getCard());
			player.giveCard(shoe.getCard());
		}
		// dealer gets two cards
		hand.addCard(shoe.getCard());
		hand.addCard(shoe.getCard());

		// show player hands
		out.println("\n***** At the table *****");
		for (Player player : players) {
			out.println(player + (showValues ? ", Value: " + player.getHandValue() : ""));
		}
		// dealer hand
		out.println(this + (showValues ? ", Value: " + hand.value() : ""));

		// start hit/stand for each player
		String turn;
		for (Player player : players) {
			out.println("\n***** Player " + player.getName() + "'s turn *****");
			while (true) {
				out.println(player.getHand());
				if (player.getHandValue() == 21) {
					out.println("Player " + player.name + " blackjack");
					break;
				}
				turn = Scan.readLine("(H)it or (s)tand? ");
				if (turn.equalsIgnoreCase("s")) {
					break;
				}
				player.giveCard(shoe.getCard());
				if (player.getHandValue() > 21) {
					out.println(player + (showValues ? ", Value: " + player.getHandValue() : ""));
					out.println("Player " + player.name + " busts");
					break;
				}
			}
		}

		// Get winners of players (not including dealer)
		ArrayList<Player> winners = new ArrayList<>();
		int maxScore = 0;
		for (Player player : players) {
			int score = player.getHandValue();
			if (score <= 21 && score > maxScore) {
				maxScore = score;
				winners.clear();
				winners.add(player);
			} else if (score == maxScore) {
				winners.add(player);
			}
		}

		if (winners.size() == 0) {
			out.println("\n***** Dealer wins *****");
			return true;
		}

		// Dealers turn
		out.println("\n***** Dealers turn *****");
		out.println(this + (showValues ? ", Value: " + hand.value() : ""));
		while (hand.value() < maxScore && hand.value() != 21) {
			out.println("Dealer hits");
			hand.addCard(shoe.getCard());
			out.println(this + (showValues ? ", Value: " + hand.value() : ""));
			if (hand.value() > 21) {
				out.println("Dealer busts");
			}
		}

		// Show winners
		int numWinners = 0;
		if (hand.value() > maxScore && hand.value() <= 21) {
			out.println("\n***** Dealer wins *****");
		} else {
			out.println("\n***** Winner(s) *****");
			for (Player winner : winners) {
				out.println(winner.getName());
				numWinners++;
			}
			// dealer
			if (hand.value() >= maxScore && hand.value() <= 21) {
				out.println("Dealer " + this.name);
				numWinners++;
			}

			if (numWinners == 0) {
				out.println("Error: nobody won this round. This should not happen");
				return true;
			}

			// Give winnings. Dealer keeps the remainder
			int winnerPot = pot / numWinners;
			if (winners.size() > 0) {
				// out.println("Winners each receive " + winnerPot);
				for (Player winner : winners) {
					winner.addBalance(winnerPot);
					out.println("New balance for " + winner.getName() + " is " + winner.getBalance());
				}
			}
		}
		return true;
	}

	void showResults(int round) {
		if (players.size() == 0) {
			return;
		}
		out.println("\n***** Round " + round + " results *****");
		for (Player player : players) {
			out.println(player + ", value: " + player.getHandValue());
		}
		out.println(this + ", value: " + hand.value());
	}

	@Override
	public String toString() {
		return "Dealer: " + name + ", hand: " + hand;
	}
}

class Player {
	String name;
	Hand hand;
	int balance;

	Player(String name, int balance) {
		this.name = name;
		this.balance = balance;
		hand = new Hand();
		balance = 1;
	}

	String getName() {
		return name;
	}

	int getBalance() {
		return balance;
	}

	Hand getHand() {
		return hand;
	}

	void giveCard(Card card) {
		hand.addCard(card);
	}

	void clearHand() {
		hand.clear();
	}

	int getHandValue() {
		return hand.value();
	}

	void addBalance(int add) {
		balance += add;
	}

	@Override
	public String toString() {
		return "Player: " + name + ", balance: " + balance + ", hand: " + hand;
	}
}

class Scan {
	static Scanner scan = new Scanner(System.in);

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

	public static int readInt(String prompt, int default_) {
		while (true) {
			String input = readLine(prompt);
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

	public static boolean readBoolean(String prompt, String default_) {
		while (true) {
			String input = readLine(prompt);
			input = input.trim();

			if (input.length() == 0) {
				input = default_;
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
}

public class BlackJack {

	public static void main(String[] args) {
		ArrayList<Player> players = new ArrayList<>();
		out.println("Welcome to Blackjack");

		int numDecks = Scan.readInt("How many decks in the shoe (1)? ", 1);
		int ante = Scan.readInt("Ante (1)? ", 1);
		boolean showValues = Scan.readBoolean("Show hand values (y/N)? ", "N");
		int numPlayers = Scan.readInt("How many players (1)? ", 1);
		int defBalance = Math.max(ante, 1);
		int numBalance = Scan.readInt("Starting balance (" + defBalance + ")? ", defBalance);

		for (int i = 1; i <= numPlayers; i++) {
			String name = Scan.readName("Player " + i + " name (player" + i + ")? ", players, "player" + i);
			players.add(new Player(name, numBalance));
		}

		Dealer dealer;
		int round = 0;
		while (true) {
			round++;
			dealer = new Dealer("Bob", players, numDecks, ante, showValues);
			if (!dealer.newRound()) {
				break;
			}

			dealer.showResults(round);

			if (!dealer.checkBalances()) {
				out.println("No players with sufficient balance to play another round");
				break;
			}
			String again = Scan.readLine("Again (Y/n)? ");
			if (again.equals("n")) {
				break;
			}
		}

		out.println("\nThank you for playing");
	}
}
