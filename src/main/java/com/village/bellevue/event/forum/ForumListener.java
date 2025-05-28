package com.village.bellevue.event.forum;

import java.util.Objects;
import com.village.bellevue.repository.FriendRepository;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.village.bellevue.event.type.PostEvent;

@Component
public class ForumListener {

  private final SimpMessagingTemplate messagingTemplate;
  private final FriendRepository friendRepository;

  public ForumListener(
    SimpMessagingTemplate messagingTemplate,
    FriendRepository friendRepository
  ) {
    this.messagingTemplate = messagingTemplate;
    this.friendRepository = friendRepository;
  }

  @Async
  @EventListener
  public void handleEvent(PostEvent event) {
    Long forum = event.getPost().getForum().getId();
    Long user = event.getUser();
    Long id = event.getPost().getId();
    if (Objects.isNull(event.getPost().getParent())) {
      messagingTemplate.convertAndSend("/topic/forum/" + forum, id);
      messagingTemplate.convertAndSendToUser(user.toString(), "/topic/feed/", id);
      for (Long friend : friendRepository.findFriends(user)) {
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/feed/", id);
      }
    }
  }
}
