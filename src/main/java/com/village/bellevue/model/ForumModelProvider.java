package com.village.bellevue.model;

import java.util.Optional;

import com.village.bellevue.entity.ForumEntity;

@FunctionalInterface
public interface ForumModelProvider  {
  default boolean canReadForum(ForumEntity forum) {
    return false;
  };

  default boolean isFavorite(ForumEntity forum) {
    return false;
  };

  default Long getUnreadCount(ForumEntity forum) {
    return 0l;
  };

  default boolean isNotify(ForumEntity forum) {
    return false;
  };

  Optional<ProfileModel> getProfile(Long user);
}
