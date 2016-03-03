package com.kimjeeyoung.serialization.avro;

import avro.shaded.com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.kimjeeyoung.avro.TypeA;
import com.kimjeeyoung.avro.TypeANoDefault;
import com.kimjeeyoung.avro.TypeAWithNewField;
import com.kimjeeyoung.avro.TypeB;
import com.kimjeeyoung.avro.TypeBShuffle;
import com.kimjeeyoung.avro.TypeC;
import com.kimjeeyoung.avro.User;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
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
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.junit.Assert;
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

  @Test
  public void testSchema() {
    assertEquals(schema.getType(), Schema.Type.UNION);
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

  @Test
  public void testParsingSchemaEvolution() {
    TypeA a = new TypeA();
    a.setValue("XXX");
    byte[] encoded = toByte(a);
    GenericRecord decoded = readWithDifferentSchema(TypeA.getClassSchema(), TypeAWithNewField.getClassSchema(), encoded);
    assertEquals(decoded.get("value").toString(), "XXX");
    assertEquals(decoded.get("new_value").toString(), "default-value");
  }

  @Test
  public void testSimple() throws Exception {
    User user = new User();
    user.setFavoriteColor("red");
    user.setFavoriteNumber(10);
    user.setName("jeeyoung kim");
  }

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

  @Test
  public void testWithoutSchema() throws Exception {
    GenericRecord record = new GenericData.Record(user);
    record.put("name", "jeeyoung kim");
    record.put("favorite_number", 10);
    record.put("favorite_color", "red");
    assertEquals("{\"name\": \"jeeyoung kim\", \"favorite_number\": 10, \"favorite_color\": \"red\"}", record.toString());
  }

  @Test
  public void testWithoutSchema_invalid() throws Exception {
    GenericRecord record = new GenericData.Record(user);
    try {
      record.put("name_does_not_exist", "jeeyoung kim");
      fail("putting a field that doesn't exist should fail.");
    } catch (AvroRuntimeException e) {
      /* expected. */
    }
  }

  public static User makeUser(String name, String color, int number) {
    return User.newBuilder().setName(name).setFavoriteColor(color).setFavoriteNumber(number).build();
  }

  public void assertOnlyForward(Schema writer, Schema reader) {
    try {
      new SchemaValidatorBuilder().canReadStrategy().validateAll().validate(
          reader, ImmutableList.of(writer));
      Assert.fail("Expected to be incompatible, but are compatible");
    } catch (SchemaValidationException e) {
      /* expected */
    }

    try {
      new SchemaValidatorBuilder().canBeReadStrategy().validateAll().validate(
          reader, ImmutableList.of(writer));
    } catch (SchemaValidationException e) {
      Assert.fail(e.getMessage());
    }
  }

  public void assertBothWays(Schema from, Schema to) {
    try {
      new SchemaValidatorBuilder().mutualReadStrategy().validateAll().validate(
          to, ImmutableList.of(from));
    } catch (SchemaValidationException e) {
      Assert.fail(e.getMessage());
    }
  }

  private void assertIncompatible(Schema schemaA, Schema schemaB) {
    try {
      new SchemaValidatorBuilder().canReadStrategy().validateAll().validate(
          schemaA, ImmutableList.of(schemaB));
      Assert.fail("Expected to be incompatible, but are compatible");
    } catch (SchemaValidationException e) {
      /* expected */
    }

    try {
      new SchemaValidatorBuilder().canReadStrategy().validateAll().validate(
          schemaA, ImmutableList.of(schemaB));
      Assert.fail("Expected to be incompatible, but are compatible");
    } catch (SchemaValidationException e) {
      /* expected */
    }
  }

  private byte[] toByte(GenericRecord record) {
    try {
      DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(record.getSchema());
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      datumWriter.write(record, new EncoderFactory().directBinaryEncoder(bos, null));
      bos.close();
      return bos.toByteArray();
    } catch (IOException e) {
      fail(e.getMessage());
      return null;
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
