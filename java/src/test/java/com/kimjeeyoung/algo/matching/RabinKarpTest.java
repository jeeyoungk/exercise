package com.kimjeeyoung.algo.matching;

public class RabinKarpTest extends StringMatchingTest {

  @Override
  protected int matches(byte[] input, byte[] pattern) {
    return RabinKarp.matches(input, pattern);
  }
}