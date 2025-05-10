package com.village.bellevue.event.message;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.village.bellevue.event.MessageEvent;
import com.village.bellevue.event.MessageReadEvent;
import com.village.bellevue.event.ThreadReadEvent;
import com.village.bellevue.event.ThreadsReadEvent;

@Component
public class MessageListener {
  private final SimpMessagingTemplate messagingTemplate;

  public MessageListener(
    SimpMessagingTemplate messagingTemplate
  ) {
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  public void handleMessageEvent(MessageEvent event) {
    Long friend = event.getFriend();
    Long user = event.getUser();
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/thread", event);
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/message/" + user, event);
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/thread/unread", "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread", event);
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/message/" + friend, event);
  }

  @Async
  @EventListener
  public void handleMessageReadEvent(MessageReadEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    Long message = event.getMessage();
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread/unread", "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread/unread/" + friend, "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/message/unread/" + message, "update");
  }

  @Async
  @EventListener
  public void handleThreadReadEvent(ThreadReadEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread/unread", "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread/unread/" + friend, "update");
  }

  @Async
  @EventListener
  public void handleThreadsReadEvent(ThreadsReadEvent event) {
    Long user = event.getUser();
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread/unread", "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/thread/all", "read");
  }
}
