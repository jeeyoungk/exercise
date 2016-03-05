// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.Comparator;

final class ComparatorUtil {
  private static final Heap.HeapNode<Object> UNSUPPORTED_HEAP_NODE = o -> {
    throw new UnsupportedOperationException();
  };

  @SuppressWarnings("unchecked")
  public static <T> Heap.HeapNode<T> unsupportedHeapNode() {
    return (Heap.HeapNode<T>) UNSUPPORTED_HEAP_NODE;
  }

  public static <T> int compareTo(Comparator<? super T> comparator, T lhs, T rhs) throws ClassCastException {
    if (comparator != null) {
      return comparator.compare(lhs, rhs);
    }
    @SuppressWarnings("unchecked")
    Comparable<T> lhsCasted = (Comparable<T>) lhs;
    return lhsCasted.compareTo(rhs);
  }
}
