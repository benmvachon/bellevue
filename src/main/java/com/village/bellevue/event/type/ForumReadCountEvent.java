package com.village.bellevue.event.type;

import lombok.Data;

@Data
public class ForumReadCountEvent implements UserEvent {
  private final Long user; // user who marked the post as read
  private final Long forum; // the forum being marked as read
}
