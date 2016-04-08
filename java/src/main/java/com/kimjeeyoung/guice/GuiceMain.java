// Copyright 2015 Square, Inc.
package com.kimjeeyoung.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import javax.inject.Inject;
import javax.inject.Singleton;

public class GuiceMain {
  public static void main(String[] args) {
    Injector injectorA = Guice.createInjector(
        new SampleModule(),
        new ConfigModule("world")
    );
    Injector injectorB = Guice.createInjector(
        new SampleModule(),
        new ConfigModule("jack dorsey")
    );

    injectorA.getInstance(App.class).run();
    injectorB.getInstance(App.class).run();
  }

  static class SampleModule extends AbstractModule {
    @Override
    protected void configure() {
      bind(App.class).to(AppImpl.class).in(Singleton.class);
    }
  }

  static class ConfigModule extends AbstractModule {
    private final String firstword;

    public ConfigModule(String firstword) {
      this.firstword = firstword;
    }

    @Override
    protected void configure() {
      bind(Key.get(String.class, Names.named("firstword"))).toInstance(firstword);
    }

    @Provides
    @Named("secondword")
    public String provideSecondWord() {
      return "world";
    }
  }

  interface App {
    void run();
  }

  static class AppImpl implements App {
    private final String firstWord;
    private final String secondWord;

    @Inject
    public AppImpl(@Named("firstword") String firstWord, @Named("secondword") String secondWord) {
      this.secondWord = secondWord;
      this.firstWord = firstWord;
    }

    @Override
    public void run() {
      System.out.println(firstWord + " " + secondWord);
    }
  }

  static class AppImpl2 implements App {
    @Override
    public void run() {

    }
  }
}