package com.kimjeeyoung.algo.matching;

import com.google.common.base.Charsets;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public abstract class StringMatchingTest {
  protected abstract int matches(byte[] input, byte[] pattern);

  protected int matches(String input, String pattern) {
    return matches(input.getBytes(Charsets.US_ASCII), pattern.getBytes(Charsets.US_ASCII));
  }

  @Test
  public void testSimple() {
    assertEquals(matches("x", "x"), 1);
    assertEquals(matches("x", "y"), 0);
    assertEquals(matches("foo", "foo"), 1);
    assertEquals(matches("foo", "bar"), 0);
    assertEquals(matches("ababa", "aba"), 2);
  }
}