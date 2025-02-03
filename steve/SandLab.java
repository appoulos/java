import java.awt.*;
// import java.util.*;

public class SandLab {
	public static void main(String[] args) {
		SandLab lab = new SandLab(/* 125, 259. */ 60, 120);
		lab.run();
	}

	// add constants for particle types here
	public static final int ERASE = 0;
	public static final int METAL = 1;
	public static final int SAND = 2;
	public static final int WATER = 3;
	public static final int WOOD = 4;
	public static final int FIRE = 5;
	public static final int LAVA = 6;
	public static final int OBSIDIAN = 7;
	public static final int SMOKE = 8;
	public static final int STEAM = 9;
	public static final int LASER = 10;
	public static final int NUKE = 11;
	public static final int ERASE_ALL = 12;

	// do not add any more fields
	private int[][] grid;
	private SandDisplay display;

	public SandLab(int numRows, int numCols) {
		String[] names;
		names = new String[13];
		names[METAL] = "Metal";
		names[SAND] = "Sand";
		names[WATER] = "Water";
		names[WOOD] = "Wood";
		names[FIRE] = "Fire";
		names[LAVA] = "Lava";
		names[OBSIDIAN] = "Obsidian";
		names[SMOKE] = "Smoke";
		names[STEAM] = "Steam";
		names[LASER] = "Laser";
		names[NUKE] = "NUKE...";
		names[ERASE] = "ERASE";
		names[ERASE_ALL] = "ERASE ALL";
		grid = new int[numRows][numCols];
		display = new SandDisplay("Falling Sand", numRows, numCols, names);
	}

