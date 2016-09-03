package com.kimjeeyoung.algo.sorting;

import org.junit.Ignore;
import org.junit.Test;

import java.util.Random;

import static com.kimjeeyoung.algo.sorting.QuickSelect.Algorithm.*;
import static org.junit.Assert.assertEquals;

public class QuickSelectTest {
  private static final Random r = new Random(0);
  private static final int size = 100000;
  private static final int trials = 1000;

  @Test
  @Ignore
  public void testRandomMedian() {
    randomTestTemplate(100, trials, MEDIAN_OF_MEDIAN);
    randomTestDuplicates(100, trials, MEDIAN_OF_MEDIAN);
  }

  @Test
  public void testRandomNaive() {
    randomTestTemplate(size, trials, NAIVE);
    randomTestDuplicates(size, trials, NAIVE);
    randomManyDuplicates(size, trials, NAIVE);
  }

  @Test
  public void testRandomDouble() {
    randomTestTemplate(size, trials, DOUBLE_PIVOT);
    randomTestDuplicates(size, trials, DOUBLE_PIVOT);
    randomManyDuplicates(size, trials, DOUBLE_PIVOT);
  }

  private void randomTestTemplate(int length, int trials, QuickSelect.Algorithm algorithm) {
    int[] ary = new int[length];
    for (int i = 0; i < length; i++) {
      ary[i] = i * 10;
    }
    int swaps = 0;
    int compares = 0;
    for (int trial = 0; trial < trials; trial++) {
      SortTestUtil.shuffleArray(ary);
      int idx = r.nextInt(length);
      Sortable.InstrumentedSortable sortable = Sortable.instrument(Sortable.from(ary));
      assertEquals(ary[QuickSelect.select(sortable, idx, algorithm)], idx * 10);
      swaps += sortable.getInstrumentation().getSwaps();
      compares += sortable.getInstrumentation().getCompares();
    }
    System.out.printf("%d %d\n", swaps / trials, compares / trials);
  }

  private void randomTestDuplicates(int length, int trials, QuickSelect.Algorithm algorithm) {
    int[] ary = new int[length];
    int breaking = (int) (length * 0.5);
    for (int i = 0; i < length; i++) {
      if (i < breaking) {
        ary[i] = 0;
      } else {
        ary[i] = 1;
      }
    }
    int swaps = 0;
    int compares = 0;
    for (int trial = 0; trial < trials; trial++) {
      SortTestUtil.shuffleArray(ary);
      int idx = r.nextInt(length);
      Sortable.InstrumentedSortable sortable = Sortable.instrument(Sortable.from(ary));
      assertEquals(ary[QuickSelect.select(sortable, idx, algorithm)], idx < breaking ? 0 : 1);
      swaps += sortable.getInstrumentation().getSwaps();
      compares += sortable.getInstrumentation().getCompares();
    }
    System.out.printf("%d %d\n", swaps / trials, compares / trials);
  }

  private void randomManyDuplicates(int length, int trials, QuickSelect.Algorithm algorithm) {
    int[] ary = new int[length];
    int breaking = (int) (length * 0.01);
    for (int i = 0; i < length; i++) {
      if (i < breaking) {
        ary[i] = 0;
      } else {
        ary[i] = 1;
      }
    }
    int swaps = 0;
    int compares = 0;
    for (int trial = 0; trial < trials; trial++) {
      SortTestUtil.shuffleArray(ary);
      Sortable.InstrumentedSortable sortable = Sortable.instrument(Sortable.from(ary));
      assertEquals(ary[QuickSelect.select(sortable, breaking - 1, algorithm)], 0);
      assertEquals(ary[QuickSelect.select(sortable, breaking, algorithm)], 1);
      swaps += sortable.getInstrumentation().getSwaps();
      compares += sortable.getInstrumentation().getCompares();
    }
    System.out.printf("%d %d\n", swaps / trials, compares / trials);
  }
}