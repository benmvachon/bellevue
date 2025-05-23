package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class RequestEvent implements UserEvent {
  private final Long user;
  private final Long friend;
}
