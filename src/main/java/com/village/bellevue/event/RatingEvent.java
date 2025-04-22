package com.village.bellevue.event;

import com.village.bellevue.entity.RatingEntity.Star;

import lombok.Data;

@Data
public class RatingEvent {
  private final Long user;
  private final Long post;
  private final Long postAuthor;
  private final Star rating;
}
