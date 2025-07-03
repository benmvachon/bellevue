package com.village.bellevue.config;

import java.sql.Timestamp;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Value;
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
  private final InactivityProperties inactivityProperties;

  @Value("${server.servlet.session.timeout}")
  private int sessionTimeoutSeconds;

  // Run every (inactivity.timeout / 2) seconds
  @Scheduled(fixedRateString = "#{${inactivity.timeout} * 500}") // half of inactivity.timeout in ms
  public void markInactiveUsers() {
    int timeoutSeconds = inactivityProperties.getTimeout();
    Instant threshold = Instant.now().minusSeconds(timeoutSeconds);
    activityService.markUsersIdle(Timestamp.from(threshold));
  }

  // Run every (session timeout / 10) seconds
  @Scheduled(fixedRateString = "#{${server.servlet.session.timeout} * 100}") // 1/10 of session timeout in ms
  public void markOfflineUsers() {
    Instant threshold = Instant.now().minusSeconds(sessionTimeoutSeconds);
    activityService.markUsersOffline(Timestamp.from(threshold));
  }
}
