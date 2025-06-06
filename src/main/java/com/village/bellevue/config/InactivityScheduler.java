package com.village.bellevue.config;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.service.ActivityService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Transactional("asyncTransactionManager")
public class InactivityScheduler {
  private final ActivityService activityService;

  @Scheduled(fixedRate = 60000) // every 60 seconds
  public void markInactiveUsers() {
    Instant now = Instant.now();
    Instant fiveMinutesAgoInstant = now.minus(Duration.ofMinutes(5));
    Timestamp fiveMinutesAgo = Timestamp.from(fiveMinutesAgoInstant);
    activityService.markUsersIdle(fiveMinutesAgo);
  }
}
