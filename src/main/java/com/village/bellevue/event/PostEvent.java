package com.village.bellevue.event;

import com.village.bellevue.model.PostModel;

import lombok.Data;

@Data
public class PostEvent {
  private final Long user;
  private final PostModel post;
}
