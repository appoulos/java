import javax.swing.*;

import java.awt.FlowLayout;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Swing {
	public static void main(String[] args) {
		int height = 200;
		// int width = 40;
		// creating instance of JFrame
		JFrame f = new JFrame();
		ImageIcon a = new ImageIcon("cards/a.png");
		ImageIcon c = new ImageIcon("cards/2_of_clubs.png");
		ImageIcon d;

		try {
			BufferedImage originalImage = ImageIO.read(new File("cards/2_of_clubs.png"));
			// Resize the image
			Image resizedImage = originalImage.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
			d = new ImageIcon(resizedImage);
		} catch (IOException e) {
			System.err.println("Error reading image file: " + e.getMessage());
			d = a; // new ImageIcon(); // Empty icon in case of error
		}

		JButton b1 = new JButton("Hello, World!", a);
		b1.setBounds(90, 100, 180, 40);
		f.add(b1);
		JLabel l1 = new JLabel(c);
		f.add(l1);
		JLabel l2 = new JLabel(d);
		f.add(l2);

		f.setSize(400, 400);
		f.setLayout(new FlowLayout());
		f.setVisible(true);
	}
}
