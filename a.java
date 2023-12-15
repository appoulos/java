import java.util.Scanner;
import java.util.Random;
public class a {
    public static void main(String args[]) {
        Random rn = new Random();
        for (int i=0; i<25; i++) {
            System.out.println(rn.nextInt(2));
        }
        Scanner s = new Scanner(System.in);
        System.out.print("Enter info:");
        //String a = s.nextLine();
        String a = "hello how are you";
        for (int i=0;i<a.length();i++) {
            System.out.print(a.charAt(i));
            try{Thread.sleep(50);}catch(Exception e){};
        }

    }
}


