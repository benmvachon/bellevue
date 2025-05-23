package com.village.bellevue.event.notification;

import java.util.List;
import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.NotificationEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.NotificationEntity.NotificationType;
import com.village.bellevue.entity.id.NotificationSettingId;
import com.village.bellevue.event.type.AcceptanceEvent;
import com.village.bellevue.event.type.EquipmentEvent;
import com.village.bellevue.event.type.ForumEvent;
import com.village.bellevue.event.type.MessageEvent;
import com.village.bellevue.event.type.NotificationReadEvent;
import com.village.bellevue.event.type.NotificationsReadEvent;
import com.village.bellevue.event.type.PostEvent;
import com.village.bellevue.event.type.RatingEvent;
import com.village.bellevue.event.type.RequestEvent;
import com.village.bellevue.model.ForumModel;
import com.village.bellevue.model.PostModel;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.NotificationRepository;
import com.village.bellevue.repository.NotificationSettingRepository;
import com.village.bellevue.repository.UserProfileRepository;

@Component
public class NotificationListener {
  private final NotificationRepository notificationRepository;
  private final FriendRepository friendRepository;
  private final UserProfileRepository userProfileRepository;
  private final NotificationSettingRepository notificationSettingRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public NotificationListener(
    NotificationRepository notificationRepository,
    FriendRepository friendRepository,
    UserProfileRepository userProfileRepository,
    NotificationSettingRepository notificationSettingRepository,
    SimpMessagingTemplate messagingTemplate
  ) {
    this.notificationRepository = notificationRepository;
    this.friendRepository = friendRepository;
    this.userProfileRepository = userProfileRepository;
    this.notificationSettingRepository = notificationSettingRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  public void handleForumEvent(ForumEvent event) {
    Long user = event.getUser();
    ForumModel forum = event.getForum();
    if (forum != null) {
      notifyFriends(user, NotificationType.FORUM, forum.getId());
    }
  }

  @Async
  @EventListener
  public void handlePostEvent(PostEvent event) {
    Long user = event.getUser();
    PostModel post = event.getPost();
    if (Objects.nonNull(post)) {
      if (Objects.nonNull(post.getParent())) {
        PostModel parent = post.getParent();
        while (Objects.nonNull(parent)) {
          if (user.equals(parent.getUser().getId())) continue;
          notifyFriend(user, parent.getUser().getId(), NotificationType.REPLY, post.getId());
          parent = parent.getParent();
        }
      } else if (Objects.isNull(post.getForum().getUser())) {
        notifyFriends(user, post.getId(), post.getForum().getId());
      } else {
        notifyMutualFriends(user, post.getForum().getUser().getId(), post.getId(), post.getForum().getId());
        if (user.equals(post.getForum().getUser().getId())) return;
        notifyFriend(user, post.getForum().getUser().getId(), post.getId(), post.getForum().getId());
      }
    }
  }

  @Async
  @EventListener
  public void handleRatingEvent(RatingEvent event) {
    Long user = event.getUser();
    Long post = event.getPost();
    Long postAuthor = event.getPostAuthor();
    if (user.equals(postAuthor)) return;
    notifyFriend(user, postAuthor, NotificationType.RATING, post);
  }

  @Async
  @EventListener
  public void handleRequestEvent(RequestEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    notifyFriend(user, friend, NotificationType.REQUEST, user);
  }

  @Async
  @EventListener
  public void handleAcceptanceEvent(AcceptanceEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    notifyFriend(user, friend, NotificationType.ACCEPTANCE, user);
  }

  @Async
  @EventListener
  public void handleMessageEvent(MessageEvent event) {
    Long user = event.getUser();
    Long friend = event.getFriend();
    notifyFriend(user, friend, NotificationType.MESSAGE, user);
  }

  @Async
  @EventListener
  public void handleEquipmentEvent(EquipmentEvent event) {
    Long user = event.getUser();
    Long item = event.getEquipment().getItem().getId();
    notifyFriend(user, user, NotificationType.EQUIPMENT, item);
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
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void notifyFriends(Long user, NotificationType type, Long entity) {
    List<Long> friends = friendRepository.findFriends(user);
    for (Long friend : friends) {
      notifyFriend(user, friend, type, entity);
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void notifyFriends(Long user, Long post, Long forum) {
    List<Long> friends = friendRepository.findFriends(user);
    for (Long friend : friends) {
      notifyFriend(user, friend, post, forum);
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void notifyMutualFriends(Long user, Long friend, NotificationType type, Long entity) {
    List<Long> friends = friendRepository.findMutualFriends(user, friend);
    for (Long mutual : friends) {
      notifyFriend(user, mutual, type, entity);
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void notifyMutualFriends(Long user, Long friend, Long post, Long forum) {
    List<Long> friends = friendRepository.findMutualFriends(user, friend);
    for (Long mutual : friends) {
      notifyFriend(user, mutual, post, forum);
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  @Modifying
  private void notifyFriend(Long user, Long friend, NotificationType type, Long entity) {
    UserProfileEntity notifier = userProfileRepository.findById(user).orElseThrow(() -> new IllegalStateException("Not found"));

    NotificationEntity notification = new NotificationEntity();
    notification.setNotifier(notifier);
    notification.setNotified(friend);
    notification.setType(type);
    notification.setEntity(entity);
    notification.setRead(false);
    notification = notificationRepository.save(notification);
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/notification/unread", "update");
    messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/notification", notification);
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  @Modifying
  private void notifyFriend(Long user, Long friend, Long post, Long forum) {
    UserProfileEntity notifier = userProfileRepository.findById(user).orElseThrow(() -> new IllegalStateException("Not found"));
    notificationSettingRepository.findById(new NotificationSettingId(friend, forum)).ifPresent((setting) -> {
      if (setting.isNotify()) {
        NotificationEntity notification = new NotificationEntity();
        notification.setNotifier(notifier);
        notification.setNotified(friend);
        notification.setType(NotificationType.POST);
        notification.setEntity(post);
        notification.setRead(false);
        notification = notificationRepository.save(notification);
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/notification/unread", "update");
        messagingTemplate.convertAndSendToUser(friend.toString(), "/topic/notification", notification);
      }
    });
  }
}
