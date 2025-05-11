
import javax.swing.*;
// import java.util.ArrayList;

public class SteveSort {

	private static JFrame f;

	public static void main(String[] args) {
		// f = new JFrame();
		// f.setSize(400, 400);
		// f.setLayout(null);
		// f.setVisible(true);
		// makeFrame(100, 100, 100, 100, "Hi");
		// makeFrame(300, 300, 100, 100, "Hi");
		int[] arr = new int[20];
		System.out.println(printArr(arr));
		randomizeArr(arr);
		System.out.println(printArr(arr));
		int[] newArr = StevenSort(arr);
		System.out.println(printArr(newArr));
		System.out.println(printSortedSpecialArr(newArr));
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
				max = i;
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

	public static void makeFrame(int l, int w, int ll, int ur, String text) {
		JButton b1 = new JButton(text);
		b1.setBounds(ll, ur, l, w);
		f.add(b1);
	}
}
