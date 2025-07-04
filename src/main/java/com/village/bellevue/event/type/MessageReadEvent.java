package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class MessageReadEvent implements UserEvent {
  private final Long user;
  private final Long friend;
  private final Long message;
}
