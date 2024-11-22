class Bc1 {
	public static void main(String[] args) {
		T1 t = new T1(5);
		System.out.println("t: " + t.x);

	}
}

class T1 {
	public int x;

	T1(int x) {
		this.x = x;
	}
}
