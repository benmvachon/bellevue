package com.village.bellevue.model;

import java.util.Optional;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.PostEntity;

@FunctionalInterface
public interface PostModelProvider {
  Optional<AggregateRatingEntity> getAggregateRating(Long postId);

  default Long getChildrenCount(Long postId) {
    return 0L;
  }

  default boolean canReadPost(PostEntity post) {
    return false;
  }
}
