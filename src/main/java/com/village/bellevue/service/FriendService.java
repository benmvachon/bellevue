package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.FriendshipException;

public interface FriendService {

  public void request(Long user) throws FriendshipException;

  public Optional<UserProfileEntity> read(Long user) throws FriendshipException;

  public Optional<String> getStatus(Long user) throws FriendshipException;

  public boolean isFriend(Long user) throws FriendshipException;

  public boolean isBlockedBy(Long user) throws FriendshipException;

  Page<UserProfileEntity> readFriendByLocation(Long forum, int page, int size);

  Page<UserProfileEntity> readNonFriendsByLocation(Long forum, int page, int size);

  public Page<FriendEntity> readAll(Long user, int page, int size) throws FriendshipException;

  public void accept(Long user) throws FriendshipException;

  public void block(Long user) throws FriendshipException;

  public void remove(Long user) throws FriendshipException;
}
