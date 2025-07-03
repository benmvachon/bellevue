package com.village.bellevue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.error.FriendshipException;
import com.village.bellevue.model.ProfileModel;

public interface FriendService {

  public void request(Long user) throws FriendshipException;

  public Optional<String> getStatus(Long user) throws FriendshipException;

  public boolean isFriend(Long user) throws FriendshipException;

  public Page<ProfileModel> readAll(String query, List<Long> excluded, int page, int size) throws FriendshipException;

  public Page<ProfileModel> readSuggestions(int page, int size) throws FriendshipException;

  public Page<ProfileModel> readAll(Long user, String query, int page, int size) throws FriendshipException;

  public void accept(Long user) throws FriendshipException;

  public void remove(Long user) throws FriendshipException;
}
