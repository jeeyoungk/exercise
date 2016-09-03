package com.kimjeeyoung.algo.sorting;

import com.google.common.base.Preconditions;

class IntegerSortable implements Sortable {
  private final int[] a;
  private final int begin;
  private final int end;

  public IntegerSortable(int[] a) {
    this.a = a;
    this.begin = 0;
    this.end = a.length;
  }

  private IntegerSortable(int[] a, int begin, int end) {
    this.a = a;
    this.begin = begin;
    this.end = end;
  }

  @Override
  public boolean less(int i, int j) {
    bound(i);
    bound(j);
    return a[i] < a[j];
  }

  @Override
  public int size() {
    return end - begin;
  }

  @Override
  public void swap(int i, int j) {
    bound(i);
    bound(j);
    int tmp = a[i];
    a[i] = a[j];
    a[j] = tmp;
  }

  @Override
  public Sortable sub(int i, int j) {
    bound(i);
    Preconditions.checkArgument(j <= end);
    Preconditions.checkArgument(i <= j);
    return new com.kimjeeyoung.algo.sorting.IntegerSortable(a, this.begin + i, this.begin + j);
  }

  @Override
  public Object get(int i) {
    bound(i);
    return a[i];
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('[');
    for (int i = begin; i < end; i++) {
      if (i != begin) {
        sb.append(", ");
      }
      sb.append(a[i]);
    }
    sb.append(']');
    return sb.toString();
  }

  private void bound(int i) {
    if (!(begin <= i && i < end)) {
      throw new IndexOutOfBoundsException();
    }
  }
}
