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
				// default ->value += 0;
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
	Shoe shoe;

	Dealer(String name, ArrayList<Player> players) {
		this.name = name;
		this.players = players;
		shoe = new Shoe(1);
	}

	void newRound() {
		// deal two cards to every player
		for (Player player : players) {
			player.giveCard(shoe.getCard());
			player.giveCard(shoe.getCard());
		}

		String turn;
		for (Player player : players) {
			while (true) {
				out.println(player);
				turn = Scan.readLine("Player " + player.name + " (H)it or (s)tand? ");
				if (turn.equals("s"))
					break;
				player.giveCard(shoe.getCard());
				if (player.hand.value() > 21) {
					out.println("Player " + player.name + " busts");
					break;
				}
			}
		}

		out.println("Results: ");
		for (Player player : players) {
			out.println("" + player);
		}
	}
}

class Player {
	String name;
	Hand hand;

	Player(String name) {
		this.name = name;
		hand = new Hand();
	}

	void giveCard(Card card) {
		hand.addCard(card);
	}

	int getHandValue() {
		return hand.value();
	}

	@Override
	public String toString() {
		return "Name: " + name + ", hand: " + hand + ", value: " + hand.value();
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

			}
		}
	}
}

public class BlackJack {

	public static void main(String[] args) {
		ArrayList<Player> players = new ArrayList<>();
		out.println("Welcome to Black Jack");

		Dealer dealer = new Dealer("Bob", players);

		int numPlayers = Scan.readInt("How many players? ");

		for (int i = 1; i <= numPlayers; i++) {
			String name = Scan.readLine("Player " + i + " name? ");
			players.add(new Player(name));

		}
		while (true) {
			dealer.newRound();

			ArrayList<Player> winners = new ArrayList<>();
			int maxScore = 0;
			for (Player player : players) {
				int score = player.hand.value();
				if (score > maxScore) {
					maxScore = score;
					winners.clear();
					winners.add(player);
				}
			}
			out.println("Winner(s): ");
			for (Player winner : winners) {
				out.println(winner);
			}
			String again = Scan.readLine("Again (Y/n)? ");
			if (again.equals("n"))
				break;
		}
	}
}
