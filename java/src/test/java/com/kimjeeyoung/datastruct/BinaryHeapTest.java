package com.kimjeeyoung.datastruct;

import java.util.Comparator;

public class BinaryHeapTest extends AbstractHeapTest {
  @Override
  <K> Heap<K> newHeap() {
    return new BinaryHeap<>();
  }

  @Override
  <K> Heap<K> newHeap(Comparator<K> comparator) {
    return new BinaryHeap<>(comparator);
  }

  @Override
  void validate(Heap<?> heap) {
    ((BinaryHeap<?>) heap).validateState();
  }
}