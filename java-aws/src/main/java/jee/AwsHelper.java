package jee;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.AmazonKinesisClientBuilder;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import org.apache.commons.codec.Charsets;

import java.io.File;
import java.io.FileReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class AwsHelper {
  public static final String ACCESS_KEY_ID = "ACCESS_KEY_ID";
  public static final String SECRET_ACCESS_KEY = "SECRET_ACCESS_KEY";
  public static final String STREAM_NAME = "test-stream";


  public static AmazonKinesis newClient() {
    AWSCredentials creds = getCredential();
    AmazonKinesisClientBuilder b = AmazonKinesisClientBuilder.standard();
    b.setCredentials(new AWSStaticCredentialsProvider(creds));
    return b.build();
  }

  public static void main(String[] args) throws Exception {
    /*
    // client.createStream("test-stream", 1);
    PutRecordRequest request = new PutRecordRequest();
    request.setStreamName(STREAM_NAME);
    ByteBuffer buffer = ByteBuffer.wrap("abc".getBytes(Charsets.UTF_8));
    request.setData(buffer);
    request.setPartitionKey("abc");
    client.putRecord(request);
    */
  }

  private static AWSCredentials getCredential() {
    try {
      File f = new File("/Users/jeeyoungk/.awskey");
      FileReader reader = new FileReader(f);
      Scanner s = new Scanner(reader);
      Map<String, String> configs = new HashMap<String, String>();

      while (s.hasNextLine()) {
        String line = s.nextLine();
        String[] tokenized = line.split("=", 2);
        String key = tokenized[0];
        String value = tokenized[1];
        configs.put(key, value);
      }

      return new BasicAWSCredentials(
        configs.get(ACCESS_KEY_ID),
        configs.get(SECRET_ACCESS_KEY)
      );
    } catch (Exception e) {
      return null;
    }
  }
}
