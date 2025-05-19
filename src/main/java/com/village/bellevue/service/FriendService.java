package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;

public interface FriendService {

  public void request(Long user) throws FriendshipException;

  public Optional<String> getStatus(Long user) throws FriendshipException;

  public boolean isFriend(Long user) throws FriendshipException;

  public boolean isBlockedBy(Long user) throws FriendshipException;

  public Page<ProfileModel> readAll(int page, int size) throws FriendshipException;

  public Page<ProfileModel> readSuggestions(int page, int size) throws FriendshipException;

  public Page<ProfileModel> readAll(Long user, int page, int size) throws FriendshipException;

  public void accept(Long user) throws FriendshipException;

  public void block(Long user) throws FriendshipException;

  public void remove(Long user) throws FriendshipException;
}
