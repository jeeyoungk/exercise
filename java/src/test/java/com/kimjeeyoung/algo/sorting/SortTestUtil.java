package com.kimjeeyoung.algo.sorting;

import org.junit.Assert;

import java.util.Random;

import static org.junit.Assert.assertTrue;

public class SortTestUtil {
  private static final Random r = new Random(0);

  /**
   * Shuffles an array. Used for tests.
   */
  public static void shuffleArray(int[] array) {
    for (int i = array.length - 1; i > 0; i--) {
      int index = r.nextInt(i + 1);
      swap(array, i, index);
    }
  }

  public static int[] newArray(int length) {
    int[] ary = new int[length];
    for (int i = 0; i < length; i++) {
      ary[i] = i;
    }
    return ary;
  }

  static void swap(int[] array, int i, int j) {
    int tmp = array[i];
    array[i] = array[j];
    array[j] = tmp;
  }

  public static void assertSorted(Sortable sortable) {
    for (int i = 1; i < sortable.size(); i++) {
      assertTrue(!sortable.less(i, i - 1));
    }
  }
}
