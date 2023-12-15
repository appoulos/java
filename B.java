class Coord {
    public int x;
    public int y;
    Coord(int a, int b) {
      x=a;
      y=b;
    }
}
int a() {
  return 5;
}
void main() {
  int b = a();
  Coord c = new Coord(3,4);
  // c.x = 2;
  // c.y = 3;

  System.out.println("hello world\n.{b}"+b+c.y);
}
