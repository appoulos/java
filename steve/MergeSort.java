import java.util.Arrays;

public class MergeSort extends ConsoleProgram {
	/*
	 * Do not make changes to this method!
	 */
	public void run() {
		int[] array1 = { 9, 8, 7, 6, 5, 4, 3, 2, 1 };
		int[] array2 = { 5, 6, 4, 8, 9, 7, 3, 1, 2 };

		System.out.print("First array: ");
		System.out.println(Arrays.toString(array1));
		System.out.print("Second array: ");
		System.out.println(Arrays.toString(array2));
		System.out.println();

		// sort first array
		mergeSort(array1);
		// sort second array
		mergeSort(array2);

		System.out.print("First array sorted: ");
		System.out.println(Arrays.toString(array1));
		System.out.print("Second array sorted: ");
		System.out.println(Arrays.toString(array2));
	}

	/*
	 * Merge sort takes in an array and sorts it.
	 */
	public static void mergeSort(int[] arr) {
		if (arr.length <= 1) {
			return;
		}

		// Split the array in half
		int[] firstHalf = new int[arr.length / 2];
		int[] secondHalf = new int[arr.length - firstHalf.length];
		System.arraycopy(arr, 0, firstHalf, 0, firstHalf.length);
		System.arraycopy(arr, firstHalf.length, secondHalf, 0, secondHalf.length);

		// Sort each half
		mergeSort(firstHalf);
		mergeSort(secondHalf);

		// Print lines
		System.out.println(Arrays.toString(firstHalf));
		System.out.println(Arrays.toString(secondHalf));

		// Merge the halves together
		merge(firstHalf, secondHalf, arr);
	}

	/*
	 * merge takes in three arrays. The first two are the two halves of an array
	 * to be merged. The result is the resulting array that consists of the elements
	 * in the two half arrays, sorted.
	 */
	private static void merge(int[] firstHalf, int[] secondHalf, int[] result) {

		// set up indices for iteration through arrays
		int firstIndex = 0;
		int secondIndex = 0;
		int resultIndex = 0;

		// while there are still elements in both halves, find which is smaller
		// and add it to the result array first. Then, add the larger.
		while (firstIndex < firstHalf.length && secondIndex < secondHalf.length) {
			if (firstHalf[firstIndex] < secondHalf[secondIndex]) {
				result[resultIndex] = firstHalf[firstIndex];
				firstIndex++;
			} else {
				result[resultIndex] = secondHalf[secondIndex];
				secondIndex++;
			}
			resultIndex++;
		}

		// There might be left over elements in one of the halves.
		// Copy it over as well.
		System.arraycopy(firstHalf, firstIndex, result, resultIndex, firstHalf.length - firstIndex);
		System.arraycopy(secondHalf, secondIndex, result, resultIndex, secondHalf.length - secondIndex);
	}
}
