public class RockPaperScissors extends ConsoleProgram {
	public static final String USER_PLAYER = "User wins!";
	public static final String COMPUTER_PLAYER = "Computer wins!";
	public static final String TIE = "Tie";

	public String getWinner(String user, String computer) {
		if (user == null || computer == null)
			return "Invalid data";
		if ((user.equals("rock") && computer.equals("scissors")) || (user.equals("paper") && computer.equals("rock"))
				|| (user.equals("scissors") && computer.equals("paper"))) {
			return USER_PLAYER;
		} else if (user.equals(computer)) {
			return TIE;
		} else {
			return COMPUTER_PLAYER;
		}
	}

	public void run() {

		RockPaperScissors game = new RockPaperScissors();
		String user = "paper";
		String comp = null;

		System.out.println(game.getWinner(user, comp));

		while (true) {
			double computer = (int) ((Math.random() * 3) + 1);
			user = readLine("Enter your choice (rock, paper, or scissors)");
			if (computer == 1) {
				System.out.println("Computer: rock");
				comp = "rock";
			} else if (computer == 2) {
				System.out.println("Computer: paper");
				comp = "paper";
			} else if (computer == 3) {
				System.out.println("Computer: scissors");
				comp = "scissors";
			} else {
				System.out.println("Please try again");
			}

			System.out.println(game.getWinner(user, comp));
		}
	}
}
