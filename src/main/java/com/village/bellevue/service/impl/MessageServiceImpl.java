package com.village.bellevue.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.village.bellevue.config.security.SecurityConfig.getAuthenticatedUserId;
import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.MessageRepository;
import com.village.bellevue.service.MessageService;
import com.village.bellevue.service.NotificationService;

import jakarta.transaction.Transactional;

@Service
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final FriendRepository friendRepository;
  private final NotificationService notificationService;

  public MessageServiceImpl(
    MessageRepository messageRepository,
    FriendRepository friendRepository,
    NotificationService notificationService
  ) {
    this.messageRepository = messageRepository;
    this.friendRepository = friendRepository;
    this.notificationService = notificationService;
  }

  @Override
  @Transactional
  public void message(Long friend, String message) throws AuthorizationException {
    MessageEntity messageEntity = new MessageEntity();
    try {
      messageEntity.setMessage(message);
      messageEntity.setReceiver(friend);
      messageEntity = save(messageEntity);
    } finally {
      if (messageEntity.getId() != null) {
        notificationService.notifyFriend(friend, 7l, getAuthenticatedUserId());
      }
    }
  }

  @Override
  public Page<UserProfileEntity> readThreads(int page, int size) {
    return messageRepository.findThreads(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<UserProfileEntity> readUnreadThreads(int page, int size) {
    return messageRepository.findUnreadThreads(getAuthenticatedUserId(), PageRequest.of(page, size));
  }

  @Override
  public Page<MessageEntity> readAll(Long friend, int page, int size) {
    return messageRepository.findAll(getAuthenticatedUserId(), friend, PageRequest.of(page, size));
  }

  @Override
  @Transactional
  public void markAllAsRead(Long friend) {
    messageRepository.markAllAsRead(getAuthenticatedUserId(), friend);
  }

  @Override
  @Transactional
  public void markAsRead(Long id) {
    messageRepository.markAsRead(id);
  }

  private MessageEntity save(MessageEntity message) throws AuthorizationException {
    Long user = getAuthenticatedUserId();
    if (!friendRepository.areFriends(message.getReceiver(), user)) throw new AuthorizationException("Not authorized to message user");
    UserProfileEntity sender = new UserProfileEntity(user);
    message.setSender(sender);
    message.setRead(false);
    return messageRepository.save(message);
  }

}
