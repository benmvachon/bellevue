package com.village.bellevue.event.post;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.village.bellevue.event.type.PostDeleteEvent;
import com.village.bellevue.repository.FriendRepository;

@Component
public class PostDeleteListener {
  private final SimpMessagingTemplate messagingTemplate;
  private final FriendRepository friendRepository;

  public PostDeleteListener(
    SimpMessagingTemplate messagingTemplate,
    FriendRepository friendRepository
  ) {
    this.messagingTemplate = messagingTemplate;
    this.friendRepository = friendRepository;
  }

  @Async
  @EventListener
  public void handleEvent(PostDeleteEvent event) {
    Long user = event.getUser();
    Long post = event.getPost();
    Long parent = event.getParent();
    Long forum = event.getForum();
    for (Long friend : friendRepository.findFriends(user)) {
      if (Objects.nonNull(parent)) {
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/post." + parent + ".delete", post);
      } else {
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/forum." + forum + ".delete", post);
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/feed.delete", post);
      }
    }
    if (Objects.nonNull(parent)) {
      messagingTemplate.convertAndSendToUser(user.toString(), "/topic/post." + parent + ".delete", post);
    } else {
      messagingTemplate.convertAndSendToUser(user.toString(), "/topic/forum." + forum + ".delete", post);
      messagingTemplate.convertAndSendToUser(user.toString(), "/topic/feed.delete", post);
    }
  }
}
