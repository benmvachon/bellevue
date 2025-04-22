package com.village.bellevue.event;

import java.util.stream.Stream;

import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.entity.NotificationTypeEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.NotificationRepository;

@Component
public class NotificationListener {
  private final NotificationRepository notificationRepository;
  private final FriendRepository friendRepository;

  public NotificationListener(
    NotificationRepository notificationRepository,
    FriendRepository friendRepository
  ) {
    this.notificationRepository = notificationRepository;
    this.friendRepository = friendRepository;
  }

  @Async
  @EventListener
  @Transactional
  public void handleForumEvent(ForumEvent event) {
    Long user = event.getUser();
    ForumModel forum = event.getForum();
    if (forum != null) {
      notifyFriends(user, 1l, forum.getId());
    }
  }

  @Async
  @EventListener
  @Transactional
  public void handlePostEvent(PostEvent event) {
    Long user = event.getUser();
    PostModel post = event.getPost();
    if (post != null) {
      if (post.getParent() != null) {
        PostModel parent = post.getParent();
        while (parent != null) {
          notifyFriend(user, parent.getUser().getId(), 3l, post.getId());
          parent = parent.getParent();
        }
      } else if (post.getForum().getUser() == null) {
        notifyFriends(user, 2l, post.getId());
      } else {
        notifyMutualFriends(user, post.getForum().getUser().getId(), 2l, post.getId());
        notifyFriend(user, post.getForum().getUser().getId(), 2l, post.getId());
      }
    }
  }

  @Async
  @EventListener
  @Transactional
  public void handleRatingEvent(RatingEvent event) {
    Long user = event.getUser();
    Long post = event.getPost();
    Long postAuthor = event.getPostAuthor();
    notifyFriend(user, postAuthor, 4l, post);
  }

  @Async
  @EventListener
  @Transactional
  public void handleRequestEvent(RequestEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    notifyFriend(user, friend, 5l, user);
  }

  @Async
  @EventListener
  @Transactional
  public void handleAcceptanceEvent(AcceptanceEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    notifyFriend(user, friend, 6l, user);
  }

  @Async
  @EventListener
  @Transactional
  public void handleMessageEvent(MessageEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    notifyFriend(user, friend, 7l, user);
  }

  @Async
  @Transactional
  public void notifyFriends(Long user, Long type, Long entity) {
    try (Stream<Long> friendStream = friendRepository.streamFriends(user)) {
      SecurityContext context = SecurityContextHolder.getContext();
      friendStream.parallel().forEach(friend -> {
        Runnable task = () -> notifyFriend(user, friend, type, entity);
        DelegatingSecurityContextRunnable securedTask = new DelegatingSecurityContextRunnable(task, context);
        securedTask.run();
      });
    }
  }

  @Async
  @Transactional
  public void notifyMutualFriends(Long user, Long friend, Long type, Long entity) {
    try (Stream<Long> friendStream = friendRepository.streamMutualFriends(user, friend)) {
      SecurityContext context = SecurityContextHolder.getContext();
      friendStream.parallel().forEach(mutual -> {
        Runnable task = () -> notifyFriend(user, mutual, type, entity);
        DelegatingSecurityContextRunnable securedTask = new DelegatingSecurityContextRunnable(task, context);
        securedTask.run();
      });
    }
  }

  @Async
  @Transactional
  @Modifying
  private void notifyFriend(Long user, Long friend, Long type, Long entity) {
    if (user.equals(friend)) return;
    UserProfileEntity notifier = new UserProfileEntity(user);
    NotificationTypeEntity notificationType = new NotificationTypeEntity(type);

    NotificationEntity notification = new NotificationEntity();
    notification.setNotifier(notifier);
    notification.setNotified(friend);
    notification.setType(notificationType);
    notification.setEntity(entity);
    notification.setRead(false);
    notificationRepository.save(notification);
  }
}
