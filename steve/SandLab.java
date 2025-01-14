import java.awt.*;
import java.util.*;

public class SandLab
{
    public static void main(String[] args)
    {
        SandLab lab = new SandLab(60, 120);
        lab.run();
    }

    //add constants for particle types here
    public static final int ERASE = 0;
    public static final int METAL = 1;
    public static final int SAND = 2;
    public static final int WATER = 3;
    public static final int LASER = 4;

    //do not add any more fields
    private int[][] grid;
    private SandDisplay display;

    public SandLab(int numRows, int numCols)
    {
        String[] names;
        names = new String[5];
        names[ERASE] = "ERASE";
        names[METAL] = "Metal";
        names[SAND] = "Sand";
        names[WATER] = "Water";
        names[LASER] = "Laser";
        grid = new int[numRows][numCols];
        display = new SandDisplay("Falling Sand", numRows, numCols, names);
    }

//called when the user clicks on a location using the given tool
    private void locationClicked(int row, int col, int tool)
    {
        if (grid[row][col] == ERASE && tool != LASER && tool != ERASE) {
            grid[row][col] = tool;
        }
    }

    //copies each element of grid into the display
    public void updateDisplay()
    {
        for(int row = 0; row < grid.length; row++) {
            for(int col = 0; col < grid[0].length; col++) {
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

   
    //called repeatedly.
    //causes one random particle to maybe do something.
    public void step()
    {
        int row = (int)(Math.random() * grid.length);
        int col = (int)(Math.random() * grid[0].length);
        
        if (row != grid.length - 1) {
            if (grid[row][col] == SAND && grid[row + 1][col] != SAND && grid[row + 1][col] != METAL) {
                grid[row + 1][col] = SAND;
                grid[row][col] = ERASE;
            } else if (grid[row][col] == LASER) {
                grid[row + 1][col] = LASER;
                grid[row][col] = ERASE;
                
            }
        }
    }

//do not modify
    public void run()
    {
        while (true)
        {
            for (int i = 0; i < display.getSpeed(); i++)
                step();
            updateDisplay();
            display.repaint();
            display.pause(1);  //wait for redrawing and for mouse
            int[] mouseLoc = display.getMouseLocation();
            if (mouseLoc != null)  //test if mouse clicked
                locationClicked(mouseLoc[0], mouseLoc[1], display.getTool());
        }
    }
}
 
