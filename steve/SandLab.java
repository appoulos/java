import java.awt.*;
import java.util.*;

public class SandLab {
	public static void main(String[] args) {
		SandLab lab = new SandLab(60, 120);
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

	public SandLab(int numRows, int numCols) {
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
			case SAND: // sand can only be place in empty space
				if (grid[row][col] == ERASE) {
					grid[row][col] = tool;
				}
				break;
			case LASER: // make laser 3 wide
				// down and left
				if (row + 1 < grid.length - 1 && col - 1 >= 0) {
					grid[row + 1][col - 1] = tool;
				}
				// up and right
				if (row - 1 >= 0 && col + 1 <= grid[0].length - 1) {
					grid[row - 1][col + 1] = tool;
				}
				break;
			default:
				grid[row][col] = tool;
		}
	}

	// copies each element of grid into the display
	public void updateDisplay() {
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				if (grid[row][col] == METAL) {
					display.setColor(row, col, Color.gray);
				} else if (grid[row][col] == SAND) {
					display.setColor(row, col, Color.yellow);
				} else if (grid[row][col] == WATER) {
					display.setColor(row, col, Color.blue);
				} else if (grid[row][col] == ERASE) {
					display.setColor(row, col, Color.black);
				} else if (grid[row][col] == LASER) {
					display.setColor(row, col, Color.red);
				} else if (grid[row][col] == NUKE) {
					display.setColor(row, col, Color.red);
				} else if (grid[row][col] == WOOD) {
					// Color anotherColor = new Color(74, 55, 40);
					Color anotherColor = new Color(108, 69, 28);
					display.setColor(row, col, anotherColor);
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

		int g = grid[row][col];
		switch (g) {
			case SAND:
				if (row >= maxRow - 1)
					break;
				// sand replaces water
				if (grid[row + 1][col] == WATER) {
					grid[row + 1][col] = g;
					grid[row][col] = WATER;
					break;
				}
				// sand falls
				if (grid[row + 1][col] == ERASE) {
					grid[row + 1][col] = g;
					grid[row][col] = ERASE;
					break;
				}
				// make pyrimids in water/erase
				// dx = random number either -1 or 1
				int dx = (int) (Math.random() * 2) * 2 - 1;
				if ((dx == -1 && col >= 1 || dx == 1 && col < maxCol - 1)
						&& (grid[row][col + dx] == WATER
								|| grid[row][col + dx] == ERASE)
						&& (grid[row + 1][col + dx] == WATER
								|| grid[row + 1][col + dx] == ERASE)) {
					grid[row][col] = grid[row + 1][col + dx];
					grid[row + 1][col + dx] = g;
				}
				break;
			case WATER:
				// Bottom row ignore
				if (row >= maxRow - 1)
					break;
				// Falling
				if (grid[row + 1][col] == ERASE) {
					grid[row + 1][col] = g;
					grid[row][col] = ERASE;
					break;
				}
				// Left or right movement
				if ((int) (Math.random() * 2) == 0) {
					// Left movement
					if (col >= 1 && grid[row][col - 1] == ERASE) {
						grid[row][col - 1] = g;
						grid[row][col] = ERASE;
					}
				} else {
					// Right movement
					if (col < maxCol - 1 && grid[row][col + 1] == ERASE) {
						grid[row][col + 1] = g;
						grid[row][col] = ERASE;
					}
				}
				break;
			case WOOD:
				if (row > maxRow - 1 && col > maxCol - 1) {
					if (grid[row + 1][col] == ERASE) {
						grid[row + 1][col] = g;
						grid[row][col] = ERASE;
						break;
					}
				}
				break;
			case LASER:
				// Erases past laser
				if (row == maxRow - 1 || col == maxCol - 1) {
					grid[row][col] = ERASE;
				}
				// Move laser down and to the right
				if (row + 1 <= maxRow - 1 && col + 1 <= maxCol - 1) {
					grid[row][col] = ERASE;
					grid[row + 1][col + 1] = LASER;
				}
				break;
			case NUKE:
				// Erases laser on an edge
				if (row == maxRow - 1 || col == maxCol - 1) {
					grid[row][col] = ERASE;
				}
				if (row + 1 <= maxRow - 1 && col + 1 <= maxCol - 1) {
					grid[row][col] = ERASE;
					grid[row + 1][col + 1] = NUKE;
				}
				if (row + 2 <= maxRow - 1 && col - 1 >= 0 && col <= maxCol - 1) {
					grid[row + 1][col - 1] = ERASE;
					grid[row + 2][col] = NUKE;
				}
				if (row <= maxRow - 1 && row - 1 >= 0 && col + 2 <= maxCol - 1) {
					grid[row - 1][col + 1] = ERASE;
					grid[row][col + 2] = NUKE;
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
