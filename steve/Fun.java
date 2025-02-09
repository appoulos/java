class Fun {
	// private int a;

	public Fun() {
		// a = 7;
	}

	public Fun chain() {
		// a++;
		return this;
	}

	public static int add(int a) {
		return a + 1;
	}

	public String toString() {
		return "empty"; // "a: " + a;
	}

	public static void main(String[] args) {
		Fun g = new Fun();
		System.out.println(g.chain().chain().chain());
		System.out.println(g.chain().chain().chain().chain().chain());
		System.out.println(add(add(add(8))));
		// System.out.println(add(add(add(g.a))));
	}
}
