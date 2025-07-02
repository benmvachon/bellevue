package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class NewUserEvent implements UserEvent {
  private final Long user;
}
