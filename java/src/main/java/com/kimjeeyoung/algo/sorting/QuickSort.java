package com.kimjeeyoung.algo.sorting;

import java.util.Random;

import static com.kimjeeyoung.algo.sorting.SortUtil.randomPivot;

public class QuickSort {
  private static final Random r = new Random(0);
  private static final int INSERTION_SORT = 10;
  private final int insertionSort;
  private final int pivots;

  public QuickSort(int insertionSort, int pivots) {
    this.insertionSort = insertionSort;
    this.pivots = pivots;
  }

  public void sort(Sortable s) {
    if (pivots == 1) {
      singlePivotSort(s, 0, s.size());
    } else if (pivots == 2) {
      doublePivotSort(s, 0, s.size());
    } else if (pivots == 3) {
      triplePivotSort(s, 0, s.size());
    }
  }

  private void singlePivotSort(Sortable s, int begin, int end) {
    if (basecase(s, begin, end)) {
      return;
    }

    int i = begin + 1;
    int j = end - 1;
    while (i <= j) {
      if (s.less(i, begin)) {
        i++;
      } else if (!s.less(j, begin)) {
        j--;
      } else {
        s.swap(i, j);
        i++;
        j--;
      }
    }
    s.swap(begin, j);
    singlePivotSort(s, begin, j);
    singlePivotSort(s, j + 1, end);
  }

  private void doublePivotSort(Sortable s, int begin, int end) {
    // TODO - implement this.
    if (basecase(s, begin, end)) {
      return;
    }
    // pick two without replacement.
    int p1 = randomPivot(r, begin, end);
    int p2 = randomPivot(r, begin, end - 1);
    if (p2 >= p1) {
      p2++;
    }
    if (s.less(p2, p1)) {
      // wlog p1 <= p2.
      int tmp = p1;
      p1 = p2;
      p2 = tmp;
    }

    if (p2 == begin && p1 == end - 1) {
      s.swap(p1, p2);
    } else if (p2 == begin) {
      s.swap(p2, end - 1);
      s.swap(p1, begin);
    } else {
      s.swap(p1, begin);
      s.swap(p2, end - 1);
    }

    int l = begin + 1; // next "less than p1" location.
    int r = end - 2; // next "greater than p2" location.
    int it = l;
    while (it <= r) {
      if (s.less(it, begin)) {
        s.swap(it, l); // push to left.
        it++;
        l++;
      } else if (s.less(end - 1, it)) {
        s.swap(it, r); // push to right.
        r--;
      } else {
        it++; // push to middle.
      }
    }
    // Invariants:
    // l <= it = r + 1
    // before l -> v < p1
    // after  r -> p2 < v
    final int newP1 = l - 1;
    final int newP2 = r + 1;
    s.swap(begin, newP1);
    s.swap(end - 1, newP2);
    doublePivotSort(s, begin, newP1);
    doublePivotSort(s, newP1 + 1, newP2);
    doublePivotSort(s, newP2 + 1, end);
  }

  private void triplePivotSort(Sortable s, int begin, int end) {
    if (basecase(s, begin, end)) {
      return;
    }

    int p1 = randomPivot(r, begin, end);
    int p2 = randomPivot(r, begin, end - 1);
    int p3 = randomPivot(r, begin, end - 2);
    if (p3 >= p2) {
      p3++;
    }
    if (p2 >= p1) {
      p2++;
    }
    if (p3 >= p1) {
      p3++;
    }
  }

  /**
   * Perform an insertion singlePivotSort.
   */
  private static void insertionSort(Sortable s, int begin, int end) {
    for (int i = begin + 1; i < end; i++) {
      int j = i;
      while (j > begin && s.less(j, j - 1)) {
        s.swap(j, j - 1);
        j--;
      }
    }
  }

  private boolean basecase(Sortable s, int begin, int end) {
    int length = (end - begin);
    if (length <= 1) {
      return true;
    }
    if (length <= insertionSort) {
      insertionSort(s, begin, end);
      return true;
    }
    return false;
  }
}
