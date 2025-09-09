package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.error.ForumException;
import com.village.bellevue.model.ForumModel;

public interface ForumService {

  public ForumModel create(ForumEntity forum) throws AuthorizationException, ForumException;

  public Optional<ForumModel> read(Long id) throws AuthorizationException;

  public Page<ForumModel> readAll(String query, boolean unread, int page, int size, boolean includeTownHall);

  public ForumModel update(Long id, ForumEntity forum) throws AuthorizationException, ForumException;

  public boolean removeSelf(Long id) throws AuthorizationException, ForumException;

  public boolean turnOnNotifications(Long forum) throws AuthorizationException;

  public boolean turnOffNotifications(Long forum) throws AuthorizationException;

  public boolean delete(Long id) throws AuthorizationException;

  public boolean canRead(Long id);

  public boolean canUpdate(Long id);
}
