package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class FriendRemovalEvent implements UserEvent {
  private final Long user;
  private final Long friend;
}
