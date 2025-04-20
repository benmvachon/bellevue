package com.village.bellevue.model;

import java.util.Optional;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;

public interface PostModelProvider {
  Optional<AggregateRatingEntity> getAggregateRating(Long post);

  Long getChildrenCount(Long post);

  boolean canReadPost(PostEntity post);

  ProfileModel getProfile(UserProfileEntity user);

  ForumModel getForum(ForumEntity forum) throws AuthorizationException;
}
