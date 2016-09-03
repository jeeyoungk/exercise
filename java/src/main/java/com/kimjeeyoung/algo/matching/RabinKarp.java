package com.kimjeeyoung.algo.matching;

import java.math.BigInteger;
import java.util.Random;

public class RabinKarp {
  private static final Random r = new Random(0);

  public static int matches(byte[] input, byte[] pattern) {
    if (pattern.length > input.length) {
      return 0;
    }
    long p = BigInteger.probablePrime(63, r).longValue();
    long b = r.nextInt(1 << 16);
    long bpow = 1;
    long patternHash = 0;
    long inputHash = 0;
    int count = 0;
    for (int i = 1; i < pattern.length; i++) {
      bpow = (bpow * b) % p;
    }
    for (int i = 0; i < pattern.length; i++) {
      patternHash = (patternHash * b + pattern[i]) % p;
      inputHash = (inputHash * b + input[i]) % p;
    }
    if (inputHash == patternHash && isSubarray(input, pattern, 0)) {
      count++;
    }
    for (int i = pattern.length; i < input.length; i++) {
      byte oldChar = input[i - pattern.length];
      byte newChar = input[i];
      inputHash = ((inputHash - oldChar * bpow) * b + newChar) % p;
      if (inputHash == patternHash && isSubarray(input, pattern, i - pattern.length + 1)) {
        count++;
      }
    }
    return count;
  }

  public static boolean isSubarray(byte[] input, byte[] pattern, int offset) {
    for (int i = 0; i < pattern.length; i++) {
      if (input[offset + i] != pattern[i]) {
        return false;
      }
    }
    return true;
  }

  public static void main(String[] args) {
  }
}
