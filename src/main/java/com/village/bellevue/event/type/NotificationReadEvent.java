package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class NotificationReadEvent implements UserEvent {
  private final Long user;
  private final Long notification;
}
