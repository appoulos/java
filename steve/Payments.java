public class Payments extends ConsoleProgram {
	public void run() {
		println("Welcome to a bank...\n");
		double principal = 0.0f;
		double interest = 0.0f;
		double payment = 0.0f;
		double minPayment = 0.0f;

		while (true) {
			principal = readDouble("What is your principal? ");
			if (principal > 0) {
				break;
			}
			println("Invalid principal. Please enter a number greater than 0");
		}

		while (true) {
			interest = readDouble("What is the annual interest rate? ");
			interest /= 12;
			if (interest >= 0 && interest < 100) {
				break;
			}
			println("Invalid interest rate. Please enter a number between 0 and 100");
		}

		minPayment = 0.01 + Math.round(principal * ((1.0 + interest / 100.0) - 1.0) * 100.0) / 100.0;

		while (true) {
			payment = readDouble("What is your monthly payment? ");
			if (minPayment <= payment) {
				break;
			}
			println("Invalid payment. Minimum payment is: " + round(minPayment));
		}

		double monthInterest = 0;
		double totInterest = 0;
		double newBalance = principal;
		int month = 1;
		interest /= 100;

		String fmt = "%-6s%15s%15s%15s%15s\n";

		System.out.printf(fmt, "Month", "Principal", "Interest", "Payment", "New Balance");

		while (principal >= payment) {
			monthInterest = principal * interest;
			newBalance = principal + monthInterest - payment;
			totInterest += monthInterest;
			System.out.printf(fmt,
					month,
					round(principal),
					round(monthInterest),
					round(payment),
					round(newBalance));
			if (newBalance > principal) {
				println("Interest rate too high to pay off loan.");
				break;
			}
			principal = newBalance;
			month++;
		}
		println(round(totInterest) + " total interest");
	}

	public static String round(double value) {
		double roundedValue = Math.round(value * 100.0) / 100.0;
		return String.format("%.2f", roundedValue);
	}
}
