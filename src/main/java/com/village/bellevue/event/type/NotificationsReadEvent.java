package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class NotificationsReadEvent implements UserEvent {
  private final Long user;
}
