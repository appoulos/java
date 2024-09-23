public class SPayments extends ConsoleProgram {
	String round(double d) {
		return String.format("%.2f", Math.round(d * 100.0) / 100.0);
	}

	public void run() {
		double principal = 10000; // readDouble("What is the Principle: $");
		double interest = 1.0f; // readDouble("What is the monthly interest rate: ");
		double payment = 300; // readDouble("How much do you pay monthly: $");
		double totInterest = 0;
		double monthInterest = 0;
		double newBalance = principal;
		String lineFormat = "%-6s%12s%12s%12s%16s\n";

		System.out.printf(lineFormat, "Month", "Principal", "Interest", "Payment", "New Balance");
		int month = 1;
		interest /= 100;
		do {
			monthInterest = principal * interest;
			newBalance = principal + monthInterest - payment;
			totInterest += monthInterest;
			System.out.printf(lineFormat,
					month,
					round(principal),
					round(monthInterest),
					round(payment),
					round(newBalance));
			principal = newBalance;
			month++;
		} while (newBalance >= payment);
		println();
		System.out.println(round(totInterest) + " total interest");
	}
}
