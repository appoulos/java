import java.util.ArrayList;
// Filename: MainClass.java
class HelperClass {
    void helperMethod() {
        System.out.println("Helper class method");
    }
}

public class MultiClassExample {
    public static void main(String[] args) {
        System.out.println("Main class method");
        HelperClass helper = new HelperClass();
        helper.helperMethod();
    }
}

class AnotherClass {
    void anotherMethod() {
        System.out.println("Another class method");
    }
}
