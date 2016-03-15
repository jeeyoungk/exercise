package com.kimjeeyoung.serialization.flatbuffers;

import com.google.flatbuffers.FlatBufferBuilder;
import java.nio.ByteBuffer;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Example working with FlatBuffers
 */
public class FlatBuffersTest {

  @Test
  public void test() {
    FlatBufferBuilder builder = new FlatBufferBuilder();
    int stringOffset = builder.createString("test");
    int pointOffset = Point.createPoint(builder, 10, 3);
    User.startUser(builder);
    User.addPos(builder, pointOffset);
    User.addName(builder, stringOffset);
    builder.finish(User.endUser(builder));
    byte[] array = builder.sizedByteArray();
    assertEquals(40, array.length);
    User decoded = User.getRootAsUser(ByteBuffer.wrap(array));
    assertEquals("test", decoded.name());
    assertEquals(10, decoded.pos().x(), 0.001);
    assertEquals(3, decoded.pos().y(), 0.001);
  }
}