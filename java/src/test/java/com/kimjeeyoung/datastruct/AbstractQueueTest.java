package com.kimjeeyoung.datastruct;

import java.util.NoSuchElementException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeNotNull;

public abstract class AbstractQueueTest {
  @Test
  public void testSimple() {
    Queue<Integer> queue = newQueue();
    queue.add(1);
    queue.add(2);
    queue.add(3);
    assertEquals(1, queue.pop().intValue());
    queue.add(4);
    assertEquals(2, queue.pop().intValue());
    assertEquals(3, queue.pop().intValue());
    assertEquals(4, queue.pop().intValue());
    queue.add(5);
    assertEquals(5, queue.pop().intValue());
  }

  @Test
  public void testPopInvalid() {
    Queue<Integer> queue = newQueue();
    for (int i = 0; i < 1024; i++) {
      queue.add(i);
    }
    for (int i = 0; i < 1024; i++) {
      assertEquals(Integer.valueOf(i), queue.pop());
    }

    expectThrowable(queue::pop, NoSuchElementException.class);
  }

  @Test
  public void test1024() {
    Queue<Integer> queue = newQueue();
    for (int i = 0; i < 1024; i++) {
      queue.add(i);
    }
    for (int i = 0; i < 1024; i++) {
      assertEquals(Integer.valueOf(i), queue.pop());
    }
  }

  @Test
  public void test1025() {
    Queue<Integer> queue = newBoundedQueue(1024);
    assumeNotNull(queue);
    for (int i = 0; i < 1024; i++) {
      queue.add(i);
    }
    expectThrowable(() -> queue.add(1025), IllegalStateException.class);
  }

  private void expectThrowable(Runnable runnable, Class<? extends Throwable> throwable) {
    boolean executed = false;
    try {
      runnable.run();
      executed = true;
    } catch (Throwable t) {
      if (!throwable.isInstance(t)) {
        fail(String.format("Incorrect throwable. expected:%s, got:%s", throwable.getName(), t.getClass().getName()));
      }
    }
    if (executed) {
      fail(String.format("Expected a throwable. expected:%s, got nothing.", throwable.getName()));
    }
  }

  /**
   * Create a new queue that can hold > 1024 elements.
   */
  protected abstract <T> Queue<T> newQueue();

  /**
   * Create a new queue that can hold maximum of {@code capacity} elements.
   *
   * Optional method - can return null.
   */
  protected abstract <T> Queue<T> newBoundedQueue(int capacity);
}