// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.Comparator;
import java.util.NoSuchElementException;

import static com.kimjeeyoung.datastruct.ComparatorUtil.compareTo;
import static com.kimjeeyoung.datastruct.ComparatorUtil.unsupportedHeapNode;

public class BinaryHeap<T> implements Heap<T> {
  private final Comparator<? super T> comparator;
  private T[] heap;
  private int size = 0;

  @SuppressWarnings("unchecked")
  public BinaryHeap(Comparator<? super T> comparator) {
    this.comparator = comparator;
    this.heap = (T[]) new Object[1024];
  }

  @Override
  public HeapNode insert(T t) throws IllegalStateException {
    if (size + 1 >= heap.length) {
      throw new IllegalStateException();
    }
    int index = size;
    heap[index] = t;
    size++;
    while (index != 0) {
      if (compareTo(comparator, heap[index], heap[parent(index)]) > 0) {
        T temp = heap[index];
        heap[index] = heap[parent(index)];
        heap[parent(index)] = temp;
      } else {
        break;
      }
    }
    return unsupportedHeapNode();
  }

  @Override
  public T getMinimum() throws NoSuchElementException {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    return heap[0];
  }

  @Override
  public T popMinimum() throws NoSuchElementException {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    T value = heap[0];
    int index = 0;
    while (true) {
      heap[index] = null;
      int left = left(index);
      int right = right(index);
      if (left >= size) {
        break; // terminal condition - both left & right children do not exist.
      } else if (right >= size) {
        heap[index] = heap[right];
      } else {
        if (compareTo(comparator, heap[left], heap[right]) < 0) {
          heap[index] = heap[left];
          index = left;
        } else {
          heap[index] = heap[right];
          index = right;
        }
      }
    }
    if (index != size - 1) {
      heap[index] = heap[size - 1];
    }
    size--;
    return value;
  }

  private static int parent(int index) {
    return (index - 1) / 2;
  }

  private static int left(int index) {
    return index * 2 + 1;
  }

  private static int right(int index) {
    return index * 2 + 2;
  }
}
