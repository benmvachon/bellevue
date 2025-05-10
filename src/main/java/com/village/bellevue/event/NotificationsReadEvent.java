package com.village.bellevue.event;

import lombok.Data;

@Data
public class NotificationsReadEvent implements UserEvent {
  private final Long user;
}
