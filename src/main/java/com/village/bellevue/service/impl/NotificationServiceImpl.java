package com.village.bellevue.service.impl;

import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.entity.NotificationTypeEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.NotificationRepository;
import com.village.bellevue.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

  private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

  private final NotificationRepository notificationRepository;
  private final FriendRepository friendRepository;

  public NotificationServiceImpl(
    NotificationRepository notificationRepository,
    FriendRepository friendRepository
  ) {
    this.notificationRepository = notificationRepository;
    this.friendRepository = friendRepository;
  }

  @Override
  @Async
  @Transactional
  public void notifyFriends(Long type, Long entity) {
    try (Stream<Long> friendStream = friendRepository.streamFriends(getAuthenticatedUserId())) {
      SecurityContext context = SecurityContextHolder.getContext();
      friendStream.parallel().forEach(friend -> {
        Runnable task = () -> notifyFriend(friend, type, entity);
        DelegatingSecurityContextRunnable securedTask = new DelegatingSecurityContextRunnable(task, context);
        securedTask.run();
      });
    }
  }

  @Override
  @Async
  @Transactional
  public void notifyMutualFriends(Long friend, Long type, Long entity) {
    try (Stream<Long> friendStream = friendRepository.streamMutualFriends(getAuthenticatedUserId(), friend)) {
      SecurityContext context = SecurityContextHolder.getContext();
      friendStream.parallel().forEach(mutual -> {
        Runnable task = () -> notifyFriend(mutual, type, entity);
        DelegatingSecurityContextRunnable securedTask = new DelegatingSecurityContextRunnable(task, context);
        securedTask.run();
      });
    }
  }

  @Override
  @Async
  @Transactional
  public void notifyFriend(Long friend, Long type, Long entity) {
    Long user = getAuthenticatedUserId();
    logger.info("Notifying friend " + friend + " of user " + user);
    UserProfileEntity notifier = new UserProfileEntity(user);
    NotificationTypeEntity notificationType = new NotificationTypeEntity(type);

    NotificationEntity notification = new NotificationEntity();
    notification.setNotifier(notifier);
    notification.setNotified(friend);
    notification.setType(notificationType);
    notification.setEntity(entity);
    notification.setRead(false);
    notificationRepository.save(notification);
  }

  @Override
  @Transactional
  public void markAllAsRead() {
    notificationRepository.markAllAsRead(getAuthenticatedUserId());
  }

  @Override
  @Transactional
  public void markAsRead(Long id) {
    notificationRepository.markAsRead(id, getAuthenticatedUserId());
  }

  @Override
  public Page<NotificationEntity> readAll(int page, int size) {
    return notificationRepository.findAll(getAuthenticatedUserId(), PageRequest.of(page, size));
  }
}
