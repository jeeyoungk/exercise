package com.kimjeeyoung.algo.sorting;

import java.util.List;

class ListSortable<T extends Comparable<T>> implements Sortable {
  private final List<T> list;

  public ListSortable(List<T> list) {
    this.list = list;
  }

  @Override
  public boolean less(int i, int j) {
    return list.get(i).compareTo(list.get(j)) < 0;
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
