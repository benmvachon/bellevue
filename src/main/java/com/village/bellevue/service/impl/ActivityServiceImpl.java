package com.village.bellevue.service.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.event.type.StatusEvent;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.service.ActivityService;

@Service
public class ActivityServiceImpl implements ActivityService {
  private final ProfileRepository profileRepository;
  private final ApplicationEventPublisher publisher;

  public ActivityServiceImpl(
    ProfileRepository profileRepository,
    ApplicationEventPublisher publisher
  ) {
    this.profileRepository = profileRepository;
    this.publisher = publisher;
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  @Modifying
  @Override
  public void updateLastSeen(Long user) {
    profileRepository.setLastSeen(user, Timestamp.from(Instant.now()));
    if (profileRepository.setStatusOnline(user) > 0) {
      publisher.publishEvent(new StatusEvent(user, "active"));
    }
  }

  @Override
  public Timestamp getLastSeen(Long user) {
    return profileRepository.getLastSeen(user);
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  @Modifying
  @Override
  public void markUsersIdle(Timestamp lastSeen) {
    List<Long> users = profileRepository.getUsersToMarkIdle(lastSeen);
    for (Long user : users) {
      profileRepository.setStatusIdle(user);
      publisher.publishEvent(new StatusEvent(user, "idle"));
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  @Modifying
  @Override
  public void markUsersOffline(Timestamp lastSeen) {
    List<Long> users = profileRepository.getUsersToMarkOffline(lastSeen);
    for (Long user : users) {
      profileRepository.setStatusOffline(user);
      publisher.publishEvent(new StatusEvent(user, "offline"));
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  @Modifying
  @Override
  public void markUserOffline(Long user) {
    if (profileRepository.setStatusOffline(user) > 0) {
      publisher.publishEvent(new StatusEvent(user, "offline"));
    }
  }
}
