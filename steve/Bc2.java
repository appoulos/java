class Bc2 {
	public static void main(String[] args) {
		T2 a = null;
		T2 t = new T2(5);
		System.out.println("t: " + t.x);
		if (a != null)
			System.out.println("a: " + a.x);

	}
}

class T2 {
	public int x;

	T2(int nx) {
		x = nx;
	}
}
