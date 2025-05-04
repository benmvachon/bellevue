package com.village.bellevue.event;

import lombok.Data;

@Data
public class BlackboardEvent implements UserEvent {
  private final Long user;
  private final String blackboard;
}
