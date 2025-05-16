package com.village.bellevue.event;

import lombok.Data;

@Data
public class PopularityEvent implements UserEvent {
  private final Long user;
  private final Long post;
  private final Long parent;
  private final Long forum;
}
