package com.village.bellevue.service;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.FriendEntity.FriendshipStatus;
import com.village.bellevue.entity.ScrubbedUserEntity;
import com.village.bellevue.error.FriendshipException;
import java.util.Optional;
import org.springframework.data.domain.Page;

public interface FriendService {

  public void request(Long user) throws FriendshipException;

  public Optional<ScrubbedUserEntity> read(Long user) throws FriendshipException;

  public Optional<FriendshipStatus> getStatus(Long user) throws FriendshipException;

  public boolean isFriend(Long user) throws FriendshipException;

  public boolean isBlockedBy(Long user) throws FriendshipException;

  public Page<FriendEntity> readAll(Long user, int page, int size) throws FriendshipException;

  public void accept(Long user) throws FriendshipException;

  public void block(Long user) throws FriendshipException;

  public void remove(Long user) throws FriendshipException;
}
