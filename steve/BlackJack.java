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

	@Override
	public String toString() {
		return rank; // + " of " + suit;
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
		int value = 0;
		int aces = 0;
		for (Card card : cards) {
			if (card.rank == "Ace")
				aces++;
			switch (card.rank) {
				case "Ace":
					value += 1;
					break;
				case "2":
				case "3":
				case "4":
				case "5":
				case "6":
				case "7":
				case "8":
				case "9":
				case "10":
					value += Integer.valueOf(card.rank);
					break;
				case "Jack":
				case "Queen":
				case "King":
					value += 10;
					break;
			}
			// switch (card.rank) {
			// case "Ace" -> value += 1;
			// case "2", "3", "4", "5", "6", "7", "8", "9", "10" ->
			// value = Integer.valueOf(card.rank);
			// case "Jack", "Queen", "King" -> value += 10;
			// default ->value += 0;
			// }
		}
		while (aces > 0 && value + 10 <= 21) {
			value += 10;
			aces--;
		}
		return value;
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

	static int ante = 1;

	Dealer(String name, ArrayList<Player> players) {
		this.name = name;
		this.players = players;
		hand = new Hand();
		shoe = new Shoe(1);
	}

	boolean checkBalances() {
		for (Player player : players) {
			if (player.getBalance() >= ante)
				return true;
		}
		return false;
	}

	boolean newRound() {
		// bounce players with insufficient funds
		for (int i = players.size() - 1; i >= 0; i--) {
			Player player = players.get(i);
			if (player.getBalance() < ante) {
				out.println("Player " + (i + 1) + ", name: " + player.name + " has insufficient balance. Removed from table");
				players.remove(i);
			} else
				player.addBalance(-ante);
		}

		if (players.size() == 0)
			return false;

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
		for (Player player : players) {
			out.println(player);
		}
		// dealer hand
		out.println(this);

		// start hit/stand for each player
		String turn;
		int i = 0;
		for (Player player : players) {
			i++;
			while (true) {
				out.println(player);
				turn = Scan.readLine("Player " + i + ", name: " + player.name + " (H)it or (s)tand? ");
				if (turn.equals("s"))
					break;
				player.giveCard(shoe.getCard());
				if (player.hand.value() > 21) {
					out.println(player);
					out.println("Player " + i + ", name: " + player.name + " busts");
					break;
				}
			}
		}

		// Get winners of players (not including dealer)
		ArrayList<Player> winners = new ArrayList<>();
		int maxScore = 0;
		for (Player player : players) {
			int score = player.hand.value();
			if (score <= 21 && score > maxScore) {
				maxScore = score;
				winners.clear();
				winners.add(player);
			}
		}

		if (winners.size() == 0) {
			out.println("Dealer wins");
			return true;
		}

		// Dealers turn
		out.println("Dealers turn");
		out.println(this);
		while (hand.value() < maxScore && hand.value() != 21) {
			out.println("Dealer hits");
			hand.addCard(shoe.getCard());
			out.println(this);
			if (hand.value() > 21) {
				out.println("Dealer busts");
			}
		}

		// Show winners
		if (hand.value() > maxScore && hand.value() <= 21)
			out.println("Dealer wins");
		else {
			out.println("Winner(s): ");
			for (Player winner : winners) {
				out.println(winner);
				winner.addBalance(1);
			}
			// dealer
			if (hand.value() >= maxScore && hand.value() <= 21)
				out.println(this);
		}
		return true;
	}

	@Override
	public String toString() {
		return "Dealer: " + name + ", hand: " + hand + ", value: " + hand.value();
	}
}

class Player {
	String name;
	Hand hand;
	int balance;

	Player(String name) {
		this.name = name;
		hand = new Hand();
		balance = 1;
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

	int getBalance() {
		return balance;
	}

	void addBalance(int add) {
		balance += add;
	}

	@Override
	public String toString() {
		return "  Name: " + name + ", hand: " + hand + ", value: " + hand.value();
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

	public static String readName(String prompt) {
		String input;
		while (true) {
			input = readLine(prompt);
			input = input.trim();
			if (input.length() > 0 && input.length() <= 10)
				break;
			out.println("Invalid input. Name length must be between one and ten characters");
		}
		return input;
	}
}

public class BlackJack {

	public static void main(String[] args) {
		ArrayList<Player> players = new ArrayList<>();
		out.println("Welcome to Black Jack");

		int numPlayers = Scan.readInt("How many players? ");

		for (int i = 1; i <= numPlayers; i++) {
			String name = Scan.readName("Player " + i + " name? ");
			players.add(new Player(name));
		}

		while (true) {
			Dealer dealer = new Dealer("Bob", players);
			if (!dealer.newRound())
				break;

			if (!dealer.checkBalances()) {
				out.println("No players with sufficient balance to play another round");
				break;
			}
			String again = Scan.readLine("Again (Y/n)? ");
			if (again.equals("n"))
				break;
		}

		out.println("Thank you for playing");
	}
}
