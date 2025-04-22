package com.village.bellevue.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.repository.NotificationRepository;
import com.village.bellevue.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository notificationRepository;

  public NotificationServiceImpl(NotificationRepository notificationRepository) {
    this.notificationRepository = notificationRepository;
  }

  @Override
  public Page<NotificationEntity> readAll(int page, int size) {
    return notificationRepository.findAll(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Long countUnread() {
    return notificationRepository.countUnread(getAuthenticatedUserId());
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
}
