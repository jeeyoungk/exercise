package com.kimjeeyoung.paradigm;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.function.Supplier;

/**
 * Created by jeeyoungk on 1/6/17.
 */
public class SupplierIterator<T> implements Iterator<T> {
  private final BlockingQueue<Wrapper<T>> queue = new SynchronousQueue<>();
  private final Queue<Wrapper<T>> residueQueue = new ArrayDeque<>();
  private final Thread t;

  public SupplierIterator(Supplier<T> supplier) {
    t = new Thread(() -> {
      try {
        while (true) {
          T object = supplier.get();
          if (object == null) {
            queue.put(new Wrapper<>(null, true));
            break;
          } else {
            queue.put(new Wrapper<>(object, false));
          }
        }
      } catch (InterruptedException e) {
        while (true) {
          try {
            queue.put(new Wrapper<>(null, true));
            break;
          } catch (InterruptedException e2) {
            // ignore the second interrupt.
          }
        }
        Thread.currentThread().interrupt();
      }
    });
    t.start();
  }

  @Override
  public boolean hasNext() {
    try {
      Wrapper<T> w = nextWrapper();
      residueQueue.add(w);
    } catch (NoSuchElementException e) {
      return false;
    }
    return true;
  }

  @Override
  public T next() {
    return nextWrapper().t;
  }

  private Wrapper<T> nextWrapper() {
    while (true) {
      Wrapper<T> residue = residueQueue.poll();
      if (residue != null) {
        return residue;
      }
      try {
        Wrapper<T> obj = queue.take();
        if (obj.depleted) {
          throw new NoSuchElementException();
        }
        return obj;
      } catch (InterruptedException e) {
        e.printStackTrace();
        // Thread.currentThread().interrupt();
      }
    }
  }

  private final static class Wrapper<T> {
    final T t;
    final boolean depleted;

    private Wrapper(T t, boolean depleted) {
      this.t = t;
      this.depleted = depleted;
    }
  }

  public static void main(String[] args) {
    Supplier<Integer> s = new Supplier<Integer>() {
      int value;
      @Override
      public Integer get() {
        if (value > 1000) {
          return null;
        }
        return value++;
      }
    };
    SupplierIterator<Integer> si = new SupplierIterator<>(s);
    while(si.hasNext()) {
      System.out.println(si.next());
    }
  }
}
