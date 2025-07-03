package com.village.bellevue.event.user;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.event.type.MessageEvent;
import com.village.bellevue.event.type.NewUserEvent;
import com.village.bellevue.repository.MessageRepository;
import com.village.bellevue.repository.UserProfileRepository;

@Component
public class NewUserListener {

  private final MessageRepository messageRepository;
  private final UserProfileRepository userProfileRepository;
  private final ApplicationEventPublisher publisher;

  private final String message = "Welcome to Blorvis! This app is in active development but we welcome new users. In order to get value from the app, you will need to add friends. Please navigate to /map/suburbs to find users to send friend requests to.";

  public NewUserListener(
    MessageRepository messageRepository,
    UserProfileRepository userProfileRepository,
    ApplicationEventPublisher publisher
  ) {
    this.messageRepository = messageRepository;
    this.userProfileRepository = userProfileRepository;
    this.publisher = publisher;
  }

  @Async
  @EventListener
  public void handleEvent(NewUserEvent event) throws AuthorizationException {
    Long user = event.getUser();
    MessageEntity messageEntity = new MessageEntity();
    messageEntity.setMessage(message);
    messageEntity.setReceiver(userProfileRepository.findById(user).orElseThrow(() -> new AuthorizationException("No such user")));
    messageEntity.setRead(false);
    messageEntity = messageRepository.save(messageEntity);
    if (messageEntity.getId() != null) {
      MessageEntity fullMessage = messageRepository.findById(messageEntity.getId())
        .orElseThrow(() -> new IllegalStateException("Message not found after save"));
      publisher.publishEvent(new MessageEvent(0l, user, fullMessage));
    }
  }
}
