package com.village.bellevue.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import java.sql.Timestamp;
import java.util.List;

import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.event.NotificationReadEvent;
import com.village.bellevue.event.NotificationsReadEvent;
import com.village.bellevue.repository.NotificationRepository;
import com.village.bellevue.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;
  private final ApplicationEventPublisher publisher;

  public NotificationServiceImpl(
    NotificationRepository notificationRepository,
    ApplicationEventPublisher publisher
  ) {
    this.notificationRepository = notificationRepository;
    this.publisher = publisher;
  }

  @Override
  public List<NotificationEntity> readAll(Timestamp cursor, Long limit) {
    return notificationRepository.findAll(getAuthenticatedUserId(), cursor, limit);
  }

  @Override
  public List<NotificationEntity> refresh(Timestamp cursor) {
    return notificationRepository.refresh(getAuthenticatedUserId(), cursor);
  }

  @Override
  public NotificationEntity read(Long notification) {
    return notificationRepository.findNotification(getAuthenticatedUserId(), notification);
  }

  @Override
  public Long countUnread() {
    return notificationRepository.countUnread(getAuthenticatedUserId());
  }

  @Override
  public Long countTotal() {
    return notificationRepository.countTotal(getAuthenticatedUserId());
  }

  @Override
  @Transactional
  public void markAllAsRead() {
    try {
      notificationRepository.markAllAsRead(getAuthenticatedUserId());
    } finally {
      publisher.publishEvent(new NotificationsReadEvent(getAuthenticatedUserId()));
    }
  }

  @Override
  @Transactional
  public void markAsRead(Long id) {
    notificationRepository.markAsRead(id, getAuthenticatedUserId());
    try {
      notificationRepository.markAsRead(id, getAuthenticatedUserId());
    } finally {
      publisher.publishEvent(new NotificationReadEvent(getAuthenticatedUserId(), id));
    }
  }
}
