package com.village.bellevue.event.profile;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.village.bellevue.event.type.AcceptanceEvent;
import com.village.bellevue.event.type.BlackboardEvent;
import com.village.bellevue.event.type.LocationEvent;
import com.village.bellevue.event.type.RequestEvent;
import com.village.bellevue.event.type.StatusEvent;

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
  public void handleEvent(LocationEvent event) {
    pingAttendees(event.getUser(), "location");
  }

  @Async
  @EventListener
  public void handleEvent(AcceptanceEvent event) {
    pingAttendees(event.getUser(), "acceptance");
    pingUsers(event.getUser(), event.getFriend(), "acceptance");
  }

  @Async
  @EventListener
  public void handleEvent(RequestEvent event) {
    pingUsers(event.getUser(), event.getFriend(), "request");
  }

  @Async
  @EventListener
  public void handleEvent(BlackboardEvent event) {
    pingAttendees(event.getUser(), "blackboard");
  }

  @Async
  @EventListener
  public void handleEvent(StatusEvent event) {
    pingAttendees(event.getUser(), "status");
  }

  @Async
  private void pingAttendees(Long user, String message) {
    messagingTemplate.convertAndSend("/topic/profile/" + user, message);
  }

  @Async
  private void pingUsers(Long user1, Long user2, String message) {
    messagingTemplate.convertAndSendToUser(user1.toString(), "/topic/friendshipStatus/" + user2, message);
    messagingTemplate.convertAndSendToUser(user2.toString(), "/topic/friendshipStatus/" + user1, message);
  }
}
