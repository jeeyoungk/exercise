package com.kimjeeyoung.algo;

import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.List;

public interface Sortable {
  static Sortable fromInt(int[] array) {
    return new IntegerSortable(array);
  }

  static <T extends Comparable<T>> Sortable fromList(List<T> list) {
    return new ListSortable<>(list);
  }

  int compare(int i, int j);

  int size();

  void swap(int i, int j);

  Sortable sub(int i, int j);

  Object get(int i);

  class IntegerSortable implements Sortable {
    private final int[] a;
    private final int begin;
    private final int end;

    public IntegerSortable(int[] a) {
      this.a = a;
      this.begin = 0;
      this.end = a.length;
    }

    public IntegerSortable(int[] a, int begin, int end) {
      this.a = a;
      this.begin = begin;
      this.end = end;
    }

    @Override
    public int compare(int i, int j) {
      bound(i);
      bound(j);
      return Integer.compare(a[i], a[j]);
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
      return new IntegerSortable(a, this.begin + i, this.begin + j);
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

    void bound(int i) {
      Preconditions.checkArgument(begin <= i && i < end);
    }
  }

  class ListSortable<T extends Comparable<T>> implements Sortable {
    private final List<T> list;

    public ListSortable(List<T> list) {
      this.list = list;
    }

    @Override
    public int compare(int i, int j) {
      return list.get(i).compareTo(list.get(j));
    }

    @Override
    public int size() {
      return list.size();
    }

    @Override
    public void swap(int i, int j) {
      T tmp = list.get(i);
      list.set(i, list.get(j));
      list.set(j, tmp);
    }

    @Override
    public Sortable sub(int i, int j) {
      return new ListSortable<>(list.subList(i, j));
    }

    @Override
    public Object get(int i) {
      return list.get(i);
    }
  }
}
