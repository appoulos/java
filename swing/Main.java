import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

class Main extends JFrame {

	private static JPanel menuPanel;
	private static JPanel centerPanel;
	private static JPanel specialPanel;
	private static JButton addButton;
	private static JButton removeButton;
	private static JButton specialButton;
	private static JButton quitButton;
	private static final int DEFAULT_WIDTH = 400;
	private static final int DEFAULT_HEIGHT = 600;
	private static List<JComponent> buttons;
	private static boolean alt = true;

	public Main() {
		buttons = new ArrayList<>();
		setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

		menuPanel = new JPanel();
		centerPanel = new JPanel();

		addButton = new JButton("add");
		addButton.addActionListener(event -> {
			JButton newButton = new JButton("<html><u>h</u>i" + buttons.size() + "</html>");
			buttons.add(newButton);
			centerPanel.add(newButton); // , BorderLayout.SOUTH);
			centerPanel.revalidate();
			centerPanel.repaint();
			// repaint();
		});

		specialPanel = new JPanel();
		specialPanel.add(new JButton("specialbutton"));
		// add(specialPanel, BorderLayout.SOUTH);
		// add(specialPanel, BorderLayout.EAST);
		specialButton = new JButton("special");
		specialButton.addActionListener(event -> {
			if (alt) {
				remove(centerPanel);
				add(specialPanel, BorderLayout.CENTER);
				specialPanel.revalidate();
				specialPanel.repaint();
			} else {
				remove(specialPanel);
				add(centerPanel, BorderLayout.CENTER);
				centerPanel.revalidate();
				centerPanel.repaint();
			}
			alt = !alt;
			// revalidate();
			// repaint();
		});

		removeButton = new JButton("remove");
		removeButton.addActionListener(event -> {
			if (buttons.size() > 0) {
				centerPanel.remove(buttons.remove(0));
				centerPanel.revalidate();
				centerPanel.repaint();
				// repaint();
			}
		});

		quitButton = new JButton("quit");
		quitButton.addActionListener(event -> System.exit(0));

		menuPanel.add(addButton);
		menuPanel.add(removeButton);
		menuPanel.add(specialButton);
		menuPanel.add(quitButton);

		add(menuPanel, BorderLayout.NORTH);
		add(centerPanel, BorderLayout.CENTER);
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
