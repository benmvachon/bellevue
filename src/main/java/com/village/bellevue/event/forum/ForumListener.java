package com.village.bellevue.event.forum;

import java.util.Objects;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.event.PostEvent;

@Component
public class ForumListener {
  private final SimpMessagingTemplate messagingTemplate;

  public ForumListener(
    SimpMessagingTemplate messagingTemplate
  ) {
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(PostEvent event) {
    Long forum = event.getPost().getForum().getId();
    if (Objects.isNull(event.getPost().getParent()))
      messagingTemplate.convertAndSend("/topic/forum/" + forum, event);
  }
}
