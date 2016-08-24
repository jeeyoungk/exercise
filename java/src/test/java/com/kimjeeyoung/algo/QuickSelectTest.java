package com.kimjeeyoung.algo;

import org.junit.Test;

import java.util.Random;

import static com.kimjeeyoung.algo.QuickSelect.Algorithm.DOUBLE_PIVOT;
import static com.kimjeeyoung.algo.QuickSelect.Algorithm.MEDIAN_OF_MEDIAN;
import static com.kimjeeyoung.algo.QuickSelect.Algorithm.NAIVE;
import static org.junit.Assert.assertEquals;

public class QuickSelectTest {
  private static final Random r = new Random(0);
  public static final int size = 100000;

  @Test
  public void testRandomMedian() {
    int trials = 100;
    randomTestTemplate(100, trials, MEDIAN_OF_MEDIAN);
  }

  @Test
  public void testRandomNaive() {
    int trials = 1000;
    randomTestTemplate(size, trials, NAIVE);
  }

  @Test
  public void testRandomDouble() {
    int trials = 1000;
    randomTestTemplate(size, trials, DOUBLE_PIVOT);
  }

  private void randomTestTemplate(int length, int trials, QuickSelect.Algorithm algorithm) {
    int[] ary = new int[length];
    for (int i = 0; i < length; i++) {
      ary[i] = i * 10;
    }
    shuffleArray(ary);
    for (int trial = 0; trial < trials; trial++) {
      int idx = r.nextInt(length);
      assertEquals(ary[QuickSelect.select(Sortable.fromInt(ary), idx, algorithm)], idx * 10);
    }
  }

  /** Shuffles an array. Used for tests. */
  private static void shuffleArray(int[] array) {
    for (int i = array.length - 1; i > 0; i--) {
      int index = r.nextInt(i + 1);
      swap(array, i, index);
    }
  }

  static void swap(int[] array, int i, int j) {
    int tmp = array[i];
    array[i] = array[j];
    array[j] = tmp;
  }
}