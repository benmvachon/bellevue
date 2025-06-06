package com.village.bellevue.service;

import java.sql.Timestamp;
import java.util.List;

import com.village.bellevue.entity.NotificationEntity;

public interface NotificationService {
  public List<NotificationEntity> readAll(Timestamp cursor, Long limit);
  public List<NotificationEntity> refresh(Timestamp cursor);
  public NotificationEntity read(Long notification);
  public Long countUnread();
  public Long countTotal();
  public void markAllAsRead();
  public void markAsRead(Long id);
}
