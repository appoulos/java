import java.awt.*;
// import java.util.*;

public class SandLabMy {
	public static void main(String[] args) {
		SandLabMy lab = new SandLabMy(60, 120);
		lab.run();
	}

	// add constants for particle types here
	public static final int ERASE = 0;
	public static final int METAL = 1;
	public static final int SAND = 2;
	public static final int WATER = 3;
	public static final int WOOD = 4;
	public static final int FIRE = 5;
	public static final int LASER = 6;
	public static final int NUKE = 7;

	// do not add any more fields
	private int[][] grid;
	private SandDisplay display;

	public SandLabMy(int numRows, int numCols) {
		String[] names;
		names = new String[8];
		names[METAL] = "Metal";
		names[SAND] = "Sand";
		names[WATER] = "Water";
		names[WOOD] = "Wood";
		names[FIRE] = "Fire";
		names[LASER] = "Laser";
		names[NUKE] = "NUKE...";
		names[ERASE] = "ERASE";
		grid = new int[numRows][numCols];
		display = new SandDisplay("Falling Sand", numRows, numCols, names);
	}

	// called when the user clicks on a location using the given tool
	private void locationClicked(int row, int col, int tool) {
		switch (tool) {
			case SAND: // sand can only be placed in empty space
				if (getGrid(row, col) == ERASE) {
					setGrid(row, col, tool);
				}
				break;
			case LASER: // make laser 3 wide
				setGrid(row, col, tool);
				// down and left
				setGrid(row, col, 1, 0, tool);
				// up and right
				setGrid(row, col, -1, 0, tool);
				break;
			default:
				setGrid(row, col, tool);
		}
	}

	// copies each element of grid into the display
	public void updateDisplay() {
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				switch (getGrid(row, col)) {
					case METAL:
						display.setColor(row, col, Color.gray);
						break;
					case SAND:
						display.setColor(row, col, Color.yellow);
						break;
					case WATER:
						display.setColor(row, col, Color.blue);
						break;
					case ERASE:
						display.setColor(row, col, Color.black);
						break;
					case LASER:
					case NUKE:
						display.setColor(row, col, Color.red);
						break;
					case WOOD:
						Color anotherColor = new Color(108, 69, 28);
						display.setColor(row, col, anotherColor);
						break;
				}
			}
		}
	}

	// "setGrid(row, col, ERASE);" instead of "grid[row][col] = ERASE;"
	public void setGrid(int row, int col, int element) {
		grid[row][col] = element;
	}

	// "int g = getGrid(row, col);" instead of "int g = grid[row][col];"
	public int getGrid(int row, int col) {
		return grid[row][col];
	}

	// "setGrid(row, col, 1, -1, ERASE);" instead of
	// "grid[row + 1][col - 1] = ERASE;"
	public void setGrid(int row, int col, int drow, int dcol, int element) {
		if (dcol > 0 && col + dcol >= grid[0].length)
			return;
		if (dcol < 0 && col + dcol < 0)
			return;
		if (drow > 0 && row + drow >= grid.length)
			return;
		if (drow < 0 && row + drow < 0)
			return;
		grid[row + drow][col + dcol] = element;
	}

	// "int g = getGrid(row, col, 1, -1);" instead of
	// "int g = grid[row + 1][col - 1];"
	public int getGrid(int row, int col, int drow, int dcol) {
		if (dcol > 0 && col + dcol >= grid[0].length)
			return METAL;
		if (dcol < 0 && col + dcol < 0)
			return METAL;
		if (drow > 0 && row + drow >= grid.length)
			return METAL;
		if (drow < 0 && row + drow < 0)
			return METAL;
		return grid[row + drow][col + dcol];
	}

	// called repeatedly.
	// causes one random particle to maybe do something.
	public void step() {
		int maxRow = grid.length;
		int maxCol = grid[0].length;
		int row = (int) (Math.random() * maxRow);
		int col = (int) (Math.random() * maxCol);

		int g = getGrid(row, col);
		switch (g) {
			case SAND:
				if (row >= maxRow - 1)
					break;
				// sand replaces water
				if (getGrid(row, col, 1, 0) == WATER) {
					setGrid(row, col, 1, 0, g);
					setGrid(row, col, WATER);
					break;
				}
				// sand falls
				if (getGrid(row, col, 1, 0) == ERASE) {
					setGrid(row, col, 1, 0, g);
					setGrid(row, col, ERASE);
					break;
				}
				// make pyrimids in water/erase
				// dx = random number either -1 or 1 for random sideways movement
				int dx = (int) (Math.random() * 2) * 2 - 1;
				// if cell to the side and below are both water or erase,
				// swap the current cell with the diagonal one
				if ((getGrid(row, col, 0, dx) == WATER
						|| getGrid(row, col, 0, dx) == ERASE)
						&& (getGrid(row, col, 1, dx) == WATER
								|| getGrid(row, col, 1, dx) == ERASE)) {
					setGrid(row, col, getGrid(row, col, 1, dx));
					setGrid(row, col, 1, dx, g);
				}
				break;
			case WATER:
				// Bottom row ignore to make it look like still puddles
				if (row >= maxRow - 1)
					break;
				// Falling
				if (getGrid(row, col, 1, 0) == ERASE) {
					setGrid(row, col, 1, 0, g);
					setGrid(row, col, ERASE);
					break;
				}
				// dx = random number either -1 or 1 for random sideways movement
				dx = (int) (Math.random() * 2) * 2 - 1;
				// if sideways movement is available (erase), swap
				if (getGrid(row, col, 0, dx) == ERASE) {
					setGrid(row, col, 0, dx, g);
					setGrid(row, col, ERASE);
				}
				break;
			case WOOD:
				// if (row > maxRow - 1 && col > maxCol - 1) {
				// if (getGrid(row, col, 1, 0) == ERASE) {
				// setGrid(row, col, 1, 0, g);
				// setGrid(row,col, ERASE);
				// break;
				// }
				// }
				break;
			case LASER:
				// Erases past laser
				setGrid(row, col, ERASE);
				// make new laser down and to right
				setGrid(row, col, 1, 1, LASER);
				break;
			case NUKE:
				// make more nukes down and to the right by 4
				for (int i = 0; i < 4; i++) {
					setGrid(row, col, 0, i, ERASE);
					setGrid(row, col, 1, i, NUKE);
				}
				break;
		}
	}

	// do not modify
	public void run() {
		while (true) {
			for (int i = 0; i < display.getSpeed(); i++)
				step();
			updateDisplay();
			display.repaint();
			display.pause(1); // wait for redrawing and for mouse
			int[] mouseLoc = display.getMouseLocation();
			if (mouseLoc != null) // test if mouse clicked
				locationClicked(mouseLoc[0], mouseLoc[1], display.getTool());
		}
	}
}
