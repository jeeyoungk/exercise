package com.kimjeeyoung.serialization.avro;

import com.google.common.collect.ImmutableList;
import com.kimjeeyoung.avro.User;
import java.io.ByteArrayOutputStream;
import java.io.File;
import org.apache.avro.AvroRuntimeException;
import org.apache.avro.Schema;
import org.apache.avro.file.DataFileWriter;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;

public class AvroExperiment {
  private final Schema schema;
  private final Schema user;

  public AvroExperiment(Schema schema) {
    this.schema = schema;
    user = schema.getTypes().get(0);
  }

  public static void main(String... args) throws Exception {
    File file = new File("src/main/avro/example.avsc");
    Schema schema = new Schema.Parser().parse(file);
    AvroExperiment experiment = new AvroExperiment(schema);
    experiment.testSimple();
    experiment.testBinary();
    experiment.testSchema();
    experiment.testWithoutSchema();
    experiment.testWithoutSchema_invalid();
  }

  private void testSchema() {
    System.out.println("> testing schema introspection");
    System.out.println(schema.getType()); // array of types become union
  }

  private void testSimple() throws Exception {
    User user = new User();
    user.setFavoriteColor("red");
    user.setFavoriteNumber(10);
    user.setName("jeeyoung kim");
    System.out.println("> Simple interaction with Avro,");
    System.out.println(user);
  }

  private void testBinary() throws Exception {
    ImmutableList<User> users = ImmutableList.of(
        makeUser("john", "red", 13),
        makeUser("amy", "blue", 14),
        makeUser("mark", "pink", 15)
    );
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    File testOutput = new File("output.avro");
    DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
    DataFileWriter<GenericRecord> fileWriter = new DataFileWriter<>(datumWriter);
    fileWriter.create(schema, testOutput);
    for (User user : users) {
      fileWriter.append(user);
    }
    fileWriter.close();
  }

  private void testWithoutSchema() throws Exception {
    System.out.println("> Simple with a dynamic schema.");
    GenericRecord record = new GenericData.Record(user);
    record.put("name", "jeeyoung kim");
    record.put("favorite_number", 10);
    record.put("favorite_color", "red");
    System.out.println(record.toString());
  }

  private void testWithoutSchema_invalid() throws Exception {
    System.out.println("> Simple with a dynamic schema, inserting an invalid data");
    GenericRecord record = new GenericData.Record(user);
    try {
      record.put("name_does_not_exist", "jeeyoung kim");
    } catch (AvroRuntimeException e) {
      e.printStackTrace(System.out);
    }
  }

  public static User makeUser(String name, String color, int number) {
    return User.newBuilder().setName(name).setFavoriteColor(color).setFavoriteNumber(number).build();
  }
}
