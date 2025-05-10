package com.village.bellevue.event;

import com.village.bellevue.entity.MessageEntity;

import lombok.Data;

@Data
public class MessageEvent implements UserEvent {
  private final Long user;
  private final Long friend;
  private final MessageEntity message;
}
