// Copyright 2015 Square, Inc.
package com.kimjeeyoung.spark;


import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import scala.Tuple2;

public class SparkIntro {
  private static final FlatMapFunction<String, String> WORDS_EXTRACTOR = s -> Arrays.asList(s.split(" "));

  private static final PairFunction<String, String, Integer> WORDS_MAPPER = s -> new Tuple2<>(s, 1);

  private static final Function2<Integer, Integer, Integer> WORDS_REDUCER = (a, b) -> a + b;

  public static void main(String[] args) {
    streamingExample();
  }

  private static void streamingExample() {
    SparkConf conf = new SparkConf().setAppName(SparkIntro.class.getCanonicalName()).setMaster("local");
    JavaStreamingContext ctx = new JavaStreamingContext(conf, Durations.seconds(30));
    JavaReceiverInputDStream<String> lines = ctx.socketTextStream("localhost", 9999);
    JavaDStream<String> words = lines.flatMap((String x) -> Arrays.asList(x.split(" ")));
    JavaPairDStream<String, Integer> pairs = words.mapToPair((s) -> new Tuple2<>(s, 1));
    JavaPairDStream<String, Integer> wordCounts = pairs.reduceByKey((i1, i2) -> i1 + i2);
    wordCounts.print();
    ctx.awaitTermination();
  }

  public static void nonStreamingExample() {
    SparkConf conf = new SparkConf().setAppName(SparkIntro.class.getCanonicalName()).setMaster("local");
    JavaSparkContext context = new JavaSparkContext(conf);
    JavaRDD<String> file = context.textFile("input.txt");
    JavaRDD<String> words = file.flatMap(WORDS_EXTRACTOR);
    JavaPairRDD<String, Integer> pairs = words.mapToPair(WORDS_MAPPER);
    JavaPairRDD<String, Integer> counter = pairs.reduceByKey(WORDS_REDUCER);
    counter.saveAsTextFile("output.txt");
  }
}
