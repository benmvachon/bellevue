package com.village.bellevue.model;

import java.util.Optional;

@FunctionalInterface
public interface ProfileModelProvider {
  default boolean isFavorite(Long user) {
    return false;
  };

  Optional<String> getFriendshipStatus(Long user);
}
