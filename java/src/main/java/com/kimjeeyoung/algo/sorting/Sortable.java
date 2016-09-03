package com.kimjeeyoung.algo.sorting;

import java.util.List;

public interface Sortable {
  static Sortable from(int[] array) {
    return new IntegerSortable(array);
  }

  static <T extends Comparable<T>> Sortable from(List<T> list) {
    return new ListSortable<>(list);
  }

  static InstrumentedSortable instrument(Sortable sortable) {
    return new InstrumentedSortable(sortable, new Instrumentation());
  }

  /**
   * True if {@code sortable[i] < sortable[j]}.
   */
  boolean less(int i, int j);

  /**
   * Size of sortable. All elements within {@code [0, size())} are valid.
   */
  int size();

  /**
   * Swap two elements.
   */
  void swap(int i, int j);

  /**
   * returns sub-sortable. Useful for debugging.
   */
  Sortable sub(int i, int j);

  /**
   * Useful for debugging.
   */
  Object get(int i);

  class InstrumentedSortable implements Sortable {
    private final Sortable inner;
    private final Instrumentation counter;

    InstrumentedSortable(Sortable inner, Instrumentation counter) {
      this.inner = inner;
      this.counter = counter;
    }

    @Override
    public boolean less(int i, int j) {
      counter.compare++;
      return inner.less(i, j);
    }

    @Override
    public int size() {
      return inner.size();
    }

    @Override
    public void swap(int i, int j) {
      counter.swap++;
      inner.swap(i, j);
    }

    @Override
    public Sortable sub(int i, int j) {
      return new InstrumentedSortable(inner.sub(i, j), counter);
    }

    @Override
    public Object get(int i) {
      return inner.get(i);
    }

    public Instrumentation getInstrumentation() {
      return counter;
    }

    @Override
    public String toString() {
      return inner.toString();
    }
  }
}
