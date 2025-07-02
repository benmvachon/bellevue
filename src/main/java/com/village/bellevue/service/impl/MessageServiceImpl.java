package com.village.bellevue.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;

import java.sql.Timestamp;
import java.util.List;

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.event.type.MessageEvent;
import com.village.bellevue.event.type.MessageReadEvent;
import com.village.bellevue.event.type.ThreadReadEvent;
import com.village.bellevue.event.type.ThreadsReadEvent;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.MessageRepository;
import com.village.bellevue.repository.UserProfileRepository;
import com.village.bellevue.service.MessageService;

@Service
public class MessageServiceImpl implements MessageService {

  private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

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
    Long user = getAuthenticatedUserId();
    logger.info("Message from: " + user + " to: " + friend + " received from client");
    MessageEntity messageEntity = new MessageEntity();
    messageEntity.setMessage(message);
    messageEntity.setReceiver(userProfileRepository.findById(friend).orElseThrow(() -> new AuthorizationException("Not authorized")));
    messageEntity = save(messageEntity);
    if (messageEntity.getId() != null) {
      logger.info("Message from: " + user + " to: " + friend + " saved to repository");
      MessageEntity fullMessage = messageRepository.findById(messageEntity.getId())
        .orElseThrow(() -> new IllegalStateException("Message not found after save"));
      logger.info("Message from: " + user + " to: " + friend + " retrieved from repository");
      friendRepository.incrementFriendshipScore(user, friend);
      logger.info("Incremented friendship score between: " + user + " and: " + friend + " retreived from repository");
      publisher.publishEvent(new MessageEvent(user, friend, fullMessage));
      logger.info("Message from: " + user + " to: " + friend + " published to event queue");
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
    if (friend == 0l) friend = null; // 0 is the id for the non-existent SYSTEM user
    return messageRepository.findAll(getAuthenticatedUserId(), friend, cursor, limit);
  }

  @Override
  public Long countAll(Long friend) {
    if (friend == 0l) friend = null; // 0 is the id for the non-existent SYSTEM user
    return messageRepository.countAll(getAuthenticatedUserId(), friend);
  }

  @Override
  @Transactional(timeout = 30)
  public void markAllAsRead() {
    try {
      messageRepository.markAllAsRead(getAuthenticatedUserId());
    } finally {
      publisher.publishEvent(new ThreadsReadEvent(getAuthenticatedUserId()));
    }
  }

  @Override
  @Transactional(timeout = 30)
  public void markThreadAsRead(Long friend) {
    try {
      messageRepository.markThreadAsRead(getAuthenticatedUserId(), friend);
    } finally {
      publisher.publishEvent(new ThreadReadEvent(getAuthenticatedUserId(), friend));
    }
  }

  @Override
  @Transactional(timeout = 30)
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
