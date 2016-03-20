package com.kimjeeyoung.avro;

import com.google.common.base.Charsets;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;

import java.io.*;
import java.util.Arrays;

import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.SchemaValidationException;
import org.apache.avro.SchemaValidatorBuilder;
import org.apache.avro.file.DataFileReader;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.*;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class AvroExperiment {
  private Schema schema;
  private Schema user;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

  @Before
  public void setUp() throws Exception {
    File file = new File("src/main/avro/example.avsc");
    schema = new Schema.Parser().parse(file);
    user = schema.getTypes().stream().filter((x) -> x.getName().equals("User")).findFirst().get();
  }

  /**
   * simple interaction using the compiled schema.
   *
   * @throws Exception
   */
  @Test
  public void testSimple() throws Exception {
    User user = new User();
    user.setFavoriteColor("red");
    user.setFavoriteNumber(10);
    user.setName("jeeyoung kim");
  }

  @Test
  public void testSchema() {
    assertEquals(schema.getType(), Schema.Type.UNION);
    assertEquals(user.getType(), Schema.Type.RECORD);
  }

  @Test
  public void testSchemaEvolution() {
    assertOnlyForward(TypeB.getClassSchema(), TypeA.getClassSchema());
    assertOnlyForward(TypeA.getClassSchema(), TypeANoDefault.getClassSchema());
    assertBothWays(TypeANoDefault.getClassSchema(), TypeAWithNewField.getClassSchema());
    assertBothWays(TypeA.getClassSchema(), TypeAWithNewField.getClassSchema());
    // why is this the case?
    assertBothWays(TypeB.getClassSchema(), TypeBShuffle.getClassSchema());
    assertIncompatible(TypeA.getClassSchema(), TypeC.getClassSchema());
  }

  @Test
  public void testParsingUnionShuffle() {
    TypeB b1 = new TypeB();
    TypeBShuffle b2 = new TypeBShuffle();
    b1.setValue("foo");
    b2.setValue("foo");
    byte[] b1Encoded = toByte(b1);
    byte[] b2Encoded = toByte(b2);
    assertFalse(Arrays.equals(b1Encoded, b2Encoded));
    GenericRecord b2Decoded = readWithDifferentSchema(b2.getSchema(), b1.getSchema(), b2Encoded);
    GenericRecord b1Decoded = readWithDifferentSchema(b1.getSchema(), b2.getSchema(), b1Encoded);
    assertEquals(b2Decoded.get("value"), b1Decoded.get("value"));
  }

  /**
   * Test the object sizes
   */
  @Test
  public void testObjectSize() {
    assertEquals("union type header + size byte + 0 bytes", 2, toByte(UnionRecord.newBuilder().setField("").build()).length);
    assertEquals("union type header + size byte + 3 bytes", 5, toByte(UnionRecord.newBuilder().setField("foo").build()).length);
    assertEquals("union type header + 1 byte", 2, toByte(UnionRecord.newBuilder().setField(0).build()).length);
    // Avro uses variable-length zig-zg encoding for integers - so they can take up to 5 (instead of 4) bytes.
    assertEquals("union type header + 1 byte", 2, toByte(UnionRecord.newBuilder().setField(1 << 7 - 2).build()).length);
    assertEquals("union type header + 2 byte", 3, toByte(UnionRecord.newBuilder().setField(1 << 7 - 1).build()).length);
    assertEquals("union type header + 2 byte", 3, toByte(UnionRecord.newBuilder().setField(1 << 15 - 3).build()).length);
    assertEquals("union type header + 3 byte", 4, toByte(UnionRecord.newBuilder().setField(1 << 15 - 2).build()).length);
    assertEquals("union type header + 3 byte", 4, toByte(UnionRecord.newBuilder().setField(1 << 23 - 4).build()).length);
    assertEquals("union type header + 4 byte", 5, toByte(UnionRecord.newBuilder().setField(1 << 23 - 3).build()).length);
    assertEquals("union type header + 4 byte", 5, toByte(UnionRecord.newBuilder().setField(1 << 31 - 5).build()).length);
    assertEquals("union type header + 5 byte", 6, toByte(UnionRecord.newBuilder().setField(1 << 31 - 4).build()).length);
    assertEquals("union type header + 5 byte", 6, toByte(UnionRecord.newBuilder().setField(Integer.MAX_VALUE).build()).length);
    // null is encoded as 0 byte, but type header uses 1 byte.
    assertEquals("union type header + null (0 bytes)", 1, toByte(UnionRecord.newBuilder().setField(null).build()).length);
    assertEquals("union type header + size byte + 4 bytes + null block", 7, toByte(UnionRecord.newBuilder().setField(Arrays.asList(true, true, false, true)).build()).length);
    // without type headers - almost as efficient as c-structs!
    assertEquals(2, toByte(Point.newBuilder().setX(0).setY(0).build()).length);
    assertEquals(4, toByte(Point.newBuilder().setX(1 << 7 - 1).setY(1 << 7 - 1).build()).length);
    assertEquals(10, toByte(Point.newBuilder().setX(Integer.MAX_VALUE).setY(Integer.MAX_VALUE).build()).length);
  }

  @Test
  public void testParsingSchemaEvolution() {
    TypeA a = new TypeA();
    a.setValue("XXX");
    byte[] encoded = toByte(a);
    GenericRecord decoded = readWithDifferentSchema(TypeA.getClassSchema(), TypeAWithNewField.getClassSchema(), encoded);
    assertEquals(decoded.get("value").toString(), "XXX");
    assertEquals(decoded.get("new_value").toString(), "default-value");
  }

  /**
   * Write to a file and read back.
   */
  @Test
  public void testWriteRead() throws Exception {
    ImmutableList<User> users = ImmutableList.of(
      makeUser("john", "red", 13),
      makeUser("amy", "blue", 14),
      makeUser("mark", "pink", 15)
    );
    File testOutput = folder.newFile();
    DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
    DataFileWriter<GenericRecord> fileWriter = new DataFileWriter<>(datumWriter);

    fileWriter.create(schema, testOutput);
    for (User user : users) {
      fileWriter.append(user);
    }
    fileWriter.close();

    GenericDatumReader<User> datumReader = new SpecificDatumReader<>();
    DataFileReader<User> fileReader = new DataFileReader<>(testOutput, datumReader);
    User read = null;
    int count = 0;
    while (fileReader.hasNext()) {
      read = fileReader.next(read);
      assertEquals(users.get(count), read);
      count++;
    }
  }

  /**
   * Sample interaction of {@link GenericRecord} without a compiled schema.
   */
  @Test
  public void testWithoutSchema() {
    GenericRecord record = new GenericData.Record(user);
    record.put("name", "jeeyoung kim");
    record.put("favorite_number", 10);
    record.put("favorite_color", "red");
    assertEquals("{\"name\": \"jeeyoung kim\", \"favorite_number\": 10, \"favorite_color\": \"red\"}", record.toString());
  }

  @Test
  public void testJson() throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    GenericRecord inputRow = new GenericData.Record(user);
    inputRow.put("name", "jeeyoung kim");
    inputRow.put("favorite_number", 10);
    inputRow.put("favorite_color", "red");
    JsonEncoder encoder = EncoderFactory.get().jsonEncoder(user, os, false);
    DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(inputRow.getSchema());
    datumWriter.write(inputRow, encoder);
    encoder.flush();
    os.close();
    // union types are not represented well in Avro.
    assertEquals("{\"name\":\"jeeyoung kim\",\"favorite_number\":{\"int\":10},\"favorite_color\":{\"string\":\"red\"}}",
      new String(os.toByteArray(), Charsets.UTF_8));
    JsonDecoder decoder = DecoderFactory.get().jsonDecoder(user,
      new DataInputStream(new ByteArrayInputStream(os.toByteArray())));
    DatumReader<GenericRecord> reader = new GenericDatumReader<>(user);
    GenericRecord deserializedRow = reader.read(null, decoder);
    assertEquals(deserializedRow, inputRow);
  }


  /**
   * Invalid operation.
   */
  @Test
  public void testWithoutSchema_invalid() throws Exception {
    GenericRecord record = new GenericData.Record(user);
    try {
      record.put("name_does_not_exist", "jeeyoung kim");
      fail("putting a field that doesn't exist should fail.");
    } catch (AvroRuntimeException e) {
      assertEquals(e.getMessage(), "Not a valid schema field: name_does_not_exist");
    }
  }

  public static User makeUser(String name, String color, int number) {
    return User.newBuilder().setName(name).setFavoriteColor(color).setFavoriteNumber(number).build();
  }

  public void assertOnlyForward(Schema writer, Schema reader) {
    try {
      new SchemaValidatorBuilder().canReadStrategy().validateAll().validate(
        reader, ImmutableList.of(writer));
      fail("Expected to be incompatible, but are compatible");
    } catch (SchemaValidationException e) {
      /* expected */
    }

    try {
      new SchemaValidatorBuilder().canBeReadStrategy().validateAll().validate(
        reader, ImmutableList.of(writer));
    } catch (SchemaValidationException e) {
      fail(e.getMessage());
    }
  }

  public void assertBothWays(Schema from, Schema to) {
    try {
      new SchemaValidatorBuilder().mutualReadStrategy().validateAll().validate(
        to, ImmutableList.of(from));
    } catch (SchemaValidationException e) {
      fail(e.getMessage());
    }
  }

  private void assertIncompatible(Schema schemaA, Schema schemaB) {
    try {
      new SchemaValidatorBuilder().canReadStrategy().validateAll().validate(
        schemaA, ImmutableList.of(schemaB));
      fail("Expected to be incompatible, but are compatible");
    } catch (SchemaValidationException e) {
      /* expected */
    }

    try {
      new SchemaValidatorBuilder().canReadStrategy().validateAll().validate(
        schemaA, ImmutableList.of(schemaB));
      fail("Expected to be incompatible, but are compatible");
    } catch (SchemaValidationException e) {
      /* expected */
    }
  }

  /**
   * Convert the given generic record to bytes.
   */
  private byte[] toByte(GenericRecord record) {
    try {
      DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(record.getSchema());
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      datumWriter.write(record, new EncoderFactory().directBinaryEncoder(bos, null));
      bos.close();
      return bos.toByteArray();
    } catch (IOException e) {
      fail(e.getMessage());
      throw com.google.common.base.Throwables.propagate(e);
    }
  }

  /**
   * Read a data that is written in a different schema.
   */
  private GenericRecord readWithDifferentSchema(Schema writerSchema, Schema readerSchema, byte[] array) {
    try {
      DatumReader<GenericRecord> datumReader = new GenericDatumReader<>(readerSchema);
      return datumReader.read(null, new DecoderFactory().resolvingDecoder(writerSchema, readerSchema, new DecoderFactory().binaryDecoder(array, null)));
    } catch (IOException e) {
      fail(e.toString());
      throw Throwables.propagate(e); // unreachable
    }
  }
}
