import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * MyProgram runs tests to time the performance of selection sort, merge sort,
 * insertion sort and a custom sort method.
 */
public class MyProgram extends JPanel {
	private Dimension d;
	private static JFrame frame;
	private final StringBuilder sb = new StringBuilder();
	private JTextArea textarea;

	/**
	 * Instantiate a new <code>MyProgram</code> object which starts a new
	 * <code>JFrame</code>.
	 * 
	 * @param args not used.
	 */
	public static void main(String[] args) {
		new MyProgram();
	}

	/**
	 * Setup the GUI.
	 */
	public MyProgram() {
		d = new Dimension(600, 400);

		frame = new JFrame();
		frame.setPreferredSize(d);
		frame.setMinimumSize(d);
		frame.setMaximumSize(d);

		frame.setTitle("Arrays");
		frame.setLayout(new BorderLayout());

		JPanel jpButtons = new JPanel();
		Font font = new Font("Sans", Font.PLAIN, 20);

		textarea = new JTextArea(24, 40);
		textarea.setFont(font);
		textarea.setEditable(false);
		textarea.setText("Press `Run` button to start tests, `Quit` to exit");

		frame.add(new JScrollPane(textarea), BorderLayout.CENTER);

		JButton buttonRun = new JButton("Run");
		buttonRun.setFont(font);
		buttonRun.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				run();
			}
		});
		// frame.add(buttonRun, BorderLayout.SOUTH);
		jpButtons.add(buttonRun); // , BorderLayout.SOUTH);

		JButton buttonQuit = new JButton("Quit");
		buttonQuit.setFont(font);
		buttonQuit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				Games game = new Games();
				game.setVisible(true);
			}
		});
		// frame.add(buttonQuit, BorderLayout.SOUTH);
		jpButtons.add(buttonQuit); // , BorderLayout.SOUTH);

		frame.add(jpButtons, BorderLayout.SOUTH);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();

		buttonRun.requestFocusInWindow();

		frame.setVisible(true);
	}

	/**
	 * Run tests and output results to the terminal and the GUI.
	 */
	void run() {
		System.out.println("Running...");
		textarea.setText("Running...");
		textarea.paintImmediately(0, 0, d.width, d.height);

		int[] arr = new int[100];
		// System.out.println(Arrays.toString(arr));
		// randomizeArr(arr);
		// System.out.println(Arrays.toString(arr));
		// int[] newArr = StevenSort(arr);
		// System.out.println(Arrays.toString(newArr));
		// System.out.println(printSortedSpecialArr(newArr));
		int i = 0;
		int j = 0;
		int k = 0;
		int x = 0;
		ArrayList<Long> list = new ArrayList<Long>();
		ArrayList<Long> list1 = new ArrayList<Long>();
		ArrayList<Long> list2 = new ArrayList<Long>();
		ArrayList<Long> list3 = new ArrayList<Long>();

		for (i = 0; i < 100000; i++) {
			randomizeArr(arr);
			long startTime = System.nanoTime();
			int[] newArr = StevenSort(arr);
			long endTime = System.nanoTime();
			long elapsedTime = endTime - startTime;
			list.add(elapsedTime);
		}

		for (j = 0; j < 100000; j++) {
			randomizeArr(arr);
			long startTime = System.nanoTime();
			insertionSort(arr);
			long endTime = System.nanoTime();
			long elapsedTime = endTime - startTime;
			list1.add(elapsedTime);
		}

		for (k = 0; k < 100000; k++) {
			randomizeArr(arr);
			long startTime = System.nanoTime();
			selectionSort(arr);
			long endTime = System.nanoTime();
			long elapsedTime = endTime - startTime;
			list2.add(elapsedTime);
		}

		for (x = 0; x < 100000; x++) {
			randomizeArr(arr);
			long startTime = System.nanoTime();
			mergeSort(arr, 0, arr.length - 1);
			long endTime = System.nanoTime();
			long elapsedTime = endTime - startTime;
			list3.add(elapsedTime);
		}

		double avg = 0.0;
		double avg1 = 0.0;
		double avg2 = 0.0;
		double avg3 = 0.0;

		for (i = 0; i < 100000; i++) {
			avg += list.get(i);
		}

		for (j = 0; j < 100000; j++) {
			avg1 += list1.get(j);
		}

		for (k = 0; k < 100000; k++) {
			avg2 += list2.get(k);
		}

		for (x = 0; x < 100000; x++) {
			avg3 += list3.get(x);
		}

		avg /= (double) i;
		avg1 /= (double) j;
		avg2 /= (double) k;
		avg3 /= (double) x;

		sb.append("Steven Sort's Elapsed time (nanoseconds): " + (int) avg + "\n");
		sb.append("Insertion Sort's Elapsed time (nanoseconds): " + (int) avg1 + "\n");
		sb.append("Selection Sort's Elapsed time (nanoseconds): " + (int) avg2 + "\n");
		sb.append("Merge Sort's Elapsed time (nanoseconds): " + (int) avg3 + "\n");
		sb.append("\n");
		textarea.setText(sb.toString());

		System.out.println("Steven Sort's Elapsed time (nanoseconds): " + (int) avg + "\n");
		System.out.println("Insertion Sort's Elapsed time (nanoseconds): " + (int) avg1);
		System.out.println("Selection Sort's Elapsed time (nanoseconds): " + (int) avg2);
		System.out.println("Merge Sort's Elapsed time (nanoseconds): " + (int) avg3);
		System.out.println();
	}

	/**
	 * Fill array with random numbers from 0-10.
	 * 
	 * @param arr array to be filled.
	 */
	public static void randomizeArr(int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (int) (Math.random() * 10);
		}
	}

	/**
	 * Use selection sort to sort array.
	 * 
	 * @param arr array to be sorted.
	 */
	public static void selectionSort(int[] arr) {
		int n = arr.length;

		for (int i = 0; i < n - 1; i++) {
			int minIndex = i;

			// Find the index of the minimum element
			for (int j = i + 1; j < n; j++) {
				if (arr[j] < arr[minIndex]) {
					minIndex = j;
				}
			}

			// Swap the found minimum element with the first element
			int temp = arr[minIndex];
			arr[minIndex] = arr[i];
			arr[i] = temp;
		}
	}

	/**
	 * Use insertion sort to sort array.
	 * 
	 * @param arr array to be sorted.
	 */
	public static void insertionSort(int[] arr) {
		int n = arr.length;

		for (int i = 1; i < n; i++) {
			int key = arr[i];
			int j = i - 1;

			// Move elements that are greater than key
			while (j >= 0 && arr[j] > key) {
				arr[j + 1] = arr[j];
				j = j - 1;
			}

			// Insert the key at its correct position
			arr[j + 1] = key;
		}
	}

	/**
	 * Merge sort helper to merge two sub arrays.
	 * 
	 * @param arr array to be sorted.
	 * @param l   left index.
	 * @param m   middle index.
	 * @param r   right index.
	 */
	static void merge(int arr[], int l, int m, int r) {
		// Find sizes of two subarrays to be merged
		int n1 = m - l + 1;
		int n2 = r - m;

		// Create temp arrays
		int L[] = new int[n1];
		int R[] = new int[n2];

		// Copy data to temp arrays
		for (int i = 0; i < n1; ++i)
			L[i] = arr[l + i];
		for (int j = 0; j < n2; ++j)
			R[j] = arr[m + 1 + j];

		// Merge the temp arrays

		// Initial indices of first and second subarrays
		int i = 0, j = 0;

		// Initial index of merged subarray array
		int k = l;
		while (i < n1 && j < n2) {
			if (L[i] <= R[j]) {
				arr[k] = L[i];
				i++;
			} else {
				arr[k] = R[j];
				j++;
			}
			k++;
		}

		// Copy remaining elements of L[] if any
		while (i < n1) {
			arr[k] = L[i];
			i++;
			k++;
		}

		// Copy remaining elements of R[] if any
		while (j < n2) {
			arr[k] = R[j];
			j++;
			k++;
		}
	}

	/**
	 * Merge sort to sort array. Call with l==0 and r==length-1 to sort entire
	 * array.
	 * 
	 * @param arr array to be sorted.
	 * @param l   left index.
	 * @param r   right index.
	 */
	public static void mergeSort(int arr[], int l, int r) {
		if (l < r) {

			// Find the middle point
			int m = l + (r - l) / 2;

			// Sort first and second halves
			mergeSort(arr, l, m);
			mergeSort(arr, m + 1, r);

			// Merge the sorted halves
			merge(arr, l, m, r);
		}
	}

	/**
	 * Use Steven sort to sort array.
	 * 
	 * @param arr array to be sorted.
	 */
	public static int[] StevenSort(int[] arr) {
		int number = findLargestNumber(arr);
		int[] newArr = new int[number + 1];
		for (int i = 0; i < arr.length; i++) {
			newArr[arr[i]] += 1;
		}
		return newArr;
	}

	/**
	 * Find largest number in array.
	 * 
	 * @param arr array to be searched.
	 * @return max value in array.
	 */
	public static int findLargestNumber(int[] arr) {
		int max = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		return max;
	}

	/**
	 * Assemble sorted array from a Steven sort special array.
	 * 
	 * @param arr special array to print.
	 * @return sorted array.
	 */
	public static String printSortedSpecialArr(int[] arr) {
		String str = "[";
		String separator = "";
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i]; j++) {
				str += separator + i;
				separator = ", ";
			}
		}
		return str + "]";
	}
}
