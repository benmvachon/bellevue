package com.village.bellevue.event.type;

import com.village.bellevue.entity.RatingEntity.Star;

import lombok.Data;

@Data
public class RatingEvent implements UserEvent {
  private final Long user;
  private final Long post;
  private final Long postAuthor;
  private final Long forum;
  private final Star rating;
}
