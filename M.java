//import java.util.Scanner; // import the Scanner class

class Coord {
    public int x;
    public int y;
}

class Board {
    int maxRow = 10;
    int maxCol = 10;

    int getMaxRow() {
        return maxRow;
    }

    int getMaxCol() {
        return maxCol;
    }

    Coord[maxRow][maxCol]b;
}

class Main {
    static void msg(String s) {
        for (int i = 0; i < s.length(); i++) {
            System.out.print(s.charAt(i));
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            ;
        }
        System.out.println();
    }

    public static void main(String[] args) {
        msg("hello");
        Board b = new Board;
        for int i = 0;
        i < b.getMaxCol();
        i++;
        {

        }
        //     System.out.println("hi");
        //     if ( 1 == 1 ) ;
        //     int z;
        //     int j=1;
        //     System.out.print(j + ", ");
        //     while (j > 0) {
        //         //System.out.print(j + ", ");
        //         j++;
        //     }
        //     System.out.print(j + ", ");
        //     try {
        //         float c=1/0;
        //     } catch (ArithmeticException e) {
        //         System.out.println(e.getMessage());
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     Scanner scan = new Scanner(System.in);
        //     String userName;
        //
        //     // Enter username and press Enter
        //     System.out.print("Enter username: ");
        //     userName = scan.nextLine();
        //     System.out.print("Enter two integers: ");
        //     int a = scan.nextInt();
        //     int b = scan.nextInt();
        // msg(userName);
        // msg(String.valueOf(a));
        // msg(String.valueOf(b));

        //System.out.println("Username is: " + userName);        
    }
}
