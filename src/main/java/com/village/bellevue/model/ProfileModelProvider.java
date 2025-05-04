package com.village.bellevue.model;

import java.util.Optional;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;

public interface ProfileModelProvider {
  default boolean isFavorite(Long user) {
    return false;
  };

  UserProfileEntity getProfileLocation(Long location);
  ForumEntity getForumLocation(Long location);
  PostEntity getPostLocation(Long location);

  Optional<String> getFriendshipStatus(Long user);
}
