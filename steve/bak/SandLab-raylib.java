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

	public void setGrid(int x, int y, int dx, int dy, int element) {
		if (dx > 0 && x + dx >= screenWidth)
			return;
		if (dx < 0 && x + dx < 0)
			return;
		if (dy > 0 && y + dy >= screenHeight)
			return;
		if (dy < 0 && y + dy < 0)
			return;
		grid[y + dy][x + dx] = element;
	}

	public void setGrid(int x, int y, int element) {
		grid[y][x] = element;
	}

	public int getGrid(int x, int y, int dx, int dy) {
		if (dx > 0 && x + dx >= screenWidth)
			return METAL;
		if (dx < 0 && x + dx < 0)
			return METAL;
		if (dy > 0 && y + dy >= screenHeight)
			return METAL;
		if (dy < 0 && y + dy < 0)
			return METAL;
		return grid[y + dy][x + dx];
	}

	public int getGrid(int x, int y) {
		return grid[y][x];
	}

	// called repeatedly.
	// causes one random particle to maybe do something.
	public void step() {
		int x = (int) (Math.random() * screenWidth);
		int y = (int) (Math.random() * screenHeight);

		switch (getGrid(x, y)) {
			case SAND:
				if (getGrid(x, y, 0, 1) == ERASE) {
					setGrid(x, y, ERASE);
					setGrid(x, y, 0, 1, SAND);
				}
				break;
		}
		// if (y != grid.length - 1) {
		// if (grid[y][x] == SAND && grid[y + 1][x] != SAND && grid[y + 1][x] != METAL)
		// {
		// grid[y + 1][x] = SAND;
		// grid[y][x] = ERASE;
		// } else if (grid[y][x] == LASER) {
		// grid[y + 1][x] = LASER;
		// grid[y][x] = ERASE;
		//
		// }
		// }
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
