import java.awt.*;
// import java.util.*;

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
	public static final int LASER = 4;

	// do not add any more fields
	private int[][] grid;
	private SandDisplay display;
	private int screenWidth;
	private int screenHeight;

	public SandLab(int numRows, int numCols) {
		String[] names;
		names = new String[5];
		names[ERASE] = "ERASE";
		names[METAL] = "Metal";
		names[SAND] = "Sand";
		names[WATER] = "Water";
		names[LASER] = "Laser";
		grid = new int[numRows][numCols];
		screenWidth = numCols;
		screenHeight = numRows;
		display = new SandDisplay("Falling Sand", numRows, numCols, names);
	}

	// called when the user clicks on a location using the given tool
	private void locationClicked(int row, int col, int tool) {
		if (grid[row][col] == ERASE && tool != LASER && tool != ERASE) {
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
				} else if (grid[row][col] == ERASE) {
					display.setColor(row, col, Color.black);
				} else if (grid[row][col] == LASER) {
					display.setColor(row, col, Color.red);
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
		int row = (int) (Math.random() * grid.length);
		int col = (int) (Math.random() * grid[0].length);

		switch (getGrid(row, col)) {
			case SAND:
				if (getGrid(row, col, 0, 1) == ERASE) {
					setGrid(row, col, ERASE);
					setGrid(row, col, 0, 1, SAND);
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
