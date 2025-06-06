package com.village.bellevue.event.forum;

import java.util.List;
import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.event.type.ForumReadCountEvent;
import com.village.bellevue.event.type.PostEvent;
import com.village.bellevue.model.ProfileModel;
import com.village.bellevue.repository.ForumRepository;
import com.village.bellevue.repository.FriendRepository;

@Component
public class ForumReadCountListener {
  private final SimpMessagingTemplate messagingTemplate;
  private final FriendRepository friendRepository;
  private final ForumRepository forumRepository;

  public ForumReadCountListener(
    SimpMessagingTemplate messagingTemplate,
    FriendRepository friendRepository,
    ForumRepository forumRepository
  ) {
    this.messagingTemplate = messagingTemplate;
    this.friendRepository = friendRepository;
    this.forumRepository = forumRepository;
  }

  @Async
  @EventListener
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void handleEvent(PostEvent event) {
    ProfileModel forumUser = event.getPost().getForum().getUser();
    Long forum = event.getPost().getForum().getId();
    List<Long> friends;
    if (Objects.isNull(event.getPost().getParent())) {
      if (Objects.isNull(forumUser)) friends = friendRepository.findFriends(event.getUser());
      else friends = friendRepository.findMutualFriends(event.getUser(), forumUser.getId());
      for (Long friend : friends) {
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/forum.unread/" + forum, "update");
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/feed.unread", "update");
      }
      if (Objects.nonNull(forumUser)) {
        messagingTemplate.convertAndSendToUser(forumUser.getId().toString(), "/topic/forum.unread/" + forum, "update");
        messagingTemplate.convertAndSendToUser(forumUser.getId().toString(), "/topic/feed.unread", "update");
        messagingTemplate.convertAndSendToUser(event.getUser().toString(), "/topic/forum.unread/" + forum, "update");
        messagingTemplate.convertAndSendToUser(event.getUser().toString(), "/topic/feed.unread", "update");
      }
    }
  }

  @Async
  @EventListener
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void handleEvent(ForumReadCountEvent event) {
    Long user = event.getUser();
    if (Objects.nonNull(event.getForum())) {
      messagingTemplate.convertAndSendToUser(user.toString(), "/topic/forum.unread." + event.getForum(), "update");
    } else {
      for (Long forum : forumRepository.findAll(user)) {
        messagingTemplate.convertAndSendToUser(user.toString(), "/topic/forum.unread." + forum, "update");
      }
    }
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/feed.unread", "update");
  }
}
