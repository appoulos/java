import static java.lang.System.out;
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
		return rank + " of " + suit;
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
		out.println("todo");
		int value = 0;
		int aces = 0;
		for (Card card : cards) {
			if (card.rank == "Ace")
				aces++;
			switch (card.rank) {
					case "Ace" -> value += 1;
				case "2", "3", "4", "5", "6", "7", "8", "9", "10" ->
					value = Integer.valueOf(card.rank);
				case "Jack", "Queen", "King" -> value += 10;
				default ->value +=  0;
			}
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
		for (Player player : players) {
			player.giveCard(shoe.getCard());
			player.giveCard(shoe.getCard());
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

public class BlackJack {
	public static void main(String[] args) {
		out.println("hi");
		ArrayList<Player> players = new ArrayList<>();
		players.add(new Player("Alice"));
		players.add(new Player("Suzy"));
		Dealer dealer = new Dealer("Bob", players);
		dealer.newRound();
		for (Player player : players) {
			out.println("" + player);
		}
	}
}
