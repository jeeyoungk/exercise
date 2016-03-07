// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.Arrays;
import java.util.Comparator;
import java.util.NoSuchElementException;

import static com.google.common.base.Preconditions.checkArgument;
import static com.kimjeeyoung.datastruct.ComparatorUtil.compareTo;
import static com.kimjeeyoung.datastruct.ComparatorUtil.unsupportedHeapNode;

public class BinaryHeap<T> implements Heap<T> {
  private final Comparator<? super T> comparator;
  private static final int DEFAULT_SIZE = 1024;
  private T[] heap;
  private int size = 0;

  @SuppressWarnings("unchecked")
  public BinaryHeap() {
    comparator = null;
    this.heap = (T[]) new Object[DEFAULT_SIZE];
  }

  @SuppressWarnings("unchecked")
  public BinaryHeap(Comparator<? super T> comparator) {
    this.comparator = comparator;
    this.heap = (T[]) new Object[DEFAULT_SIZE];
  }

  @Override
  public HeapNode insert(T t) throws IllegalStateException {
    if (size + 1 >= heap.length) {
      heap = Arrays.copyOf(heap, heap.length * 2);
    }
    int index = size;
    heap[index] = t;
    size++;
    while (index != 0) {
      if (compareTo(comparator, heap[index], heap[parent(index)]) < 0) {
        swap(index, parent(index));
        index = parent(index);
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
    T returnValue = heap[0];
    size--;
    heap[0] = heap[size];
    heap[size] = null;
    int index = 0;
    while (true) {
      int left = left(index);
      int right = right(index);
      if (left >= size) {
        break; // terminal condition - both left & right children do not exist.
      } else if (right >= size) {
        // terminal condition - only "left" remains.
        if (compareTo(comparator, heap[index], heap[left]) > 0) {
          swap(index, left);
        }
        break;
      } else {
        int parentLeft = compareTo(comparator, heap[index], heap[left]);
        int parentRight = compareTo(comparator, heap[index], heap[right]);
        int leftRight = compareTo(comparator, heap[left], heap[right]);
        if (parentLeft <= 0 && parentRight <= 0) {
          break;
        } else if ((parentLeft > 0 && leftRight >= 0) || parentLeft < 0 && parentRight >= 0) {
          swap(index, right);
          index = right;
        } else {
          swap(index, left);
          index = left;
        }
      }
    }
    return returnValue;
  }

  @Override
  public int size() {
    return size;
  }

  void validateState() {
    for (int i = 1; i < size; i++) {
      checkArgument(compareTo(comparator, heap[i], heap[parent(i)]) >= 0);
    }
  }

  private void swap(int i, int j) {
    T temp = heap[i];
    heap[i] = heap[j];
    heap[j] = temp;
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
