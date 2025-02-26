import javax.swing.*;

// import java.awt.*;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Swing {

	public static void main(String[] args) {
		JFrame f = new JFrame();

		BufferedImage tiles = null;
		BufferedImage back = null;
		try {
			tiles = ImageIO.read(new File("tiles.png"));
			back = ImageIO.read(new File("pngegg.png")); // Bicyclebackside.jpg"));
		} catch (IOException e) {
			System.err.println("Error reading image file: " + e.getMessage());
			System.exit(1);
		}

		// Create a JPanel to hold the images
		JPanel imagePanel = new JPanel();
		imagePanel.setLayout(new GridLayout(4, 13)); // Use FlowLayout or other as needed

		ImageIcon imageIcon = null;
		imageIcon = new ImageIcon(back);
		Image image = imageIcon.getImage();
		Image scaledImage = image.getScaledInstance(300, 300, java.awt.Image.SCALE_SMOOTH);
		imageIcon = new ImageIcon(scaledImage);
		imagePanel.add(new JLabel(imageIcon));

		for (int i = 0; i < 51; i++) {
			imageIcon = new ImageIcon(tiles.getSubimage(207 * i, 0, 207, 300));
			// Scale image if it's too big
			image = imageIcon.getImage();
			scaledImage = image.getScaledInstance((i + 1) * 207 / 52, (i + 1) * 300 / 52, java.awt.Image.SCALE_SMOOTH);
			imageIcon = new ImageIcon(scaledImage);
			JLabel label = new JLabel(imageIcon);
			imagePanel.add(label);
			// imagePanel.add(new JLabel(new ImageIcon(tiles.getSubimage(207 * i, 0, 207,
			// 300))));
		}

		// Create the JScrollPane
		JScrollPane scrollPane = new JScrollPane(imagePanel);
		// scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
		// scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		// Add the JScrollPane to the frame
		f.add(scrollPane);

		// private void addImage(JPanel panel, String imagePath) {
		// try {
		// ImageIcon imageIcon = new ImageIcon(imagePath);
		// //Scale image if it's too big
		// Image image = imageIcon.getImage();
		// Image scaledImage = image.getScaledInstance(200, 200,
		// java.awt.Image.SCALE_SMOOTH);
		// imageIcon = new ImageIcon(scaledImage);
		// JLabel label = new JLabel(imageIcon);
		// panel.add(label);
		// } catch (Exception e) {
		// System.err.println("Error loading image: " + imagePath);
		// e.printStackTrace();
		// }
		// }

		// try {
		// BufferedImage originalImage = ImageIO.read(new File("cards/2_of_clubs.png"));
		// Image resizedImage = originalImage.getScaledInstance(-1, height,
		// Image.SCALE_SMOOTH);
		// d = new ImageIcon(resizedImage);
		// } catch (IOException e) {
		// System.err.println("Error reading image file: " + e.getMessage());
		// System.exit(1);
		// }

		// String str = "";
		// // for (enum_ rank : enum_.values())
		// // str += rank + " ";
		// for (String s : ranks)
		// str += s + " ";
		// JButton b1 = new JButton(str);
		// b1.setBounds(90, 100, 180, 40);
		// f.add(b1);
		// JLabel l1 = new JLabel(c);
		// f.add(l1);
		// JLabel l2 = new JLabel(d);
		// f.add(l2);

		f.setSize(400, 400);
		// f.setLayout(new FlowLayout());
		f.setVisible(true);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
}
