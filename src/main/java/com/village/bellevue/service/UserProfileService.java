package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.ProfileEntity.LocationType;
import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;

public interface UserProfileService {

  Optional<ProfileModel> read(Long user) throws FriendshipException;

  Page<ProfileModel> readByNamePrefix(String prefix, int page, int size);

  Page<ProfileModel> readFriendsByLocation(Long location, LocationType locationType, int page, int size);

  Page<ProfileModel> readNonFriendsByLocation(Long location, LocationType locationType, int page, int size);

  void setLocation(Long location, LocationType locationType);

  void setBlackboard(String blackboard);
}