	public static void d(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception ignored) {

		}
	}

	public void setGrid(int row, int col, int element) {
		grid[row][col] = element;
	}

	public int getGrid(int row, int col) {
		return grid[row][col];
	}

	public int getGrid(int row, int col, int drow, int dcol) {
		if (dcol > 0 && col + dcol >= grid[0].length) {
			return METAL;
		}
		if (dcol < 0 && col + dcol < 0) {
			return METAL;
		}
		if (drow > 0 && row + drow >= grid.length) {
			return METAL;
		}
		if (drow < 0 && row + drow < 0) {
			return METAL;
		}
		return grid[row + drow][col + dcol];
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
		if (grid[row][col] == ERASE || tool == ERASE || tool == LASER) {
			grid[row][col] = tool;
		}
		switch (tool) {
			case SAND: // Sand can only be placed in empty space
				if (grid[row][col] == ERASE) {
					grid[row][col] = tool;
				}
				break;
			case LASER:
				// make laser 3 wide
				if (tool == LASER) {
					// down and left
					if (row + 1 < grid.length - 1 && col - 1 >= 0) {
						grid[row + 1][col - 1] = tool;
					}
					// up and right
					if (row - 1 >= 0 && col + 1 <= grid[0].length - 1) {
						grid[row - 1][col + 1] = tool;
					}
				}
				break;
			case FIRE:
				if (tool == FIRE && grid[row][col] == WOOD) {
					grid[row][col] = tool;
				}
				break;
			// case WOOD:
			// //Make wood brown or a lighter brown
			// int randomNum = (int)(Math.random() * 2);
			// if (randomNum == 0) {
			// Color anotherColor1 = new Color(74, 55, 40);
			// display.setColor(row, col, anotherColor1);
			// } else {
			// Color anotherColor2 = new Color(108, 69, 28);
			// display.setColor(row, col, anotherColor2);
			// }
			default:
				if (grid[row][col] == ERASE) {
					grid[row][col] = tool;
				}
		}
	}

	// copies each element of grid into the display
	public void updateDisplay() {
		for (int row = 0; row < grid.length; row++) {
			for (int col = 0; col < grid[0].length; col++) {
				if (grid[row][col] == ERASE || grid[row][col] == ERASE_ALL) {
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
					Color anotherColor2 = new Color(108, 69, 28);
					display.setColor(row, col, anotherColor2);
				} else if (grid[row][col] == FIRE) {
					Color anotherColor = new Color(255, 64, 0);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == LAVA) {
					Color anotherColor = new Color(207, 16, 32);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == OBSIDIAN) {
					Color anotherColor = new Color(58, 33, 82);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == SMOKE) {
					int randomNum = (int) (Math.random() * 8);
					if (randomNum > 0) {
						Color anotherColor1 = new Color(255, 255, 255);
						display.setColor(row, col, anotherColor1);
					} else {
						Color anotherColor2 = new Color(211, 211, 211);
						display.setColor(row, col, anotherColor2);
					}
				} else if (grid[row][col] == STEAM) {
					Color anotherColor = new Color(199, 213, 244);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == LASER) {
					Color anotherColor = new Color(255, 87, 51);
					display.setColor(row, col, anotherColor);
				} else if (grid[row][col] == NUKE) {
					Color anotherColor = new Color(255, 87, 51);
					display.setColor(row, col, anotherColor);
				}
				if (display.getTool() == ERASE_ALL) {
					for (int r = 0; r < grid.length; r++) {
						for (int c = 0; c < grid[0].length; c++) {
							grid[r][c] = ERASE;
						}
					}
				}
			}
		}
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
				// // old water

				// // Bottom row ignore
				// if (row >= maxRow - 1 || row <= 0 || col >= maxCol - 1 || col < 1) {
				// break;
				// }
				// // Falling
				// if (grid[row + 1][col] == ERASE) {
				// grid[row + 1][col] = g;
				// grid[row][col] = ERASE;
				// break;
				// }
				// // Left or right movement
				// if ((int) (Math.random() * 2) == 0) {
				// // Left movement
				// if (grid[row][col - 1] == ERASE) {
				// grid[row][col - 1] = g;
				// grid[row][col] = ERASE;
				// }
				// } else {
				// // Right movement
				// if (grid[row][col + 1] == ERASE) {
				// grid[row][col + 1] = g;
				// grid[row][col] = ERASE;
				// }
				// break;
				// }

				// new water

				// Bottom row ignore to make it look like still puddles
				if (row >= maxRow - 1) {
					break;
				}
				// Falling
				if (getGrid(row, col, 1, 0) == ERASE) {
					setGrid(row, col, 1, 0, g);
					setGrid(row, col, ERASE);
					break;
				}
				// dx = random number either -1 or 1 for random sideways movement
				int dx = (int) (Math.random() * 2) * 2 - 1;
				// if sideways movement is available (erase), swap
				if (getGrid(row, col, 0, dx) == ERASE) {
					setGrid(row, col, 0, dx, g);
					setGrid(row, col, ERASE);
				}
				break;
			case WOOD:
				if (row < maxRow - 1 && col < maxCol - 1) {
					if (grid[row + 1][col] == ERASE) {
						grid[row + 1][col] = g;
						grid[row][col] = ERASE;
						break;
					}
				}
				break;
			case FIRE:
				int ra = (int) (Math.random() * 2000);
				if (ra == 500) {
					grid[row][col] = ERASE;
				}
				int r = (int) (Math.random() * 4);
				if (r == 0) {
					if (getGrid(row, col, 0, 1) == WOOD) {
						int random = (int) (Math.random() * 150);
						if (random > 147) {
							setGrid(row, col, 0, 1, FIRE);
							for (int i = 0; i < 2; i++) {
								// int ran = (int)(Math.random() * 10);
								// if (ran > 8) {
								if (random > 8) {
									setGrid(row, col, i - 3, 0, SMOKE);
								}
							}
						}
					}
					if (getGrid(row, col, 0, 1) == WATER) {
						int random = (int) (Math.random() * 150);
						if (random > 145) {
							setGrid(row, col, 0, 1, STEAM);
							setGrid(row, col, 0, 0, ERASE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, STEAM);
								}
							}
						}
					}
				} else if (r == 1) {
					if (getGrid(row, col, 0, -1) == WOOD) {
						int random = (int) (Math.random() * 150);
						if (random > 147) {
							setGrid(row, col, 0, -1, FIRE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, SMOKE);
								}
							}
						}
					}
					if (getGrid(row, col, 0, -1) == WATER) {
						int random = (int) (Math.random() * 150);
						if (random > 145) {
							setGrid(row, col, 0, -1, STEAM);
							setGrid(row, col, 0, 0, ERASE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, STEAM);
								}
							}
						}
					}
				} else if (r == 2) {
					if (getGrid(row, col, 1, 0) == WOOD) {
						int random = (int) (Math.random() * 150);
						if (random > 147) {
							setGrid(row, col, 1, 0, FIRE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, SMOKE);
								}
							}
						}
					}
					if (getGrid(row, col, 1, 0) == WATER) {
						int random = (int) (Math.random() * 150);
						if (random > 145) {
							setGrid(row, col, 1, 0, STEAM);
							setGrid(row, col, 0, 0, ERASE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, STEAM);
								}
							}
						}
					}
				} else if (r == 3) {
					if (getGrid(row, col, -1, 0) == WOOD) {
						int random = (int) (Math.random() * 150);
						if (random > 147) {
							setGrid(row, col, -1, 0, FIRE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, SMOKE);
								}
							}
						}
					}
					if (getGrid(row, col, -1, 0) == WATER) {
						int random = (int) (Math.random() * 150);
						if (random > 145) {
							setGrid(row, col, -1, 0, STEAM);
							setGrid(row, col, 0, 0, ERASE);
							for (int i = 0; i < 2; i++) {
								int ran = (int) (Math.random() * 10);
								if (random > 8) {
									setGrid(row, col, i - 3, 0, STEAM);
								}
							}
						}
					}
				}
				// if (getGrid(row, col, 0, -1) == WOOD) {
				// setGrid(row, col, 0, -1, FIRE);
				// for (int i = 0; i < 20; i++) {
				// setGrid(row, col, 2, 0, SMOKE);
				// }
				// setGrid(row, col, 0, -1, FIRE);
				// break;
				// }
				break;
			case LAVA:
				if (getGrid(row, col, 1, 0) == WOOD) {
					int someRan1 = (int) (Math.random() * 30);
					if (someRan1 == 1) {
						setGrid(row, col, 1, 0, FIRE);
					}
				} else if (getGrid(row, col, -1, 0) == WOOD) {
					int someRan1 = (int) (Math.random() * 30);
					if (someRan1 == 1) {
						setGrid(row, col, -1, 0, FIRE);
					}
				} else if (getGrid(row, col, 0, -1) == WOOD) {
					int someRan1 = (int) (Math.random() * 30);
					if (someRan1 == 1) {
						setGrid(row, col, 0, -1, FIRE);
					}
				} else if (getGrid(row, col, 0, 1) == WOOD) {
					int someRan1 = (int) (Math.random() * 30);
					if (someRan1 == 1) {
						setGrid(row, col, 0, 1, FIRE);
					}
				}
				if (getGrid(row, col, 0, 1) == WATER) {
					setGrid(row, col, 0, 1, STEAM);
					int someRan2 = (int) (Math.random() * 10);
					if (someRan2 == 1) {
						setGrid(row, col, 0, 0, OBSIDIAN);
					}
				} else if (getGrid(row, col, 0, -1) == WATER) {
					setGrid(row, col, 0, -1, STEAM);
					int someRan2 = (int) (Math.random() * 10);
					if (someRan2 == 1) {
						setGrid(row, col, 0, 0, OBSIDIAN);
					}
				} else if (getGrid(row, col, 1, 0) == WATER) {
					setGrid(row, col, 1, 0, STEAM);
					int someRan2 = (int) (Math.random() * 10);
					if (someRan2 == 1) {
						setGrid(row, col, 0, 0, OBSIDIAN);
					}
				} else if (getGrid(row, col, -1, 0) == WATER) {
					setGrid(row, col, -1, 0, STEAM);
					int someRan2 = (int) (Math.random() * 10);
					if (someRan2 == 1) {
						setGrid(row, col, 0, 0, OBSIDIAN);
					}
				}
				int someRan3 = (int) ((Math.random() * 100) + 1);
				if (someRan3 == 55 || someRan3 == 86) {
					// to fall
					if (getGrid(row, col, 1, 0) == ERASE) {
						setGrid(row, col, 1, 0, g);
						setGrid(row, col, ERASE);
						break;
					}
					// move horizontally
					int ran2 = (int) (Math.random() * 8);
					if (ran2 == 1) {
						// dxx = random number either -1 or 1 for random sideways movement
						int dxxx = (int) (((Math.random() * 2) * 2) - 1);
						if (getGrid(row, col, 0, dxxx) == ERASE) {
							setGrid(row, col, 0, dxxx, g);
							grid[row][col] = ERASE;
						}
					}
				}
				break;
			case SMOKE:
				// tried and true smoke

				// slow smoke down
				int ran = (int) (Math.random() * 50);
				if (ran < 47) {
					break;
				}

				// Move smoke upward
				ran = (int) (Math.random() * 4);
				if (ran > 0) {
					if (row - 1 >= 0 // oob check row
							&& grid[row - 1][col] == ERASE) {
						grid[row - 1][col] = g;
						grid[row][col] = ERASE;
						break;
					}
				}

				// random left or right movement
				ran = (int) (Math.random() * 2);
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
				if (getGrid(row, col, -1, 0) == WOOD) {
					setGrid(row, col, -1, 0, grid[row][col]);
					setGrid(row, col, WOOD);
				}
				int ra2 = (int) (Math.random() * 2000);
				if (ra2 == 554) {
					grid[row][col] = ERASE;
				}
				break;
			case STEAM:
				int ra1 = (int) (Math.random() * 50);
				if (ra1 < 44) {
					break;
				}

				if (getGrid(row, col, -1, 0) == WATER ||
						getGrid(row, col, -1, 0) == WOOD ||
						getGrid(row, col, -1, 0) == SAND) {
					// setGrid(row, col, 0, 0, );
					int save = grid[row - 1][col];
					setGrid(row, col, -1, 0, STEAM);
					setGrid(row, col, 0, 0, save);
				}

				// Move smoke upward
				ran = (int) (Math.random() * 4);
				if (ran > 0) {
					if (row - 1 >= 0 // oob check row
							&& grid[row - 1][col] == ERASE) {
						grid[row - 1][col] = g;
						grid[row][col] = ERASE;
						break;
					}
				}

				// random left or right movement
				ran = (int) (Math.random() * 2);
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
				if (getGrid(row, col, -1, 0) == WOOD) {
					setGrid(row, col, -1, 0, grid[row][col]);
					setGrid(row, col, WOOD);
					break;
				}
				int ra3 = (int) (Math.random() * 2000);
				if (ra3 == 600) {
					grid[row][col] = ERASE;
				}
				break;
			case LASER:
				// old laser
				/*
				 * // Erases current laser
				 * grid[row][col] = ERASE;
				 * 
				 * // Move laser down and to the right
				 * if (row + 1 <= maxRow - 1 && col + 1 <= maxCol - 1) {
				 * grid[row + 1][col + 1] = LASER;
				 * }
				 * break;
				 */

				// new laser

				// Erases past laser
				setGrid(row, col, 0, 0, ERASE);
				// make new laser down and to right
				setGrid(row, col, 1, 1, LASER);
				break;
			case NUKE:
				// old nuke:
				// Erases laser on an edge
				// if (row == maxRow - 1 || col == maxCol - 1) {
				// grid[row][col] = ERASE;
				// }
				// if (row + 1 <= maxRow - 1 && col + 1 <= maxCol - 1) {
				// grid[row][col] = ERASE;
				// grid[row + 1][col + 1] = NUKE;
				// }
				// if (row + 2 <= maxRow - 1 && col - 1 >= 0 && col <= maxCol - 1) {
				// grid[row + 1][col - 1] = ERASE;
				// grid[row + 2][col] = NUKE;
				// }
				// if (row <= maxRow - 1 && row - 1 >= 0 && col + 2 <= maxCol - 1) {
				// grid[row - 1][col + 1] = ERASE;
				// grid[row][col + 2] = NUKE;
				// }
				// break;

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
			for (int i = 0; i < display.getSpeed(); i++) {
				step();
			}
			updateDisplay();
			display.repaint();
			display.pause(1); // wait for redrawing and for mouse
			int[] mouseLoc = display.getMouseLocation();
			if (mouseLoc != null) { // test if mouse clicked
				locationClicked(mouseLoc[0], mouseLoc[1], display.getTool());
			}
		}
	}
}
