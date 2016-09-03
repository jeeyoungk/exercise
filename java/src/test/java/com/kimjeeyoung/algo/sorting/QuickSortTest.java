package com.kimjeeyoung.algo.sorting;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.UniformReservoir;
import org.junit.Test;

import java.util.function.Consumer;

public class QuickSortTest {
  @Test
  public void randomSinglePivotTest() {
    QuickSort qs = new QuickSort(10, 1);
    sortingTest(1000, qs::sort);
    sortingTest(10000, qs::sort);
    sortingTest(100000, qs::sort);
    sortingTest(1000000, qs::sort);
  }

  @Test
  public void randomSinglePivotTest_higherThreshold() {
    QuickSort qs = new QuickSort(100, 1);
    sortingTest(1000, qs::sort);
    sortingTest(10000, qs::sort);
    sortingTest(100000, qs::sort);
    sortingTest(1000000, qs::sort);
  }

  @Test
  public void randomDoublePivotTest() {
    QuickSort qs = new QuickSort(10, 2);
    sortingTest(1000, qs::sort);
    sortingTest(10000, qs::sort);
    sortingTest(100000, qs::sort);
    sortingTest(1000000, qs::sort);
  }

  @Test
  public void randomDoublePivotTest_higherThreshold() {
    QuickSort qs = new QuickSort(100, 2);
    sortingTest(1000, qs::sort);
    sortingTest(10000, qs::sort);
    sortingTest(100000, qs::sort);
    sortingTest(1000000, qs::sort);
  }

  @Test
  public void randomDoublePivotTest_noThreshold() {
    QuickSort qs = new QuickSort(0, 2);
    sortingTest(1000, qs::sort);
    sortingTest(10000, qs::sort);
    sortingTest(100000, qs::sort);
    sortingTest(1000000, qs::sort);
  }

  public void sortingTest(int n, Consumer<Sortable> sorter) {
    int[] array = SortTestUtil.newArray(n);
    Histogram swaps = new Histogram(new UniformReservoir());
    Histogram compares = new Histogram(new UniformReservoir());
    for (int trial = 0; trial < 10; trial++) {
      SortTestUtil.shuffleArray(array);
      Sortable.InstrumentedSortable instrumented = Sortable.instrument(Sortable.from(array));
      sorter.accept(instrumented);
      SortTestUtil.assertSorted(instrumented);
      swaps.update(instrumented.getInstrumentation().getSwaps());
      compares.update(instrumented.getInstrumentation().getCompares());
    }
    System.out.printf("n = %d\nswaps    = %f\ncompares = %f\n",
      n,
      swaps.getSnapshot().getMean() / n,
      compares.getSnapshot().getMean() / n);
  }
}