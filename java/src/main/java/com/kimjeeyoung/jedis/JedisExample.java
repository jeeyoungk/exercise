package com.kimjeeyoung.jedis;

import redis.clients.jedis.Jedis;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;

/**
 * Created by jeeyoungk on 4/3/16.
 */
public class JedisExample {
  public static void main(String[] args) {
    Jedis jedis = new Jedis("localhost");
    ByteBuffer bb = ByteBuffer.allocate(8);
    LongBuffer lb = bb.asLongBuffer();
    for (int i = 0; i < 1000000; i++) {
      lb.put(0, i);
      jedis.set(bb.array(), bb.array());
    }
  }
}
