package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class AcceptanceEvent implements UserEvent {
  private final Long user;
  private final Long friend;
}
