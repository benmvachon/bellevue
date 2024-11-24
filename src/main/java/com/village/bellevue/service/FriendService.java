package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.error.FriendshipException;

public interface FriendService {

  public void request(Long user) throws FriendshipException;

  public Optional<ScrubbedUserEntity> read(Long user) throws FriendshipException;

  public Optional<String> getStatus(Long user) throws FriendshipException;

  public boolean isFriend(Long user) throws FriendshipException;

  public boolean isBlockedBy(Long user) throws FriendshipException;

  public Page<FriendEntity> readAll(Long user, int page, int size) throws FriendshipException;

  public void accept(Long user) throws FriendshipException;

  public void block(Long user) throws FriendshipException;

  public void remove(Long user) throws FriendshipException;
}
