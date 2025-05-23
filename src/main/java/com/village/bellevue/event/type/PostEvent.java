package com.village.bellevue.event.type;

import com.village.bellevue.model.PostModel;

import lombok.Data;

@Data
public class PostEvent implements UserEvent {
  private final Long user;
  private final PostModel post;
}
