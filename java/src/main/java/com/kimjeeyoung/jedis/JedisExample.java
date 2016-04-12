package com.kimjeeyoung.jedis;

import redis.clients.jedis.Jedis;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Random;

/**
 * Performance testing Jedis.
 */
public class JedisExample {
  private final Clock clock;
  private final Jedis jedis;
  private final Random random;

  public JedisExample() {
    clock = Clock.systemUTC();
    jedis = new Jedis("localhost");
    random = new Random(0);
  }

  public static void main(String[] args) {
    JedisExample example = new JedisExample();
    example.runInsertTest("Sequential insert (4 byte data)", (i) -> i, new byte[4]);
    example.runInsertTest("Random insert (4 byte data)", (i) -> example.random.nextLong(), new byte[4]);
    example.runInsertTest("Random insert (1024 byte data)", (i) -> example.random.nextLong(), new byte[1024]);
    example.runInsertTest("Random insert (4096 byte data)", (i) -> example.random.nextLong(), new byte[4096]);
    example.runInsertTest("Random insert (10240 byte data)", (i) -> example.random.nextLong(), new byte[10240]);
    // > Sequential insert (4 byte data)
    // 100000 iteration took  7057 ms.
    // > Random insert (4 byte data)
    // 100000 iteration took  5204 ms.
    // > Random insert (1024 byte data)
    // 100000 iteration took  5381 ms.
    // > Random insert (4096 byte data)
    // 100000 iteration took  5865 ms.
    // > Random insert (10240 byte data)
    // 100000 iteration took  8235 ms.
  }

  private void runInsertTest(String title, Generator generator, byte[] blob) {
    System.out.printf("> %s\n", title);
    random.nextBytes(blob);
    for (int iteration : new int[]{100000}) {
      jedis.flushAll();
      ByteBuffer bb = ByteBuffer.allocate(8);
      LongBuffer lb = bb.asLongBuffer();
      Duration duration = run(() -> {
        for (int i = 0; i < iteration; i++) {
          lb.put(0, generator.generate(i));
          jedis.set(bb.array(), blob);
        }
      });
      System.out.printf("%6d iteration took %5d ms.\n", iteration, duration.toMillis());
    }
  }

  private Duration run(Runnable runnable) {
    Instant start = clock.instant();
    runnable.run();
    Instant end = clock.instant();
    return Duration.between(start, end);
  }

  interface Generator {
    long generate(long index);
  }
}
