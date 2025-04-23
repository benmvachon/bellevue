package com.village.bellevue.event;

import lombok.Data;

@Data
public class MessageEvent implements UserEvent {
  private final Long user;
  private final Long friend;
  private final String message;
}
