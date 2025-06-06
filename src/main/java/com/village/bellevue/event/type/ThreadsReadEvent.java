package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class ThreadsReadEvent implements UserEvent {
  private final Long user;
}
