import java.util.Arrays;
public class Java {
    public static void main(String args[]){
        System.out.println("asdf: " + 1234);
        String s = Integer.toString(1234);
        System.out.println("s: " + s);
        // String[] s2 = s.split("");
        int num=1357;
        int[] ia;
        String[] s2 = Integer.toString(num).split("");
        for (int i=0; i<s2.length; i++) {
            System.out.println(s2[i]);
            //ia.add(Integer.parseInt(s2[i]));
        }
        //Integer[] i2 = Integer.toString(num).split("");
        int[] numbers = Arrays.stream(s.split("")).mapToInt(Integer::parseInt).toArray();
        System.out.println(Arrays.toString(numbers));
        for (int i=0; i<numbers.length; i++) {
            System.out.println(numbers[i]);
        }
        //System.out.println(Arrays.toString(Integer.toString(1234).split("")));//.split(""));
    }
}
