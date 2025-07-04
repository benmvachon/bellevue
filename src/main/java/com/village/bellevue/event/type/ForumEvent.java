package com.village.bellevue.event.type;

import com.village.bellevue.model.ForumModel;

import lombok.Data;

@Data
public class ForumEvent implements UserEvent {
  private final Long user;
  private final ForumModel forum;
}
