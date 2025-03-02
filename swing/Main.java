import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

class Main extends JFrame {

	private static JPanel panel;
	private static JButton addButton;
	private static JButton removeButton;
	private static JButton revalidateButton;
	private static JButton validateButton;
	private static JButton repaintButton;
	private static JButton quitButton;
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 600;
	private static List<JComponent> buttons;

	public Main() {
		buttons = new ArrayList<>();
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		panel = new JPanel();

		addButton = new JButton("add");
		// addButton.setEnabled(false);
		addButton.addActionListener(event -> {
			JButton newButton = new JButton("hi" + buttons.size());
			buttons.add(newButton);
			panel.add(newButton);
			// panel.revalidate();
			// panel.repaint();
		});

		removeButton = new JButton("remove");
		removeButton.addActionListener(event -> {
			if (buttons.size() > 0) {
				panel.remove(buttons.remove(0));
				// panel.revalidate();
				// panel.repaint();
			}
		});

		repaintButton = new JButton("repaint");
		repaintButton.addActionListener(event -> panel.repaint());

		validateButton = new JButton("validate");
		validateButton.addActionListener(event -> validate());

		revalidateButton = new JButton("revalidate");
		revalidateButton.addActionListener(event -> panel.revalidate());

		quitButton = new JButton("quit");
		quitButton.addActionListener(event -> System.exit(0));

		panel.add(addButton);
		panel.add(removeButton);
		panel.add(repaintButton);
		panel.add(revalidateButton);
		panel.add(validateButton);
		panel.add(quitButton);

		add(panel);
	}

	public static void main(String[] args) {
		System.out.println("hi");
		EventQueue.invokeLater(() -> {
			JFrame main = new Main();
			main.setTitle("title1");
			main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			main.setVisible(true);
		});
	}
}
