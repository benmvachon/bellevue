package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class PostDeleteEvent implements UserEvent {
  private final Long user;
  private final Long post;
  private final Long parent;
  private final Long forum;
}
