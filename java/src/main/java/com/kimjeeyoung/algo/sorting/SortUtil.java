package com.kimjeeyoung.algo.sorting;

import java.util.Random;

class SortUtil {
  static int randomPivot(Random r, int startInclusive, int endExclusive) {
    return startInclusive + r.nextInt(endExclusive - startInclusive);
  }
}
