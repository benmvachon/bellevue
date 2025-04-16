package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;

public interface UserProfileService {

  Optional<UserProfileEntity> read(Long user) throws FriendshipException;

  Page<UserProfileEntity> readByNamePrefix(String prefix, int page, int size);

  Page<UserProfileEntity> readFriendsByLocation(Long forum, int page, int size);

  Page<UserProfileEntity> readNonFriendsByLocation(Long forum, int page, int size);
}
