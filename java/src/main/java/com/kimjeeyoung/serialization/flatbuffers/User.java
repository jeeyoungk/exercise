// automatically generated, do not modify

package com.kimjeeyoung.serialization.flatbuffers;

import java.nio.*;
import java.lang.*;
import java.util.*;
import com.google.flatbuffers.*;

@SuppressWarnings("unused")
public final class User extends Table {
  public static User getRootAsUser(ByteBuffer _bb) { return getRootAsUser(_bb, new User()); }
  public static User getRootAsUser(ByteBuffer _bb, User obj) { _bb.order(ByteOrder.LITTLE_ENDIAN); return (obj.__init(_bb.getInt(_bb.position()) + _bb.position(), _bb)); }
  public User __init(int _i, ByteBuffer _bb) { bb_pos = _i; bb = _bb; return this; }

  public Point pos() { return pos(new Point()); }
  public Point pos(Point obj) { int o = __offset(4); return o != 0 ? obj.__init(o + bb_pos, bb) : null; }
  public String name() { int o = __offset(6); return o != 0 ? __string(o + bb_pos) : null; }
  public ByteBuffer nameAsByteBuffer() { return __vector_as_bytebuffer(6, 1); }

  public static void startUser(FlatBufferBuilder builder) { builder.startObject(2); }
  public static void addPos(FlatBufferBuilder builder, int posOffset) { builder.addStruct(0, posOffset, 0); }
  public static void addName(FlatBufferBuilder builder, int nameOffset) { builder.addOffset(1, nameOffset, 0); }
  public static int endUser(FlatBufferBuilder builder) {
    int o = builder.endObject();
    return o;
  }
  public static void finishUserBuffer(FlatBufferBuilder builder, int offset) { builder.finish(offset); }
};

