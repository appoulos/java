import javax.swing.*;

import java.awt.FlowLayout;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Swing {
	enum enum_ {
		ace, two, _3, king
	};

	static String[] ranks = { "ace", "2", "3", "jack" };

	public static void main(String[] args) {
		int height = 200;
		// int width = 40;
		// creating instance of JFrame
		JFrame f = new JFrame();
		// ImageIcon a = new ImageIcon("cards/a.png");
		ImageIcon c = new ImageIcon("cards/2_of_clubs.png");
		ImageIcon d = null;
		ImageIcon tile = null;
		BufferedImage tiles = null;
		try {
			tiles = ImageIO.read(new File("tiles.png"));
		} catch (IOException e) {
			System.err.println("Error reading image file: " + e.getMessage());
			System.exit(1);
		}
		for (int i = 0; i < 52; i++) {
			tile = new ImageIcon(tiles.getSubimage(207 * i, 0, 207, 300));
			f.add(new JLabel(tile));
		}
		try {
			BufferedImage originalImage = ImageIO.read(new File("cards/2_of_clubs.png"));
			Image resizedImage = originalImage.getScaledInstance(-1, height, Image.SCALE_SMOOTH);
			d = new ImageIcon(resizedImage);
		} catch (IOException e) {
			System.err.println("Error reading image file: " + e.getMessage());
			System.exit(1);
		}

		String str = "";
		// for (enum_ rank : enum_.values())
		// str += rank + " ";
		for (String s : ranks)
			str += s + " ";
		JButton b1 = new JButton(str);
		b1.setBounds(90, 100, 180, 40);
		f.add(b1);
		JLabel l1 = new JLabel(c);
		f.add(l1);
		JLabel l2 = new JLabel(d);
		f.add(l2);

		f.setSize(400, 400);
		f.setLayout(new FlowLayout());
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
