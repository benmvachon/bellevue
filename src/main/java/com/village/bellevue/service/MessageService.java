package com.village.bellevue.service;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;

public interface MessageService {
  public void message(Long friend, String message) throws AuthorizationException;
  public Page<UserProfileEntity> readThreads(int page, int size);
  public Long countUnreadThreads();
  public Page<MessageEntity> readAll(Long friend, int page, int size);
  public void markAllAsRead();
  public void markThreadAsRead(Long friend);
  public void markAsRead(Long id);
}
