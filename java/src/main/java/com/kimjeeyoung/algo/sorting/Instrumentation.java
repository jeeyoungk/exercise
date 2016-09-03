package com.kimjeeyoung.algo.sorting;

/** Statistics for sorting. */
public class Instrumentation {
  int swap;
  int compare;

  public int getSwaps() {
    return swap;
  }

  public int getCompares() {
    return compare;
  }

  @Override
  public String toString() {
    return String.format("Instrumentation{swap=%d,compare=%d}", swap, compare);
  }
}
