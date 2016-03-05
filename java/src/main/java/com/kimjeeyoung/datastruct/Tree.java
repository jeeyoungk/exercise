// Copyright 2015 Square, Inc.
package com.kimjeeyoung.datastruct;

import java.util.function.BiConsumer;

public interface Tree<K, V> {
  void add(K key, V value);

  boolean contains(K key);

  V remove(K key);

  V get(K key);

  void iterate(BiConsumer<K, V> consumer);
}
