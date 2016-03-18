// Copyright 2015 Square, Inc.
package com.kimjeeyoung.paradigm;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

/**
 * Created by jee on 3/16/16.
 */
public class RxJava {
  public static void main(String... args) {
    Subscription subscription = hello("ben", "george");
  }

  public static Subscription hello(String... names) {
    return Observable.from(names).subscribe(s -> {
      System.out.println("Hello " + s + " !");
    });
  }
}
