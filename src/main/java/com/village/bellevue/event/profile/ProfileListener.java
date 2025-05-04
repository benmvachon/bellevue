package com.village.bellevue.event.profile;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.event.AcceptanceEvent;
import com.village.bellevue.event.BlackboardEvent;
import com.village.bellevue.event.LocationEvent;
import com.village.bellevue.event.UserEvent;

@Component
public class ProfileListener {
  private final SimpMessagingTemplate messagingTemplate;

  public ProfileListener(
    SimpMessagingTemplate messagingTemplate
  ) {
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(LocationEvent event) {
    pingAttendees(event);
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(AcceptanceEvent event) {
    pingAttendees(event);
  }

  @Async
  @EventListener
  @Transactional
  public void handleEvent(BlackboardEvent event) {
    pingAttendees(event);
  }

  @Async
  @Transactional
  private void pingAttendees(UserEvent event) {
    Long user = event.getUser();
    messagingTemplate.convertAndSend("/topic/profile/"+user, event);
  }
}
