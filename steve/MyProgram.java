import java.util.Random;

public class MyProgram {
	static int choice(int i) {
		return (int) (Math.random() * i) + 1;
	}

	public static void main(String[] args) {

		Random random = new Random();
		random.setSeed(12345L);
		for (int i = 0; i < 9; ++i) {
			System.out.println("Random Integer Number: " + Math.abs(random.nextInt()) % 3);
		}
		System.out.println("Random Long Number: " + random.nextLong());
		int choice = 0;
		boolean iceSkater = false;
		boolean shovel = false;
		boolean zamboni = false;
		for (int i = 0; i < 9; ++i) {
			System.out.println(choice(3) - 1);
			continue;
			// iceSkater = false;
			// shovel = false;
			// zamboni = false;
			// choice = choice(3);
			// if (choice == 1) {
			// iceSkater = true;
			// } else if (choice == 2) {
			// shovel = true;
			// } else {
			// zamboni = true;
			// }
			// System.out.println("iceSkater: " + iceSkater);
			// System.out.println("shovel: " + shovel);
			// System.out.println("zamboni: " + zamboni);
			// System.out.println();
		}
	}
}
