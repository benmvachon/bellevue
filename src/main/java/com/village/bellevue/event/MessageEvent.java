package com.village.bellevue.event;

import lombok.Data;

@Data
public class MessageEvent {
  private final Long user;
  private final Long friend;
  private final String message;
}
