package com.kimjeeyoung.algo.sorting;

import java.util.Random;

import static com.google.common.base.Preconditions.checkArgument;
import static com.kimjeeyoung.algo.sorting.SortUtil.randomPivot;

/**
 * Implementation of quickselect with pivoting strategy.
 */
public class QuickSelect {
  private static final Random r = new Random(0);
  private static final boolean DEBUG = false;
  private final Algorithm algorithm;
  private final Sortable sortable;

  public enum Algorithm {
    NAIVE,
    DOUBLE_PIVOT,
    MEDIAN_OF_MEDIAN
  }

  private QuickSelect(Algorithm algorithm, Sortable sortable) {
    this.algorithm = algorithm;
    this.sortable = sortable;
  }

  /**
   * This method modifies the underlying array.
   */
  public static int select(Sortable sortable, int percentile, Algorithm algorithm) {
    QuickSelect qs = new QuickSelect(algorithm, sortable);
    return qs.select(percentile, 0, sortable.size());
  }

  private int select(
    int percentile,
    int startInclusive,
    int endExclusive) {
    debug("%d th in [%d, %d)", percentile, startInclusive, endExclusive);
    checkArgument(0 <= startInclusive);
    checkArgument(startInclusive < endExclusive);
    checkArgument(endExclusive <= sortable.size());
    int length = endExclusive - startInclusive;
    if (length == 1) {
      return startInclusive;
    }
    checkArgument(percentile < length);
    if (algorithm != Algorithm.DOUBLE_PIVOT) {
      return selectSingle(percentile, startInclusive, endExclusive);
    } else {
      return selectDouble(percentile, startInclusive, endExclusive);
    }
  }

  private int selectSingle(int percentile, final int startInclusive, int endExclusive) {
    int pivotIndex = pivot(startInclusive, endExclusive);
    sortable.swap(pivotIndex, startInclusive); // move the pivot to the left end.
    int l = startInclusive + 1;
    int r = endExclusive - 1;
    while (l <= r) {
      // everything before l is smaller-or-equal to the pivot,
      // everything after r is greater-or-equal to the pivot.
      // notice elements equal to pivot can to either direction - this is intentional
      // and ensures that size(left) ~ size(right) when most elements are duplicates.
      if (sortable.less(l, startInclusive)) {
        l++;
      } else if (sortable.less(startInclusive, r)) {
        r--;
      } else {
        sortable.swap(l, r);
        l++;
        r--;
      }
    }
    // invariant after loop: (left - 1 = right)
    int newPivotIndex = l - 1;
    sortable.swap(startInclusive, newPivotIndex);
    int leftSize = newPivotIndex - startInclusive;
    if (leftSize == percentile) {
      return newPivotIndex;
    } else if (percentile < leftSize) {
      return select(percentile, startInclusive, newPivotIndex);
    } else {
      return select(percentile - leftSize - 1, newPivotIndex + 1, endExclusive);
    }
  }

  private int selectDouble(int percentile, final int startInclusive, final int endExclusive) {
    // pick two without replacement.
    int p1 = randomPivot(r, startInclusive, endExclusive);
    int p2 = randomPivot(r, startInclusive, endExclusive - 1);
    if (p2 >= p1) {
      p2++;
    }
    if (sortable.less(p2, p1)) {
      // wlog p1 <= p2.
      int tmp = p1;
      p1 = p2;
      p2 = tmp;
    }

    if (p2 == startInclusive && p1 == endExclusive - 1) {
      sortable.swap(p1, p2);
    } else if (p2 == startInclusive) {
      sortable.swap(p2, endExclusive - 1);
      sortable.swap(p1, startInclusive);
    } else {
      sortable.swap(p1, startInclusive);
      sortable.swap(p2, endExclusive - 1);
    }

    int l = startInclusive + 1; // next "less than p1" location.
    int r = endExclusive - 2; // next "greater than p2" location.
    int it = l;
    while (it <= r) {
      // TODO - handle duplicate values better.
      if (sortable.less(it, startInclusive)) {
        sortable.swap(it, l); // push to left.
        it++;
        l++;
      } else if (sortable.less(endExclusive - 1, it)) {
        sortable.swap(it, r); // push to right.
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
    sortable.swap(startInclusive, newP1);
    sortable.swap(endExclusive - 1, newP2);
    int leftSize = newP1 - startInclusive;
    int middleSize = newP2 - newP1 - 1;
    if (percentile < leftSize) {
      return select(percentile, startInclusive, newP1);
    } else if (percentile == leftSize) {
      return newP1;
    }
    int middlePercentile = percentile - leftSize - 1;
    if (middlePercentile < middleSize) {
      if (!sortable.less(newP1, newP2) && !sortable.less(newP2, newP1)) {
        // if p1 = p2, then no need to traverse to the middle.
        return newP2;
      }
      return select(middlePercentile, newP1 + 1, newP2);
    } else if (middlePercentile == middleSize) {
      return newP2;
    }
    int lastPercentile = middlePercentile - middleSize - 1;
    return select(lastPercentile, newP2 + 1, endExclusive);
  }

  /**
   * Find a pivot.
   */
  private int pivot(int startInclusive, int endExclusive) {
    debug("pivot [%d,%d)", startInclusive, endExclusive);
    if (algorithm == Algorithm.MEDIAN_OF_MEDIAN) {
      return medianOfMedian(startInclusive, endExclusive);
    } else {
      return randomPivot(r, startInclusive, endExclusive);
    }
  }

  private int medianOfMedian(int startInclusive, int endExclusive) {
    int length = endExclusive - startInclusive;
    if (length <= 5) {
      naiveMedian(sortable, startInclusive, endExclusive, startInclusive);
    } else {
      int toStore = startInclusive;

      for (int rangeStart = startInclusive; rangeStart < endExclusive; rangeStart += 5) {
        int rangeEnd = Math.min(startInclusive + 5, endExclusive);
        naiveMedian(sortable, rangeStart, rangeEnd, toStore);
        toStore++;
      }
      select(length / 10, startInclusive, toStore);
    }
    return startInclusive;
  }

  /**
   * Find the median via sorting and swap it with {@code storeTo}.
   */
  private static void naiveMedian(Sortable sortable, int start, int end, int storeTo) {
    // naive short-circuiting insertion singlePivotSort.
    int toIndex = (end + start) / 2;
    for (int i = start; i <= toIndex; i++) {
      for (int j = i + 1; j < end; j++) {
        if (sortable.less(j, i)) {
          sortable.swap(i, j);
        }
      }
    }
    sortable.swap(storeTo, toIndex);
  }

  /**
   * Prints a debug message.
   */
  private static void debug(String s, Object... args) {
    if (DEBUG) {
      System.err.printf(s, args);
      System.err.print('\n');
    }
  }
}
