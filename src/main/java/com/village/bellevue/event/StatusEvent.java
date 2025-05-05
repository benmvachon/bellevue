package com.village.bellevue.event;

import lombok.Data;

@Data
public class StatusEvent implements UserEvent {
  private final Long user;
  private final String status;
}
