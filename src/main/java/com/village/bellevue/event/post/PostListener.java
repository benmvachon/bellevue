package com.village.bellevue.event.post;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.event.PostEvent;
import com.village.bellevue.event.RatingEvent;
import com.village.bellevue.model.PostModel;

@Component
public class PostListener {
  private final SimpMessagingTemplate messagingTemplate;

  public PostListener(
    SimpMessagingTemplate messagingTemplate
  ) {
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(PostEvent event) {
    PostModel parent = event.getPost().getParent();
    if (Objects.nonNull(parent)) {
      Long id = event.getPost().getId();
      messagingTemplate.convertAndSend("/topic/post/" + parent.getId(), id);
    }
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(RatingEvent event) {
    Long post = event.getPost();
    messagingTemplate.convertAndSend("/topic/post/" + post, "rating");
  }
}
