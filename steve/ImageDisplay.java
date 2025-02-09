
// this doesn't work in codehs.com
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageDisplay extends JFrame {

	private ImageIcon imageIcon;
	private JLabel imageLabel;

	public ImageDisplay(String imagePath, int width, int height) {
		try {
			BufferedImage originalImage = ImageIO.read(new File(imagePath));
			// Resize the image
			Image resizedImage = originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
			imageIcon = new ImageIcon(resizedImage);
		} catch (IOException e) {
			System.err.println("Error reading image file: " + e.getMessage());
			imageIcon = new ImageIcon(); // Empty icon in case of error
		}

		imageLabel = new JLabel(imageIcon);
		add(imageLabel);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public static void main(String[] args) {
		// Replace "path/to/your/image.png" with the actual path to your PNG file.
		// Adjust the width and height as needed.
		SwingUtilities.invokeLater(() -> new ImageDisplay("cards/2_of_diamonds.png", 400, 300));
	}
}
