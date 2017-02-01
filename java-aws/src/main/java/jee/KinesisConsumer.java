package jee;

import com.amazonaws.services.kinesis.AmazonKinesis;
import com.amazonaws.services.kinesis.model.*;

import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by jeeyoungk on 1/31/17.
 */
public class KinesisConsumer {

  public static void main(String[] args) throws Exception {
    AmazonKinesis client = AwsHelper.newClient();
    Random r = new Random();
    GetShardIteratorRequest request = new GetShardIteratorRequest();
    request.setStreamName(AwsHelper.STREAM_NAME);
    request.setShardId("0");
    request.setShardIteratorType(ShardIteratorType.LATEST);
    GetShardIteratorResult iterator = client.getShardIterator(request);
    String token = iterator.getShardIterator();
    while (true) {
      System.out.println("Consuming...");
      GetRecordsRequest req = new GetRecordsRequest();
      req.setShardIterator(token);
      req.setLimit(1024);
      GetRecordsResult records = client.getRecords(req);
      // System.out.println(records.getRecords());
      token = records.getNextShardIterator();
      for (Record record : records.getRecords()) {
        int value = record.getData().getInt(0);
        System.out.println(value);
      }
      Thread.sleep(2000);
    }
  }
}
