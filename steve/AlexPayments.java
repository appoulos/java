public class AlexPayments extends ConsoleProgram {
	public void run() {
		double p = 10000; // readDouble("What is the Principle: $");
		double ir = 1.0f; // readDouble("What is the monthly interest rate: ");
		double mp = 300; // readDouble("How much do you pay monthly: $");
		double i = 0;
		double nb = 0;
		double itotal = 0;
		String fmt = "%-6s%12s%12s%12s%12s\n";

		System.out.printf(fmt, "Month", "Principal", "Interest", "Payment", "New Balance");
		System.out.printf(fmt, "-----", "---------", "--------", "-------", "-----------");
		int month = 1;
		ir /= 100;
		do {
			i = p * ir;
			nb = p + i - mp;
			itotal += i;
			System.out.printf(fmt,
					String.format("%02d", month) + ".",
					"$" + String.format("%.2f", p),
					String.format("%.2f", i) + "%",
					"$" + String.format("%.2f", mp),
					"$" + String.format("%.2f", nb));
			p = nb;
			month++;
		} while (nb >= mp);
		System.out.println();
		System.out.printf("%s%.2f\n", "Total interest paid: $", itotal);
	}
}
