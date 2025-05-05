package com.village.bellevue.service.impl;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.stream.Stream;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.village.bellevue.event.StatusEvent;
import com.village.bellevue.repository.ProfileRepository;
import com.village.bellevue.service.ActivityService;

import jakarta.transaction.Transactional;

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
  @Transactional
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
  @Transactional
  @Modifying
  @Override
  public void markUsersIdle(Timestamp lastSeen) {
    try (Stream<Long> users = profileRepository.getUsersToMarkIdle(lastSeen);) {
      users.parallel().forEach(user -> {
        profileRepository.setStatusIdle(user);
        publisher.publishEvent(new StatusEvent(user, "idle"));
      });
    }
  }

  @Async
  @Transactional
  @Modifying
  @Override
  public void markUserOffline(Long user) {
    if (profileRepository.setStatusOffline(user) > 0) {
      publisher.publishEvent(new StatusEvent(user, "offline"));
    }
  }
}
