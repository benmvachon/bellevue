package com.village.bellevue.service;

import java.sql.Timestamp;
import java.util.List;

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.error.AuthorizationException;

public interface MessageService {
  public void message(Long friend, String message) throws AuthorizationException;
  public MessageEntity read(Long message);
  public List<MessageEntity> readThreads(Timestamp cursor, Long limit);
  public List<MessageEntity> refreshThreads(Timestamp cursor);
  public Long countUnreadThreads();
  public Long countThreads();
  public List<MessageEntity> readAll(Long friend, Timestamp cursor, Long limit);
  public Long countAll(Long friend);
  public void markAllAsRead();
  public void markThreadAsRead(Long friend);
  public void markAsRead(Long friend, Long id);
}
