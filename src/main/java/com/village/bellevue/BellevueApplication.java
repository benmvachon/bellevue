package com.village.bellevue;

import java.util.TimeZone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@EnableScheduling
public class BellevueApplication {

  public static void main(String[] args) {
    TimeZone.setDefault(TimeZone.getTimeZone("America/New_York"));
    SpringApplication.run(BellevueApplication.class, args);
  }
}
