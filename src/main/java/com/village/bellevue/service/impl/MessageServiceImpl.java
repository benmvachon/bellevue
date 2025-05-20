package com.village.bellevue.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import java.sql.Timestamp;
import java.util.List;

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.event.MessageEvent;
import com.village.bellevue.event.MessageReadEvent;
import com.village.bellevue.event.ThreadReadEvent;
import com.village.bellevue.event.ThreadsReadEvent;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.MessageRepository;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final FriendRepository friendRepository;
  private final UserProfileRepository userProfileRepository;
  private final ApplicationEventPublisher publisher;

  public MessageServiceImpl(
    MessageRepository messageRepository,
    FriendRepository friendRepository,
    UserProfileRepository userProfileRepository,
    ApplicationEventPublisher publisher
  ) {
    this.messageRepository = messageRepository;
    this.friendRepository = friendRepository;
    this.userProfileRepository = userProfileRepository;
    this.publisher = publisher;
  }

  @Override
  @Transactional(timeout = 30)
  public void message(Long friend, String message) throws AuthorizationException {
    MessageEntity messageEntity = new MessageEntity();
    messageEntity.setMessage(message);
    messageEntity.setReceiver(userProfileRepository.findById(friend).orElseThrow(() -> new AuthorizationException("Not authorized")));
    messageEntity = save(messageEntity);
    if (messageEntity.getId() != null) {
      MessageEntity fullMessage = messageRepository.findById(messageEntity.getId())
        .orElseThrow(() -> new IllegalStateException("Message not found after save"));
      friendRepository.incrementFriendshipScore(getAuthenticatedUserId(), friend);
      publisher.publishEvent(new MessageEvent(getAuthenticatedUserId(), friend, fullMessage));
    }
  }

  @Override
  public MessageEntity read(Long message) {
    MessageEntity entity = messageRepository.find(getAuthenticatedUserId(), message);
    return entity;
  }

  @Override
  public List<MessageEntity> readThreads(Timestamp cursor, Long limit) {
    return messageRepository.findThreads(getAuthenticatedUserId(), cursor, limit);
  }

  @Override
  public List<MessageEntity> refreshThreads(Timestamp cursor) {
    return messageRepository.refreshThreads(getAuthenticatedUserId(), cursor);
  }

  @Override
  public Long countUnreadThreads() {
    return messageRepository.countUnreadThreads(getAuthenticatedUserId());
  }

  @Override
  public Long countThreads() {
    return messageRepository.countThreads(getAuthenticatedUserId());
  }

  @Override
  public List<MessageEntity> readAll(Long friend, Timestamp cursor, Long limit) {
    return messageRepository.findAll(getAuthenticatedUserId(), friend, cursor, limit);
  }

  @Override
  public Long countAll(Long friend) {
    return messageRepository.countAll(getAuthenticatedUserId(), friend);
  }

  @Override
  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void markAllAsRead() {
    try {
      messageRepository.markAllAsRead(getAuthenticatedUserId());
    } finally {
      publisher.publishEvent(new ThreadsReadEvent(getAuthenticatedUserId()));
    }
  }

  @Override
  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void markThreadAsRead(Long friend) {
    try {
      messageRepository.markThreadAsRead(getAuthenticatedUserId(), friend);
    } finally {
      publisher.publishEvent(new ThreadReadEvent(getAuthenticatedUserId(), friend));
    }
  }

  @Override
  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void markAsRead(Long friend, Long id) {
    try {
      messageRepository.markAsRead(getAuthenticatedUserId(), id);
    } finally {
      publisher.publishEvent(new MessageReadEvent(getAuthenticatedUserId(), friend, id));
    }
  }

  @Transactional(timeout = 30)
  private MessageEntity save(MessageEntity message) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!friendRepository.areFriends(message.getReceiver().getUser(), user)) throw new AuthorizationException("Not authorized to message user");
    message.setSender(userProfileRepository.findById(user).orElseThrow(() -> new AuthorizationException("Not authorized")));
    message.setRead(false);
    return messageRepository.save(message);
  }

}
