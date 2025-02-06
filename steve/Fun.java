class Fun {
	private int a;

	public Fun() {
		a = 7;
	}

	public Fun chain() {
		a++;
		return this;
	}

	public static int add(int a) {
		return a + 1;
	}

	public String toString() {
		return "a: " + a;
	}

	public static void main(String[] args) {
		Fun g = new Fun();
		System.out.println(g.chain().chain().chain());
		System.out.println(g.chain().chain().chain());
		System.out.println(add(add(add(g.a))));
		System.out.println(add(add(add(g.a))));
	}
}
