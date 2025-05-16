package com.village.bellevue.event.popularity;

import java.util.List;
import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.id.AggregateRatingId;
import com.village.bellevue.event.PopularityEvent;
import com.village.bellevue.model.PopularityMessageModel;
import com.village.bellevue.repository.AggregateRatingRepository;
import com.village.bellevue.repository.FriendRepository;
import com.village.bellevue.repository.PostRepository;

@Component
public class PopularityListener {
  private final PostRepository postRepository;
  private final FriendRepository friendRepository;
  private final AggregateRatingRepository aggregateRatingRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public PopularityListener(
    PostRepository postRepository,
    FriendRepository friendRepository,
    AggregateRatingRepository aggregateRatingRepository,
    SimpMessagingTemplate messagingTemplate
  ) {
    this.postRepository = postRepository;
    this.friendRepository = friendRepository;
    this.aggregateRatingRepository = aggregateRatingRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void handleEvent(PopularityEvent event) {
    Long user = event.getUser();
    Long post = event.getPost();
    Long parent = event.getParent();
    Long forum = event.getForum();
    List<Long> friends = friendRepository.findFriends(user);
    for (Long friend : friends) {
      sendEventToClient(friend, post, parent, forum);
    }
  }

  @Async
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  private void sendEventToClient(Long user, Long post, Long parent, Long forum) {
    if (!postRepository.canRead(post, user)) return;
    AggregateRatingEntity rating = aggregateRatingRepository.findById(new AggregateRatingId(user, post)).orElseThrow(() -> new IllegalStateException("Aggregate rating not found"));
    PopularityMessageModel message = new PopularityMessageModel(post, rating.getPopularity());
    if (Objects.nonNull(parent)) messagingTemplate.convertAndSendToUser(user.toString(), "/topic/post/" + parent + "/popularity", message);
    else messagingTemplate.convertAndSendToUser(user.toString(), "/topic/forum/" + forum + "/popularity", message);
  }
}
