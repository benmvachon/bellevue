package com.village.bellevue.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.event.MessageEvent;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.MessageRepository;
import com.village.bellevue.service.MessageService;

import jakarta.transaction.Transactional;

@Service
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final FriendRepository friendRepository;
  private final ApplicationEventPublisher publisher;

  public MessageServiceImpl(
    MessageRepository messageRepository,
    FriendRepository friendRepository,
    ApplicationEventPublisher publisher
  ) {
    this.messageRepository = messageRepository;
    this.friendRepository = friendRepository;
    this.publisher = publisher;
  }

  @Override
  @Transactional
  public void message(Long friend, String message) throws AuthorizationException {
    MessageEntity messageEntity = new MessageEntity();
    try {
      messageEntity.setMessage(message);
      messageEntity.setReceiver(new UserProfileEntity(friend));
      messageEntity = save(messageEntity);
    } finally {
      if (messageEntity.getId() != null) {
        publisher.publishEvent(new MessageEvent(getAuthenticatedUserId(), friend, message));
      }
    }
  }

  @Override
  public Page<UserProfileEntity> readThreads(int page, int size) {
    return messageRepository.findThreads(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Long countUnreadThreads() {
    return messageRepository.countUnreadThreads(getAuthenticatedUserId());
  }

  @Override
  public Page<MessageEntity> readAll(Long friend, int page, int size) {
    return messageRepository.findAll(getAuthenticatedUserId(), friend, PageRequest.of(page, size));
  }

  @Override
  @Async
  @Transactional
  public void markAllAsRead() {
    messageRepository.markAllAsRead(getAuthenticatedUserId());
  }

  @Override
  @Async
  @Transactional
  public void markThreadAsRead(Long friend) {
    messageRepository.markThreadAsRead(getAuthenticatedUserId(), friend);
  }

  @Override
  @Async
  @Transactional
  public void markAsRead(Long id) {
    messageRepository.markAsRead(getAuthenticatedUserId(), id);
  }

  @Transactional
  private MessageEntity save(MessageEntity message) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!friendRepository.areFriends(message.getReceiver().getUser(), user)) throw new AuthorizationException("Not authorized to message user");
    UserProfileEntity sender = new UserProfileEntity(user);
    message.setSender(sender);
    message.setRead(false);
    return messageRepository.save(message);
  }

}
