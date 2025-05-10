package com.village.bellevue.event.notification;

import java.util.stream.Stream;

import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.entity.NotificationTypeEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.event.AcceptanceEvent;
import com.village.bellevue.event.EquipmentEvent;
import com.village.bellevue.event.ForumEvent;
import com.village.bellevue.event.MessageEvent;
import com.village.bellevue.event.NotificationReadEvent;
import com.village.bellevue.event.NotificationsReadEvent;
import com.village.bellevue.event.PostEvent;
import com.village.bellevue.event.RatingEvent;
import com.village.bellevue.event.RequestEvent;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.NotificationRepository;
import com.village.bellevue.repository.NotificationTypeRepository;
import com.village.bellevue.repository.UserProfileRepository;

@Component
public class NotificationListener {
  private final NotificationRepository notificationRepository;
  private final FriendRepository friendRepository;
  private final UserProfileRepository userProfileRepository;
  private final NotificationTypeRepository notificationTypeRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public NotificationListener(
    NotificationRepository notificationRepository,
    FriendRepository friendRepository,
    UserProfileRepository userProfileRepository,
    NotificationTypeRepository notificationTypeRepository,
    SimpMessagingTemplate messagingTemplate
  ) {
    this.notificationRepository = notificationRepository;
    this.friendRepository = friendRepository;
    this.userProfileRepository = userProfileRepository;
    this.notificationTypeRepository = notificationTypeRepository;
    this.messagingTemplate = messagingTemplate;
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
          if (user.equals(parent.getUser().getId())) continue;
          notifyFriend(user, parent.getUser().getId(), 3l, post.getId());
          parent = parent.getParent();
        }
      } else if (post.getForum().getUser() == null) {
        notifyFriends(user, 2l, post.getId());
      } else {
        notifyMutualFriends(user, post.getForum().getUser().getId(), 2l, post.getId());
        if (user.equals(post.getForum().getUser().getId())) return;
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
    if (user.equals(postAuthor)) return;
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
  @EventListener
  @Transactional
  public void handleEquipmentEvent(EquipmentEvent event) {
    Long user = event.getUser();
    Long item = event.getEquipment().getItem().getId();
    notifyFriend(user, user, 8l, item);
  }

  @Async
  @EventListener
  public void handleNotificationReadEvent(NotificationReadEvent event) {
    Long user = event.getUser();
    Long notification = event.getNotification();
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/notification/unread", "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/notification/unread/" + notification, "update");
  }

  @Async
  @EventListener
  public void handleNotificationsReadEvent(NotificationsReadEvent event) {
    Long user = event.getUser();
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/notification/unread", "update");
    messagingTemplate.convertAndSendToUser(user.toString(), "/topic/notification/all", "read");
  }

  @Async
  @Transactional
  public void notifyFriends(Long user, Long type, Long entity) {
    try (Stream<Long> friendStream = friendRepository.streamFriends(user)) {
      SecurityContext context = SecurityContextHolder.getContext();
      friendStream.parallel().forEach(friend -> {
        if (user.equals(friend)) return;
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
        if (user.equals(mutual)) return;
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
    UserProfileEntity notifier = userProfileRepository.findById(user).orElseThrow(() -> new IllegalStateException("Not found"));
    NotificationTypeEntity notificationType = notificationTypeRepository.findById(type).orElseThrow(() -> new IllegalStateException("Not found"));

    NotificationEntity notification = new NotificationEntity();
    notification.setNotifier(notifier);
    notification.setNotified(friend);
    notification.setType(notificationType);
    notification.setEntity(entity);
    notification.setRead(false);
    notification = notificationRepository.save(notification);
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/notification/unread", "update");
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/notification", notification);
  }
}
