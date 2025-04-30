package com.village.bellevue.model;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.ProfileEntity.Status;
import com.village.bellevue.entity.UserProfileEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileModel {

  private Long id;

  private String name;
  private String username;

  private Status status = Status.OFFLINE;

  private String avatar = "cat";

  private Map<String, String> equipment = new HashMap<>();

  private ForumEntity location = null;

  private Timestamp lastSeen = new Timestamp(System.currentTimeMillis());
  private String blackboard = "";

  private String friendshipStatus;

  private boolean favorite = false;

  public ProfileModel(UserProfileEntity profile, ProfileModelProvider helper) {
    this.id = profile.getUser();
    this.name = profile.getName();
    this.username = profile.getUsername();
    this.status = profile.getStatus();
    this.avatar = profile.getAvatar();
    this.equipment = profile.getEquipment();
    this.location = profile.getLocation();
    this.lastSeen = profile.getLastSeen();
    this.blackboard = profile.getBlackboard();

    Optional<String> friendshipStatusOption = helper.getFriendshipStatus(id);
    if (friendshipStatusOption.isPresent()) {
      this.friendshipStatus = friendshipStatusOption.get();
    }

    this.favorite = helper.isFavorite(profile.getUser());
  }

  public ProfileModel(UserProfileEntity profile) {
    this.id = profile.getUser();
    this.name = profile.getName();
    this.username = profile.getUsername();
    this.status = profile.getStatus();
    this.avatar = profile.getAvatar();
    this.equipment = profile.getEquipment();
    this.location = profile.getLocation();
    this.lastSeen = profile.getLastSeen();
    this.blackboard = profile.getBlackboard();
  }
}
