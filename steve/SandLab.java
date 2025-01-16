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
	public static final int SMOKE = 6;
	public static final int LASER = 7;
	public static final int NUKE = 8;

	// do not add any more fields
	private int[][] grid;
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
		grid = new int[numRows][numCols];
		display = new SandDisplay("Falling Sand", numRows, numCols, names);
	}

	public static void d(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception ignored) {

		}
	}

	public void setGrid(int row, int col, int drow, int dcol, int element) {
		if (dcol > 0 && col + dcol >= grid[0].length) {
			return;
		}
		if (dcol < 0 && col + dcol < 0) {
			return;
		}
		if (drow > 0 && row + drow >= grid.length) {
			return;
		}
		if (drow < 0 && row + drow < 0) {
			return;
		}
		grid[row + drow][col + dcol] = element;
	}

	// called when the user clicks on a location using the given tool
	private void locationClicked(int row, int col, int tool) {
		switch (tool) {
			case SAND: // Sand can only be placed in empty space
				if (grid[row][col] == ERASE) {
					grid[row][col] = tool;
				}
				break;
			case LASER:
				if (tool == LASER) {
					// make laser 4 tall
					for (int i = 1; i <= 3; i++) {
						if (row + i <= grid.length - 1) {
							grid[row + i][col] = tool;
						}
					}
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
				if (grid[row][col] == ERASE) {
					Color anotherColor = new Color(0, 0, 0);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == METAL) {
					Color anotherColor = new Color(128, 128, 128);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == SAND) {
					Color anotherColor = new Color(250, 220, 50);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == WATER) {
					Color anotherColor = new Color(46, 213, 255);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == WOOD) {
					int randomNum = (int) (Math.random() * 2);
					if (randomNum == 0) {
						Color anotherColor1 = new Color(74, 55, 40);
						display.setColor(row, col, anotherColor1);
					} else {
						Color anotherColor2 = new Color(108, 69, 28);
						display.setColor(row, col, anotherColor2);
					}
				} else if (grid[row][col] == FIRE) {
					Color anotherColor = new Color(255, 64, 0);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == SMOKE) {
					Color anotherColor = new Color(255, 255, 255);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == LASER) {
					Color anotherColor = new Color(255, 87, 51);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == NUKE) {
					Color anotherColor = new Color(255, 87, 51);
					display.setColor(row, col, anotherColor);
				}
			}
		}
	}

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
				if ((int) (Math.random() * 2) == 0) {
					// down and left
					if (col >= 1 && (grid[row + 1][col - 1] == WATER
							|| grid[row + 1][col - 1] == ERASE)) {
						grid[row][col] = grid[row + 1][col - 1];
						grid[row + 1][col - 1] = g;
					}
				} else { // down and right
					if (col < maxCol - 1 && (grid[row + 1][col + 1] == WATER
							|| grid[row + 1][col + 1] == ERASE)) {
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
				if (grid[row + 1][col] == ERASE) {
					grid[row + 1][col] = g;
					grid[row][col] = ERASE;
					break;
				}
				// Left or right movement
				if ((int) (Math.random() * 2) == 0) {
					// Left movement
					if (grid[row][col - 1] == ERASE) {
						grid[row][col - 1] = g;
						grid[row][col] = ERASE;
					}
				} else {
					// Right movement
					if (grid[row][col + 1] == ERASE) {
						grid[row][col + 1] = g;
						grid[row][col] = ERASE;
					}
					break;
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
								&& grid[row + 1][col] == ERASE) {
							grid[row + 1][col] = g;
							grid[row][col] = ERASE;
							break;
						}
						break;
					case 3:
						// Move smoke upward
						if (row - 1 >= 0 // oob check row
								&& grid[row - 1][col] == ERASE) {
							grid[row - 1][col] = g;
							grid[row][col] = ERASE;
							break;
						}
						break;
					case 4:
						// random left or right movement
						int ran = (int) (Math.random() * 2);
						if (ran == 0) { // attempt to move right
							if (col + 1 <= maxCol - 1 // oob check col
									&& grid[row][col + 1] == ERASE) {
								grid[row][col + 1] = g;
								grid[row][col] = ERASE;
							}
						} else { // attempt to move left
							if (col - 1 >= 0 // oob check col
									&& grid[row][col - 1] == ERASE) {
								grid[row][col - 1] = g;
								grid[row][col] = ERASE;
							}
						}
						break;
				}

				// // slow smoke down
				// int ran = (int) (Math.random() * 50);
				// if (ran < 48)
				// break;
				//
				// // Move smoke upward
				// ran = (int) (Math.random() * 4);
				// if (ran > 0) {
				// if (row - 1 >= 0 // oob check row
				// && grid[row - 1][col] == ERASE) {
				// grid[row - 1][col] = g;
				// grid[row][col] = ERASE;
				// break;
				// }
				// }
				//
				// // random left or right movement
				// ran = (int) (Math.random() * 2);
				// if (ran == 0) { // attempt to move right
				// if (col + 1 <= maxCol - 1 // oob check col
				// && grid[row][col + 1] == ERASE) {
				// grid[row][col + 1] = g;
				// grid[row][col] = ERASE;
				// }
				// } else { // attempt to move left
				// if (col - 1 >= 0 // oob check col
				// && grid[row][col - 1] == ERASE) {
				// grid[row][col - 1] = g;
				// grid[row][col] = ERASE;
				// }
				// }
				break;
			case LASER:
				// erase current laser
				grid[row][col] = ERASE;

				// Move laser down and to the right
				if (row + 1 <= maxRow - 1 && col + 1 <= maxCol - 1) {
					grid[row + 1][col + 1] = LASER;
				}
				break;
			case NUKE:
				// erase current nuke
				grid[row][col] = ERASE;

				// add more nukes on next row
				if (row + 1 <= maxRow - 1) {
					for (int i = 0; i < 4; i++) {
						if (col + i <= maxCol - 1) {
							grid[row + 1][col + i] = NUKE;
						}
					}
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
