import java.awt.*;
// import java.util.*;

public class SandLab {
	public static void main(String[] args) {
		SandLab lab = new SandLab(60, 120);
		lab.run();
	}

	// add constants for particle types here
	// remember to update elemColors[] in SandDisplay.java
	public static final int ERASE = 0;
	public static final int METAL = 1;
	public static final int SAND = 2;
	public static final int WATER = 3;
	public static final int WOOD = 4;
	public static final int FIRE = 5;
	public static final int SMOKE = 6;
	public static final int LASER = 7;
	public static final int NUKE = 8;

	// apoulos, each pixel contains both the element and color
	class cell {
		int element;
		Color color;

		cell(int element, Color color) {
			this.element = element;
			this.color = color;
		}
	}

	// apoulos, used to erase contents in laser/nuke and initalize the grid
	cell erase = new cell(ERASE, new Color(0, 0, 0));

	// do not add any more fields
	private cell[][] grid;
	private SandDisplay display;

	public SandLab(int numRows, int numCols) {
		String[] names;
		names = new String[9];
		names[METAL] = "Metal";
		names[SAND] = "Sand";
		names[WATER] = "Water";
		names[WOOD] = "Wood";
		names[FIRE] = "Fire";
		names[SMOKE] = "Smoke";
		names[LASER] = "Laser";
		names[NUKE] = "NUKE...";
		names[ERASE] = "ERASE";
		grid = new cell[numRows][numCols];

		// apoulos, Initialize grid to ERASE
		for (int row = 0; row < grid.length; row++)
			for (int col = 0; col < grid[0].length; col++)
				grid[row][col] = erase;

		display = new SandDisplay("Falling Sand", numRows, numCols, names);
	}

	public static void d(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception ignored) {

		}
	}

	// called when the user clicks on a location using the given tool
	private void locationClicked(int row, int col, int tool) {
		switch (tool) {
			case SAND: // Sand can only be placed in empty space
				if (grid[row][col].element == ERASE) {
					// display.getColor() gets the color from the color chooser
					grid[row][col] = new cell(tool, display.getColor());
				}
				break;
			case LASER:
				if (tool == LASER) {
					// make laser 4 tall
					for (int i = 1; i <= 3; i++) {
						if (row + i <= grid.length - 1) {
							grid[row + i][col] = new cell(tool, display.getColor());
						}
					}
				}
				break;
			default:
				grid[row][col] = new cell(tool, display.getColor());
		}
	}

	// copies each element of grid into the display
	public void updateDisplay() {
		for (int row = 0; row < grid.length; row++)
			for (int col = 0; col < grid[0].length; col++)
				display.setColor(row, col, grid[row][col].color);
	}

	// return a one based index based on the probabilities in args
	// for example:
	// chance(1, 3) ->
	// return 1: 25% of the time (1/4)
	// return 2: 75% (3/4)
	public int chance(int... args) {
		int tot = 0;
		for (int num : args) {
			tot += num;
		}
		int rnd = (int) (Math.random() * tot + 1);
		int sum = 0;
		int cnt = 0;
		for (int num : args) {
			cnt++;
			sum += num;
			if (rnd <= sum)
				return cnt;
		}
		return 0;
	}

	// swap two cells
	public void swap(int r1, int c1, int r2, int c2) {
		cell tmp = grid[r1][c1];
		grid[r1][c1] = grid[r2][c2];
		grid[r2][c2] = tmp;
	}

	// called repeatedly.
	// causes one random particle to maybe do something.
	public void step() {
		int maxRow = grid.length;
		int maxCol = grid[0].length;
		int row = (int) (Math.random() * maxRow);
		int col = (int) (Math.random() * maxCol);

		cell g = grid[row][col];
		switch (g.element) {
			case SAND:
				if (row >= maxRow - 1)
					break;

				// sand replaces water
				if (grid[row + 1][col].element == WATER) {
					swap(row, col, row + 1, col);
					break;
				}

				// sand falls
				if (grid[row + 1][col].element == ERASE) {
					swap(row, col, row + 1, col);
					break;
				}

				// make pyrimids in water/erase
				if ((int) (Math.random() * 2) == 0) {
					// down and left
					if (col >= 1 && (grid[row + 1][col - 1].element == WATER
							|| grid[row + 1][col - 1].element == ERASE)) {
						grid[row][col] = grid[row + 1][col - 1];
						grid[row + 1][col - 1] = g;
					}
				} else { // down and right
					if (col < maxCol - 1 && (grid[row + 1][col + 1].element == WATER
							|| grid[row + 1][col + 1].element == ERASE)) {
						grid[row][col] = grid[row + 1][col + 1];
						grid[row + 1][col + 1] = g;
					}
				}
				break;
			case WATER:
				// Bottom row ignore
				if (row >= maxRow - 1 || row <= 0 || col >= maxCol - 1 || col < 1) {
					break;
				}
				// Falling
				if (grid[row + 1][col].element == ERASE) {
					swap(row, col, row + 1, col);
					break;
				}
				// Left or right movement
				if ((int) (Math.random() * 2) == 0) {
					// Left movement
					if (grid[row][col - 1].element == ERASE) {
						swap(row, col, row, col - 1);
					}
				} else {
					// Right movement
					if (grid[row][col + 1].element == ERASE) {
						swap(row, col, row, col + 1);
					}
					break;
				}
				break;
			case WOOD:
				if (row >= maxRow - 1)
					break;

				// wood replaces water
				if (grid[row + 1][col].element == WATER) {
					swap(row, col, row + 1, col);
					break;
				}

				// wood falls
				if (grid[row + 1][col].element == ERASE) {
					swap(row, col, row + 1, col);
					break;
				}
				break;
			case FIRE:
				if (row >= maxRow - 1) {
					break;
				}
				break;
			case SMOKE:
				switch (chance(10, 2, 3, 6)) {
					case 1:
						// don't do anything; slow down smoke
						break;
					case 2:
						// Move smoke downward
						if (row + 1 <= maxRow - 1 // oob check row
								&& grid[row + 1][col].element == ERASE) {
							swap(row, col, row + 1, col);
							// grid[row + 1][col] = g;
							// grid[row][col] = ERASE;
							break;
						}
						break;
					case 3:
						// Move smoke upward
						if (row - 1 >= 0 // oob check row
								&& grid[row - 1][col].element == ERASE) {
							swap(row, col, row - 1, col);
							break;
						}
						break;
					case 4:
						// random left or right movement
						int ran = (int) (Math.random() * 2);
						if (ran == 0) { // attempt to move right
							if (col + 1 <= maxCol - 1 // oob check col
									&& grid[row][col + 1].element == ERASE) {
								swap(row, col, row, col + 1);
							}
						} else { // attempt to move left
							if (col - 1 >= 0 // oob check col
									&& grid[row][col - 1].element == ERASE) {
								swap(row, col, row, col - 1);
							}
						}
						break;
				}
				break;
			case LASER:

				// Move laser down and to the right
				if (row + 1 <= maxRow - 1 && col + 1 <= maxCol - 1) {
					grid[row + 1][col + 1] = grid[row][col];
				}
				// erase current laser
				grid[row][col] = erase;
				break;
			case NUKE:

				// add more nukes on next row
				if (row + 1 <= maxRow - 1) {
					for (int i = 0; i < 4; i++) {
						if (col + i <= maxCol - 1) {
							grid[row + 1][col + i] = grid[row][col];
						}
					}
				}
				// erase current nuke
				grid[row][col] = erase;
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
