package com.village.bellevue.model;

import java.util.Optional;

@FunctionalInterface
public interface ProfileModelProvider {
  Optional<String> getFriendshipStatus(Long user);
}
