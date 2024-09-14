public class MyProgram extends ConsoleProgram {
	public void run() {
		String str;
		boolean on = false;
		System.out.println("Press return to start and stop the stopwatch (q to exit)...");
		long start = 0;

		while (true) {
			str = readLine("");
			if (!on) {
				start = System.nanoTime();
			} else {
				long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
				System.out.printf("%,d Second %03d Millisecond\n",
						elapsedMs / 1_000L,
						elapsedMs % 1000L);
			}
			if (str.equals("q")) {
				System.out.println("Exit.");
				System.exit(0);
			}

			on = !on;
			System.out.println(on ? "Running..." : "Stopped.");
		}
	}
}
