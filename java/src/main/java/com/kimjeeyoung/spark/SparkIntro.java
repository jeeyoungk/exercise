// Copyright 2015 Square, Inc.
package com.kimjeeyoung.spark;


import java.util.Arrays;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import scala.Tuple2;

public class SparkIntro {
  private static final FlatMapFunction<String, String> WORDS_EXTRACTOR = s -> Arrays.asList(s.split(" "));

  private static final PairFunction<String, String, Integer> WORDS_MAPPER = s -> new Tuple2<>(s, 1);

  private static final Function2<Integer, Integer, Integer> WORDS_REDUCER = (a, b) -> a + b;

  public static void main(String[] args) {
    SparkConf conf = new SparkConf().setAppName("com.kimjeeyoung.spark.SparkIntro").setMaster("local");
    JavaSparkContext context = new JavaSparkContext(conf);

    JavaRDD<String> file = context.textFile("input.txt");
    JavaRDD<String> words = file.flatMap(WORDS_EXTRACTOR);
    JavaPairRDD<String, Integer> pairs = words.mapToPair(WORDS_MAPPER);
    JavaPairRDD<String, Integer> counter = pairs.reduceByKey(WORDS_REDUCER);

    counter.saveAsTextFile("output.txt");
  }
}
