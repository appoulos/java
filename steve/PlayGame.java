import java.util.Scanner;

class PlayGame {
	static Scanner scan = new Scanner(System.in);

	static int question(String q, int a) {
		String line;
		while (true) {
			System.out.print("What is " + q + "? ");
			line = scan.nextLine();
			if (line.equals("q")) {
				return 0;
			}
			if (line.equals(String.valueOf(a))) {
				System.out.println("Good job!");
				return 1;
			}
			System.out.println("Try again...");
		}
	}

	public static void main(String[] args) {
		System.out.println("Hi there!");
		Scanner scan = new Scanner(System.in);
		while (true) {
			System.out.println("Would you like to play a math game?\n\t\ty (yes)\t\t\t\tn (no)");
			if (!scan.hasNextLine()) {
				scan.close();
				return;
			}
			String raisin = scan.nextLine();
			if (raisin.toLowerCase().equals("y")) {
				System.out.println("Yay, the game will be coming soon though :).");
				break;
			}
			if (raisin.toLowerCase().equals("n")) {
				System.out.println("Oh, that's okay :).");
				break;
			}
			System.out.println("Please answer 'y' or 'n'...");
		}
		while (true) {
			int n1 = (int) (Math.random() * 10);
			int n2 = (int) (Math.random() * 10);
			long begTime = System.currentTimeMillis();
			if (question(n1 + " + " + n2, n1 + n2) == 0) {
				break;
			}
			long totTime = System.currentTimeMillis() - begTime;
			System.out.printf("Total time: %d ms.\n", totTime);
		}
		scan.close();
		return;
	}
}
