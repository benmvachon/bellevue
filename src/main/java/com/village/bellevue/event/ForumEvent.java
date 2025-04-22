package com.village.bellevue.event;

import com.village.bellevue.model.ForumModel;

import lombok.Data;

@Data
public class ForumEvent {
  private final Long user;
  private final ForumModel forum;
}
