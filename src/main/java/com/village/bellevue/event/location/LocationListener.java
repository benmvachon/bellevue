package com.village.bellevue.event.location;

import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.ProfileEntity.LocationType;
import com.village.bellevue.event.type.LocationEvent;
import com.village.bellevue.repository.UserProfileRepository;

@Component
public class LocationListener {
  private final UserProfileRepository userProfileRepository;
  private final SimpMessagingTemplate messagingTemplate;

  public LocationListener(
    UserProfileRepository userProfileRepository,
    SimpMessagingTemplate messagingTemplate
  ) {
    this.userProfileRepository = userProfileRepository;
    this.messagingTemplate = messagingTemplate;
  }

  @Async
  @EventListener
  @Transactional(value = "asyncTransactionManager", timeout = 300)
  public void handleEvent(LocationEvent event) {
    Long user = event.getUser();
    Long location = event.getLocation();
    LocationType locationType = event.getLocationType();
    boolean entrance = event.isEntrance();
    List<Long> users = userProfileRepository.findAllUsersByLocation(user, location, locationType);
    for (Long other : users) {
      if (user.equals(other)) return;
      messagingTemplate.convertAndSendToUser(other.toString(), "/topic/location", entrance ? "entrance" : "exit");
    }
  }
}
