import javax.swing.*;

public class HelloWorldSwing {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Hello, World (Swing)");
        JLabel label = new JLabel("Hello, World!");
        frame.add(label);
        frame.setSize(300, 100);
        frame.setVisible(true);
    }
}

