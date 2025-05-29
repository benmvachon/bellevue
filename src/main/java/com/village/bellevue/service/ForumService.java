package com.village.bellevue.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.ForumModel;

public interface ForumService {

  public ForumModel create(ForumEntity forum) throws AuthorizationException;

  public Optional<ForumModel> read(Long id) throws AuthorizationException;

  public Page<ForumModel> readAll(int page, int size);

  public List<ForumModel> readAll(String query);

  public Page<ForumModel> readAllWithUnreadPosts(int page, int size);

  public boolean turnOnNotifications(Long forum) throws AuthorizationException;

  public boolean turnOffNotifications(Long forum) throws AuthorizationException;

  public boolean delete(Long id) throws AuthorizationException;

  public boolean canRead(Long id);
}
