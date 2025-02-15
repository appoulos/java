import static java.lang.System.out;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.Collections;

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

	public String getRank() {
		return rank;
	}
}

class Deck {
	private ArrayList<Card> cards;
	private int numDecks;
	private static String[] ranks = { "Ace", "2", "3", "4", "5", "6", "7", "8", "9", "10", "Jack", "Queen", "King" };
	private static String[] suits = { "Clubs", "Diamonds", "Hearts", "Spades" };

	public Deck(int decks) {
		numDecks = decks;
		cards = new ArrayList<>();
		newDeck();
	}

	public void newDeck() {
		// Fill cards with deck(s)
		for (String rank : ranks) {
			for (int deck = 0; deck < numDecks; deck++) {
				for (String suit : suits) {
					cards.add(new Card(rank, suit));
				}
			}
		}

		for (int i = 0; i < 16; i++)
			cards.remove(0);
		// Shuffle
		out.println("\n***** Shuffling deck(s) *****\n");
		Collections.shuffle(cards);

		// int rand;
		// for (int i = cards.size()-1; i > 0; i--) {
		// rand = (int) (Math.random() * i);
		// Card temp = cards.get(i);
		// cards.set(i, cards.get(rand));
		// cards.set(rand, temp);
		// }
	}

	public Card getCard() {
		if (cards.size() < 52 * numDecks / 4) { // cards left less than 25% restock shoe
			out.println("***** New decks filled in shoe *****");
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
			sep = " ";
		}
		return str;
	}
}

class Shoe {
	private Deck deck;

	public Shoe(int decks) {
		deck = new Deck(decks);
	}

	public Card getCard() {
		return deck.getCard();
	}
}

class Hand {
	private ArrayList<Card> cards;
	private boolean split;
	private int bet;
	private boolean doubleDown;
	private boolean surrender;
	private boolean done;

	public Hand(int bet) {
		cards = new ArrayList<>();
		doubleDown = false;
		surrender = false;
		done = false;
		this.bet = bet;
	}

	public boolean isDone() {
		return done;
	}

	public void setDone() {
		done = true;
	}

	public int getCardNum() {
		return cards.size();
	}

	public boolean isSurrender() {
		return surrender;
	}

