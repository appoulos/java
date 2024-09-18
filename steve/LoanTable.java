public class LoanTable extends ConsoleProgram {
	public void run() {
		double c = 0;
		double a = 0;
		double lowRate = 0;
		double highRate = 0;
		double p = 0;
		double k = 0;
		double n = 0;
		println("Welcome to a bank...\n\n");
		{
			int x = 1;
			int y = 2;
			int z = 3;
			println((x < y) ? ((x < z) ? x : z) : (y < z) ? y : z);
		}
		String yesOrNo = readLine("Would you like to take out a loan... (y/n) ");
		while (true) {
			if (yesOrNo.equals("y")) {
				println("Alright...");
				break;
			} else if (yesOrNo.equals("n")) {
				println("Okay... Goodbye!");
				return;
			} else {
				println("Please try again...");
			}
			yesOrNo = readLine("Would you like to take out a loan... (y/n) ");
		}
		p = readDouble("What is the amount of money you would like to take out? ");
		k = readDouble("What is the annual interest rate? ");
		n = readDouble("In how many years would you like the loan to be paid off? ");

		// Quick testing:
		// p = 100000;
		// k = 11;
		// n = 30;

		n *= 12;
		c = Math.pow((1 + k / 1200), n);
		a = (p * k / 1200 * c) / (c - 1);
		a = (Math.round(a * 100.0) / 100.0);
		println(
				"Principal: $" + String.format("%.2f", p) + "\nLength: " + String.format("%.2f", n / 12)
						+ " years\nInterest Rate: " + String.format("%.0f", k) +
						"%" + "\nMonthly Payment: $" + a);

		yesOrNo = readLine("Would you like to take out a loan... (y/n) ");
		while (true) {
			if (yesOrNo.equals("y")) {
				println("Alright...");
				break;
			} else if (yesOrNo.equals("n")) {
				println("Okay... Goodbye!");
				return;
			} else {
				println("Please try again...");
			}
			yesOrNo = readLine("Would you like to take out a loan... (y/n) ");
		}

		p = readDouble("What is the amount of money you would like to take out? ");
		lowRate = readDouble("What is the low annual interest rate? ");
		highRate = readDouble("What is the high annual interest rate? ");
		n = readDouble("In how many years would you like the loan to be paid off? ");

		// Quick testing:
		// p = 100000;
		// lowRate = 10;
		// highRate = 11;
		// n = 30;

		println("\nAnnual Interest Rate\tMonthly Payment");
		while (lowRate <= highRate) {
			c = Math.pow((1 + lowRate / 1200.0), n * 12);
			a = (p * lowRate / 1200.0 * c) / (c - 1);
			// println("c: " + c + " a: " + a);
			a = (Math.round(a * 100.0) / 100.0);
			println((Math.round(lowRate * 1000.0) / 1000.0) + "%\t\t\t$" + String.format("%.2f", a));
			lowRate += 0.25;
		}

		println("\nThank you for using this bank. Goodbye!");
	}
}
