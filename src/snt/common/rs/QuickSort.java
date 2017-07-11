package snt.common.rs;

import java.util.Comparator;

// Decompiled by DJ v2.9.9.60 Copyright 2000 Atanas Neshkov Date: 2002-10-11
// 12:51:40
// Home Page : http://members.fortunecity.com/neshkov/dj.html - Check often for
// new version!
// Decompiler options: packimports(3)
// Source File Name: Sort.java

// Referenced classes of package sun.misc:
//            Compare

public class QuickSort {

	public QuickSort() {
	}

	public static void quicksort(Object arr[], int left, int right,
			Comparator comp) {
		if (left >= right)
			return;
		swap(arr, left, (left + right) / 2);
		int last = left;
		for (int i = left + 1; i <= right; i++)
			if (comp.compare(arr[i], arr[left]) < 0)
				swap(arr, ++last, i);

		swap(arr, left, last);
		quicksort(arr, left, last - 1, comp);
		quicksort(arr, last + 1, right, comp);
	}

	public static void quicksort(Object arr[], Comparator comp) {
		quicksort(arr, 0, arr.length - 1, comp);
	}

	private static void swap(Object arr[], int i, int j) {
		Object tmp = arr[i];
		arr[i] = arr[j];
		arr[j] = tmp;
	}
}