	public void setSurrender(boolean surrender) {
		this.surrender = surrender;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public void setSplit() {
		split = true;
	}

	public boolean getSplit() {
		return split;
	}

	public void clear() {
		cards.clear();
	}

	public boolean splitOption() {
		if (cards.size() != 2) {
			return false;
		}
		return cards.get(0).getRank() == cards.get(1).getRank();
	}

	public boolean blackJack() {
		if (!split && value() == 21 && cards.size() == 2) {
			return true;
		}
		return false;
	}

	public int value() {
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

	public Card removeSecond() {
		if (cards.size() >= 2) {
			return cards.remove(1);
		}
		return null;
	}

	public Card getSecond() {
		if (cards.size() >= 2) {
			return cards.get(1);
		}
		return null;
	}

	public Card getFirst() {
		if (cards.size() >= 1) {
			return cards.get(0);
		}
		return null;
	}

	public void addCard(Card c) {
		cards.add(c);
	}

	public void hit(Shoe shoe) {
		cards.add(shoe.getCard());
	}

	@Override
	public String toString() {
		String str = "";
		String sep = "";
		for (Card card : cards) {
			str += sep + card;
			sep = " ";
		}
		return str;
	}

	public void setSurrender() {
		surrender = true;
	}

	public String toString(boolean showValues) {
		return "$" + bet + ", (" + this.value() + ") " + this;
	}

	public String toDealerString(boolean showValues) {
		return "(" + this.value() + ") " + this;
	}

	public boolean isDoubleDown() {
		return doubleDown;
	}

	public void setDoubleDown() {
		doubleDown = true;
	}
}

class Dealer {
	private String name;
	private ArrayList<Player> players;
	private Hand hand;
	private Shoe shoe;
	private int minBet;
	private int maxBet;
	private boolean showValues;

	public Dealer(String name, ArrayList<Player> players, int numDecks, int minBet, int maxBet, boolean showValues) {
		this.name = name;
		this.players = players;
		hand = new Hand(0);
		shoe = new Shoe(numDecks);
		this.minBet = minBet;
		this.maxBet = maxBet;
		this.showValues = showValues;
	}

	public boolean checkBalances() {
		for (Player player : players) {
			if (player.getBalance() >= minBet) {
				return true;
			}
		}
		return false;
	}

	public boolean blackjack() {
		if (hand.blackJack()) {
			return true;
		}
		return false;
	}

	/**
	 * Start new round.
	 * 
	 * @return true if any player has more than ante balance to start another round
	 * @return false if no player has more than ante balance
	 */
	public boolean newRound() {
		hand.clear();

		// Bounce players with insufficient funds
		for (int i = players.size() - 1; i >= 0; i--) {
			Player player = players.get(i);
			if (player.getBalance() < minBet) {
				out.println("Player " + player.getName()
						+ " has insufficient balance. Removed from table with balance $"
						+ player.getBalance());
				players.remove(i);
			}
		}

		if (players.size() == 0) {
			return false;
		}

		// Get player bets
		for (Player player : players) {
			int bet = Scan.readBet("" + player.getName() + " bet", minBet,
					Math.min(maxBet, player.getBalance()));
			player.addBalance(-bet);
			player.setBet(bet);

			// deal two cards to every player
			player.newHand(shoe, bet);
		}

		// dealer gets two cards
		hand.addCard(shoe.getCard());
		hand.addCard(shoe.getCard());

		// show player hands
		out.println("\n***** At the table *****");
		for (Player player : players) {
			out.println(player.toString(showValues));
		}

		// dealer hand hide one card
		if (blackjack()) {
			out.println(this + " blackjack!");
		} else {
			out.println("Dealer: " + "\uf656 " + hand.getSecond());

			// start hit/stand for each player
			char choice;
			for (Player player : players) {
				out.println("\n***** Player " + player.getName() + "'s turn *****");

				// int turn = 0;
				for (int handNum = 0; handNum < player.getHands().size(); handNum++) {
					Hand playerHand = player.getHand(handNum);

					if (playerHand.isDone()) {
						continue;
					}

					// turn++;
					// if (handNum > 0) {
					// out.println("Hand number: " + (handNum + 1));
					// }

					out.println(playerHand.toString(showValues));

					if (playerHand.value() == 21) {
						out.println("***** " + (playerHand.blackJack() ? "blackjack!" : "21") + " *****");
						// Scan.readPause();
						continue;
					}

					ArrayList<String> choices = new ArrayList<>();
					ArrayList<Character> keys = new ArrayList<>();

					// NOTE: allow multiple splits
					// if (!playerHand.getSplit() || playerHand.getFirst().rank != "Ace") {
					choices.add("(h)it");
					keys.add('h');
					// }

					choices.add("(s)tay");
					keys.add('s');

					// Balance required for split or double down
					boolean enoughBalance = false;
					if (player.getBalance() >= player.getBet()) {
						enoughBalance = true;
					}

					boolean twoCards = false;
					if (playerHand.getCardNum() == 2) {
						twoCards = true;
					}

					// This option allows you to double your initial bet and receive only
					// one additional card, but you can only double down on your initial
					// two cards, not after splitting
					// if (handNum == 1 && twoCards && !playerHand.isDoubleDown() && enoughBalance
					// && turn == 1) {
					if (twoCards && !playerHand.isDoubleDown() && enoughBalance) {
						choices.add("(d)ouble down");
						keys.add('d');
					}

					if (twoCards && enoughBalance && player.numHands() < 4 && playerHand.splitOption()) {
						choices.add("sp(l)it");
						keys.add('l');
					}

					if (twoCards && player.numHands() < 2) {
						choices.add("s(u)rrender");
						keys.add('u');
					}

					choice = Scan.readChoice2(choices, keys);

					switch (choice) {
						case 'h':
							playerHand.hit(shoe);
							if (playerHand.value() > 21) {
								out.println(playerHand.toString(showValues));
								out.println("\n***** Player " + player.getName() + " busts *****");
								// Scan.readPause();
								break;
							}
							handNum--; // Don't go to next hand
							break;
						case 's':
							break;
						case 'd':
							player.addBalance(-player.getBet());
							playerHand.setBet(player.getBet() * 2);
							playerHand.setDoubleDown();
							playerHand.hit(shoe);
							playerHand.setDone();
							out.println(playerHand.toString(showValues));
							if (playerHand.value() > 21) {
								out.println("\n***** Player " + player.getName() + " busts *****");
								// Scan.readPause();
								break;
							}
							break;
						case 'l':
							player.split(shoe, handNum, showValues);
							handNum--; // Don't go to next hand
							break;
						case 'u': // NOTE: test don't allow after splitting
							playerHand.setSurrender();
							break;
						default:
							out.println("Error: unreachable switch: " + choice);
							System.exit(1);
					}
				}
			}
		}

		boolean possibleWinners = false;
		for (Player player : players) {
			for (Hand playerHand : player.getHands()) {
				int score = playerHand.value();
				if (score <= 21) {
					possibleWinners = true;
				}
			}
		}

		// Dealers turn
		if (!blackjack() && possibleWinners) {
			out.println("\n***** Dealers turn *****");
			out.println(this.toString(showValues));
			// Dealer rule must hit below 17
			while (hand.value() < 17) {
				out.println("Dealer hits");
				hand.addCard(shoe.getCard());
				out.println(this.toString(showValues));
				if (hand.value() > 21) {
					out.println("\n***** Dealer busts *****");
				}
			}
		}

		out.println("\n***** Results *****");
		int dealerHandValue = hand.value();

		for (Player player : players) {
			out.println(" - " + player.getName());
			for (Hand playerHand : player.getHands()) {
				out.print("   " + playerHand.toString(showValues) + ": ");
				if (playerHand.isSurrender()) {
					int payment = player.getBet() / 2;
					out.println("surrender half $"+ payment);
					player.addBalance(payment);
					continue;
				}
				int playerHandValue = playerHand.value();
				if (playerHandValue > 21) {
					out.println("loses bet");
					continue;
				}
				if (playerHandValue > dealerHandValue || dealerHandValue > 21) {
					float payout = 2.0f; // 1:1
					if (playerHand.blackJack()) {
						payout = 2.5f; // 3:2
					}
					int winnings = (int) (player.getBet() * payout);
					out.println("awarding $" + winnings);
					player.addBalance(winnings);
				} else if (playerHandValue == dealerHandValue) {
					if (blackjack() && playerHand.blackJack() || playerHandValue < 21) {
						out.println("push");
						player.addBalance(player.getBet());
					}
				} else {
					out.println("loses bet");
				}
			}
		}

		return true;
	}

	public void showResults(int round) {
		if (players.size() == 0) {
			return;
		}
		out.println("\n***** Round " + round + " *****");
		for (Player player : players) {
			out.println(player.toString(showValues));
		}
	}

	public String toString(boolean showValue) {
		return "Dealer: " + name + ", " + hand.toDealerString(showValue);
	}

	@Override
	public String toString() {
		return "Dealer: " + name + ", hand: " + hand;
	}
}

class Player {
	private String name;
	private ArrayList<Hand> hands;
	private int balance;
	private int bet;
	private boolean split;

	public Player(String name, int balance) {
		this.name = name;
		this.balance = balance;
		hands = new ArrayList<Hand>();
		split = false;
	}

	public void surrender(int handNum) {
		hands.get(handNum).setSurrender();
	}

	public String getName() {
		return name;
	}

	public int getBet() {
		return bet;
	}

	public void setBet(int bet) {
		this.bet = bet;
	}

	public int getBalance() {
		return balance;
	}

	public int numHands() {
		return hands.size();
	}

	public void split(Shoe shoe, int numHand, boolean showValues) {
		split = true;

		Hand hand = hands.get(numHand);
		Hand newHand = new Hand(hand.getBet());
		newHand.addCard(hand.removeSecond());
		newHand.addCard(shoe.getCard());
		hands.add(numHand + 1, newHand);
		balance -= hand.getBet();

		hand.addCard(shoe.getCard());

		hand.setSplit();
		newHand.setSplit();

		// Only one card with split Ace's
		if (hand.getFirst().getRank() == "Ace") {
			hand.setDone();
			newHand.setDone();
			out.println("Hand " + (numHand + 1) + ": " + hand.toString(showValues));
			out.println("Hand " + (numHand + 2) + ": " + newHand.toString(showValues));
		}
	}

	public ArrayList<Hand> getHands() {
		return hands;
	}

	public Hand getHand(int numHand) {
		return hands.get(numHand);
	}

	public int getHandValue(int numHand) {
		return hands.get(numHand).value();
	}

	public boolean getSplit() {
		return split;
	}

	public void newHand(Shoe shoe, int bet) {
		this.bet = bet;
		split = false;
		hands.clear();
		Hand hand = new Hand(bet);
		hand.addCard(shoe.getCard());
		hand.addCard(shoe.getCard());
		hands.add(hand);
	}

	public void addBalance(int add) {
		balance += add;
	}

	public String toString(boolean showValues) {
		String str;
		str = "Player: " + name + ", balance: " + balance;
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

	public static boolean isDefaults() {
		return useDefaults;
	}

	public static void setDefaults(boolean b) {
		useDefaults = b;
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

	public static int readInt(String prompt, int min, int max) {
		String minToMax = min + "-" + max;
		if (min == max) {
			minToMax = "" + min;
		}
		prompt = prompt + " (" + minToMax + ")? ";
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
					out.println("Max is " + max + ". Try again");
					continue;
				} else if (n < min) {
					out.println("Min is " + min + ". Try again");
					continue;
				}
				return n;
			} catch (NumberFormatException e) {
				out.println("Invalid input. Please enter a number");
			}
		}
	}

	public static int readBet(String prompt, int min, int max) {
		String minToMax;
		max = max/10*10;
		if (min == max) {
			minToMax = "" + min;
		} else {
			minToMax = "(" + min + ")-" + max;
		}
		prompt = prompt + " [" + minToMax + "]? ";
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
					out.println("Max is " + max + ". Try again");
					continue;
				} else if (n < min) {
					out.println("Min is " + min + ". Try again");
					continue;
				} else if (n % 10 != 0) {
					out.println("Bets must be in increments of 10. Try again");
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
		char no = default_ ? 'n' : 'N';
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

	public static char readChoice2(ArrayList<String> choices, ArrayList<Character> keys) {
		String prompt = "";
		String sep = "";
		for (String choice : choices) {
			prompt += sep + choice;
			sep = ", ";
		}
		String input;
		while (true) {
			input = readLine(prompt + "? ").trim();
			switch (input.length()) {
				case 1:
					char choice = input.charAt(0);
					if (keys.contains(choice)) {
						return choice;
					}
					break;
				default:
					out.println("Invalid input. Please enter one letter from \"" + choices + "\"");
			}
		}
	}

	public static void readPause() {
		readLine("Press enter to continue...");
	}
}

class Game {
	// private int round;
	private Dealer dealer;

	public Game(String dealerName, ArrayList<Player> players, int numDecks, int minBet, int maxBet, boolean showValues) {
		dealer = new Dealer("Bob", players, numDecks, minBet, maxBet, showValues);
	}

	public void newGame() {
		int round = 0;
		while (true) {
			round++;

			if (!dealer.newRound()) {
				break;
			}

			dealer.showResults(round);

			if (!dealer.checkBalances()) {
				out.println("\nNo players with sufficient balance to play another round");
				break;
			}
			if (!Scan.readBoolean("\nAnother round", true)) {
				break;
			}
		}
	}
}

public class BlackJack {
	public static void main(String[] args) {
		out.println("Welcome to Blackjack\n");
		out.println("Rules:");
		out.println(" - Minimum bet 10");
		out.println(" - Betting in increments of 10");
		out.println(" - Resplit to 4 hands");
		out.println(" - After splitting aces, only one card will be dealt to each ace (no blackjacks)");
		out.println(" - Cannot surrender after splitting your hand");
		out.println(" - Double down after split");
		out.println(" - Double down if first two cards are 11 or less (rule or suggestion?)");

		int numDecks;
		int minBet;
		int maxBet;
		boolean showValues;
		int numPlayers;
		int defBalance;
		int numBalance;
		ArrayList<Player> players = new ArrayList<>();

		Scan.setDefaults(true);

		while (true) {
			out.println();
			numDecks = Scan.readBet("How many decks in the shoe", 4, 8);
			minBet = 10; // 3:2 return will keep integers balances with minBet of 10
			maxBet = Scan.readInt("Max bet", minBet * 100);
			showValues = Scan.readBoolean("Show hand values", true);
			defBalance = minBet * 10;
			numBalance = Scan.readInt("Starting balance", defBalance);
			numPlayers = Scan.readInt("How many players", 2);
			players.clear();
			for (int i = 1; i <= numPlayers; i++) {
				String name = Scan.readName("Player " + i + " name (player" + i + ")? ", players, "player" + i);
				players.add(new Player(name, numBalance));
			}
			Scan.setDefaults(false);
			out.println();
			Scan.setDefaults(Scan.readBoolean("Use these defaults", true));
			if (Scan.isDefaults()) {
				break;
			}
		}
		Scan.setDefaults(false);

		Game game = new Game("Bob", players, numDecks, minBet, maxBet, showValues);
		game.newGame();

		out.println("\nThank you for playing");
	}
}
