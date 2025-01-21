import java.awt.*;
// import java.util.*;

public class SandLabMy {
	public static void main(String[] args) {
		SandLabMy lab = new SandLabMy(60, 120);
		lab.run();
	}

	// apoulos, each pixel contains both the element and color
	class cell {
		int element;
		Color color;
		boolean burning;
		int fuel;

		cell(int element, Color color) {
			this.element = element;
			this.color = color;
			this.burning = false;
			this.fuel = 1000;
		}
	}

	// apoulos, used to erase contents in laser/nuke and initalize the grid
	cell erase = new cell(ERASE, new Color(0, 0, 0));

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
	public static final int NUM_ELEMENTS = 9;

	// do not add any more fields
	private cell[][] grid;
	private SandDisplayMy display;

	public SandLabMy(int numRows, int numCols) {
		String[] names;
		Color[] colors;
		colors = new Color[NUM_ELEMENTS];
		names = new String[NUM_ELEMENTS];
		names[METAL] = "Metal";
		colors[METAL] = new Color(128, 128, 128);
		names[SAND] = "Sand";
		colors[SAND] = new Color(250, 220, 50);
		names[WATER] = "Water";
		colors[WATER] = new Color(46, 213, 255);
		names[WOOD] = "Wood";
		colors[WOOD] = new Color(108, 69, 28);
		names[FIRE] = "Fire";
		colors[FIRE] = new Color(255, 64, 0);
		names[SMOKE] = "Smoke";
		colors[SMOKE] = new Color(255, 255, 255);
		names[LASER] = "Laser";
		colors[LASER] = new Color(255, 87, 51);
		names[NUKE] = "NUKE...";
		colors[NUKE] = new Color(255, 87, 51);
		names[ERASE] = "ERASE";
		colors[ERASE] = Color.black;
		if (names.length != NUM_ELEMENTS) {
			System.out.println("Error: names.length must be equal to " + NUM_ELEMENTS);
			System.exit(1);
		}

		display = new SandDisplayMy("Falling Sand", numRows, numCols, names, colors);
		if (display.elemColors.length != NUM_ELEMENTS) {
			System.out.println("Error: display.elemColors.length must be equal to " + NUM_ELEMENTS);
			System.exit(1);
		}
		grid = new cell[numRows][numCols];

		// apoulos, Initialize grid to ERASE
		for (int row = 0; row < grid.length; row++)
			for (int col = 0; col < grid[0].length; col++)
				grid[row][col] = erase;
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
			case FIRE:
				cell curr = grid[row][col];
				if (curr.element == WOOD) {
					if (row - 1 >= 0 && grid[row - 1][col].element == ERASE) {
						if (curr.fuel > 0) {
							curr.burning = true;
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
	// chance(25, 75) ->
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
			case FIRE:
				cell curr = grid[row][col];
				if (curr.element == WOOD) {
					if (row - 1 >= 0 && grid[row - 1][col].element == ERASE) {
						if (curr.fuel > 0) {
							curr.burning = true;
						}
					}
				}
			case WATER:
				// Falling
				if (row + 1 <= maxRow - 1 && grid[row + 1][col].element == ERASE) {
					swap(row, col, row + 1, col);
					break;
				}
				// Left or right movement
				if ((int) (Math.random() * 2) == 0) {
					// Left movement
					if (col - 1 >= 0 && grid[row][col - 1].element == ERASE) {
						swap(row, col, row, col - 1);
					}
				} else {
					// Right movement
					if (col + 1 <= maxCol - 1 && grid[row][col + 1].element == ERASE) {
						swap(row, col, row, col + 1);
					}
					break;
				}
				break;
			case WOOD:
				if (row + 1 <= maxRow - 1) {
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
				}

				// on fire
				if (grid[row][col].burning) {
					switch (chance(1, 1)) {
						case 1:
							if (col - 1 >= 0 && grid[row][col - 1].element == WOOD) {
								if (row - 1 >= 0 && grid[row - 1][col
										- 1].element == ERASE) {
									grid[row][col - 1].burning = true;
								}
							} else if (row - 1 >= 0 && col - 2 >= 0
									&& grid[row - 1][col - 2].element == WOOD) {
								if (row - 2 >= 0 && grid[row - 2][col
										- 2].element == ERASE) {
									grid[row - 1][col - 2].burning = true;
								}
							}
							break;
						case 2:
							if (col + 1 <= maxCol - 1
									&& grid[row][col + 1].element == WOOD) {
								if (row - 1 >= 0 && grid[row - 1][col
										+ 1].element == ERASE) {
									grid[row][col + 1].burning = true;
								}
							} else if (row - 1 >= 0 && col + 1 <= maxCol - 1
									&& grid[row - 1][col + 1].element == WOOD) {
								if (row - 2 >= 0 && grid[row - 2][col
										+ 1].element == ERASE) {
									grid[row - 1][col + 1].burning = true;
								}
							}
							break;
					}
					grid[row][col].fuel -= 10;
					if (grid[row][col].fuel <= 0) { // start next cell down on fire if wood
						if (row + 1 <= maxRow - 1 && grid[row + 1][col].element == WOOD) {
							grid[row + 1][col].burning = true;
						}
						grid[row][col] = erase;
					} else {
						if (row - 1 >= 0 && grid[row - 1][col].element == ERASE) {
							grid[row - 1][col] = new cell(SMOKE, display.elemColors[SMOKE]);
						}
					}
				}
				break;
			case SMOKE:
				if (grid[row][col].fuel <= 0)
					grid[row][col] = erase;
				grid[row][col].fuel--;
				switch (chance(20, 30, 50)) {
					case 1:
						// Move smoke downward
						if (row + 1 <= maxRow - 1 // oob check row
								&& grid[row + 1][col].element == ERASE) {
							swap(row, col, row + 1, col);
							// grid[row + 1][col] = g;
							// grid[row][col] = ERASE;
							break;
						}
						break;
					case 2:
						// Move smoke upward
						if (row - 1 >= 0 // oob check row
								&& grid[row - 1][col].element == ERASE) {
							swap(row, col, row - 1, col);
							break;
						}
						break;
					case 3:
						// random left or right movement
						if ((int) (Math.random() * 2) == 0) { // attempt to move right
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
