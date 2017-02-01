package jee;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import org.apache.commons.codec.Charsets;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by jeeyoungk on 1/31/17.
 */
public class KinesisProducer {
  public static void main(String[] args) throws Exception {
    AmazonKinesis client = AwsHelper.newClient();
    ByteBuffer buffer = ByteBuffer.allocate(4);
    int i = 0;
    while (true) {
      System.out.println("Producing...");
      i++;
      PutRecordRequest request = new PutRecordRequest();
      request.setStreamName(AwsHelper.STREAM_NAME);
      System.out.println(i);
      buffer.putInt(0, i);
      request.setData(buffer);
      request.setPartitionKey("key");
      client.putRecord(request);
      Thread.sleep(500);
    }
  }
}
