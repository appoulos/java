import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import javax.swing.*;

public class MyProgram extends JPanel {

	private static JFrame frame;
	private final StringBuilder sb = new StringBuilder();

	public static void main(String[] args) {
		new MyProgram();
	}

	public MyProgram() {
		Dimension d = new Dimension(600, 400);

		frame = new JFrame();
		frame.setPreferredSize(d);
		frame.setMinimumSize(d);
		frame.setMaximumSize(d);

		frame.setTitle("Arrays");
		frame.setLayout(new BorderLayout());

		Font font = new Font("Sans", Font.PLAIN, 20);

		JTextArea textarea = new JTextArea(24, 40);
		textarea.setFont(font);
		textarea.setEditable(false);
		textarea.setText("Running tests\n\nPlease wait...");
		frame.add(new JScrollPane(textarea), BorderLayout.CENTER);

		JButton button = new JButton("Back to main menu...");
		button.setFont(font);
		button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
				Games game = new Games();
				game.setVisible(true);
			}
		});
		frame.add(button, BorderLayout.SOUTH);

		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		frame.pack();
		textarea.paintImmediately(0, 0, d.width, d.height);
		button.paintImmediately(0, 0, d.width, d.height);
		frame.repaint();
		this.repaint();

		System.out.println("Running...");
		int[] arr = new int[100];
		// System.out.println(printArr(arr));
		// randomizeArr(arr);
		// System.out.println(printArr(arr));
		// int[] newArr = StevenSort(arr);
		// System.out.println(printArr(newArr));
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

		System.out.println("Steven Sort's Elapsed time (nanoseconds): " + (int) avg + "\n");
		sb.append("Steven Sort's Elapsed time (nanoseconds): " + (int) avg + "\n");
		sb.append("Insertion Sort's Elapsed time (nanoseconds): " + (int) avg1 + "\n");
		sb.append("Selection Sort's Elapsed time (nanoseconds): " + (int) avg2 + "\n");
		sb.append("Merge Sort's Elapsed time (nanoseconds): " + (int) avg3 + "\n");
		System.out.println("Insertion Sort's Elapsed time (nanoseconds): " + (int) avg1);
		System.out.println("Selection Sort's Elapsed time (nanoseconds): " + (int) avg2);
		System.out.println("Merge Sort's Elapsed time (nanoseconds): " + (int) avg3);
		textarea.setText(sb.toString());
	}

	public static String printArr(int[] arr) {
		String str = "";
		str += "[";
		for (int i = 0; i < arr.length; i++) {
			if (i != arr.length - 1) {
				str += arr[i] + ", ";
			} else {
				str += arr[i];
			}
		}
		str += "]";
		return str;
	}

	public static void randomizeArr(int[] arr) {
		for (int i = 0; i < arr.length; i++) {
			arr[i] = (int) (Math.random() * 10);
		}
	}

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

	// Main function that sorts arr[l..r] using
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

	public static int[] StevenSort(int[] arr) {
		int number = findLargestNumber(arr);
		int[] newArr = new int[number + 1];
		for (int i = 0; i < arr.length; i++) {
			newArr[arr[i]] += 1;
		}
		return newArr;
	}

	public static int findLargestNumber(int[] arr) {
		int max = 0;
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		return max;
	}

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

	public static void delay(int m) {
		try {
			Thread.sleep(m);
		} catch (Exception e) {
		}
	}
}
