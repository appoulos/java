public class ExpandingArray {
	private static final int STARTING_SIZE = 10;
	private int[] arr;
	private int currentSize;
	private int numElements;

	public ExpandingArray() {
		arr = new int[STARTING_SIZE];
		currentSize = STARTING_SIZE;
		numElements = 0;
	}

	// Remove the element at index `index` and shift
	// all subsequent elements to the left.
	public int remove(int index) {
		// sanity check input
		if (index >= numElements) {
			return -1;
		}
		// store return value
		int result = arr[index];
		// shift left
		for (int i = index; i < numElements - 1; i++) {
			arr[i] = arr[i + 1];
		}
		numElements--;
		return result;
	}
	// int element = arr[index];
	// int[] tArr = new int[arr.length - 1];
	// int ind = 0;
	// for (int i = 0; i < arr.length; i++) {
	// if (i == index) {
	// continue;
	// }
	// tArr[ind++] = arr[i];
	// }
	// arr = tArr;
	// currentSize--;
	// numElements--;
	// // for(int i = index; i < arr.length - 1; i++) {
	// // arr[i] = arr[i + 1];
	// // }
	// return element;
	// }

	// Add the int `element` at the `index` in the array.
	// You'll need to shift everything one index to the right
	// after this index.
	public void add(int index, int element) {
		// copied from the add(int x) method
		if (isFull()) {
			expand();
		}
		// shift elements to right by one to make room for element at index
		for (int i = numElements; i > index; i--) {
			arr[i] = arr[i - 1];
		}
		// add element at index
		arr[index] = element;
		numElements++;
	}

	// int[] tArr = new int[arr.length + 1];
	// int ind = 0;
	// for(int i = 0; i < tArr.length; i++) {
	// if (i == index) {
	// tArr[i] = element;
	// }
	// tArr[ind++] = arr[i];
	// }
	// arr = tArr;
	// currentSize++;
	// numElements++;
	// }

	// Return the number of elements in your array.
	public int size() {
		return arr.length;
	}

	private boolean isFull() {
		return numElements == currentSize;
	}

	private void expand() {
		System.out.println("Expanding");
		int newSize = currentSize * 2;
		int[] newArray = new int[newSize];

		// Copy over old elements
		for (int i = 0; i < currentSize; i++) {
			newArray[i] = arr[i];
		}

		currentSize = newSize;
		arr = newArray;
	}

	public int get(int index) {
		return arr[index];
	}

	public void add(int x) {
		if (isFull()) {
			expand();
		}
		arr[numElements] = x;
		numElements++;
	}

	public String toString() {
		// Return empty curly braces if the array is empty
		if (numElements == 0) {
			return "{}";
		}

		// Return Elements
		String str = "{";
		for (int i = 0; i < numElements; i++) {
			str += arr[i] + ", ";
		}
		if (str.length() > 0 && str.charAt(str.length() - 2) == ',') {
			str = str.substring(0, str.length() - 2);
			str += "}";
		}
		return str;
	}

	public static void main(String[] args) {
		ExpandingArray arr = new ExpandingArray();

		for (int i = 0; i < 9; i++) {
			System.out.println("adding " + i);
			arr.add(i);
		}

		// arr.add(1, 7);
		System.out.println(arr.numElements);
		System.out.println(arr.remove(8));
		System.out.println(arr);

		// for (int i = 0; i < arr.currentSize; i++) {
		// System.out.println(arr.get(i));
		// }
	}
}
