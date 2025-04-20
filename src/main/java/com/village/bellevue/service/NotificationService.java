package com.village.bellevue.service;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.NotificationEntity;

public interface NotificationService {
  public Page<NotificationEntity> readAll(int page, int size);
  public Long countUnread();
  public void notifyFriends(Long type, Long entity);
  public void notifyMutualFriends(Long friend, Long type, Long entity);
  public void notifyFriend(Long friend, Long type, Long entity);
  public void markAllAsRead();
  public void markAsRead(Long id);
}
