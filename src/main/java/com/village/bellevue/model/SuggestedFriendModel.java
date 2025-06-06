package com.village.bellevue.model;

import com.village.bellevue.entity.UserProfileEntity;

import lombok.Data;

@Data
public class SuggestedFriendModel {
  private final UserProfileEntity friend;
  private final Long score;
}
