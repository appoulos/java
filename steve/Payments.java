public class Payments extends ConsoleProgram {
	public void run() {
		println("Welcome to a bank...\n");
		double principal = readDouble("What is your principal? ");
		double interest = readDouble("What is the annual interest rate? ");
		double payment = readDouble("What is your monthly payment? ");
		// double principal = 10000;
		// double interest = 1;
		// double payment = 300;
		double monthInterest = 0;
		double totInterest = 0;
		double newBalance = principal;
		int month = 1;
		interest /= 100;
		println("Month\tPrincipal\tInterest\tPayment\tNew Balance");
		while (principal >= payment) {
			monthInterest = principal * interest;
			newBalance = principal + monthInterest - payment;
			totInterest += monthInterest;
			print(month + "\t" +
					round(principal) + "\t\t" +
					round(monthInterest) + "\t\t" +
					round(payment) + "\t" +
					round(newBalance)
					+ "\n");
			principal = newBalance;
			month += 1;
		}
		println(round(totInterest) + " total interest");
	}

	public static double round(double value) {
		double roundedValue = Math.round(value * 100.0) / 100.0;
		return roundedValue;
	}
}
