package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;

public interface ForumService {

  public ForumEntity create(ForumEntity forum);

  public Optional<ForumEntity> read(Long id) throws AuthorizationException;
  
  public Page<String> readAllCategories(int page, int size);

  public Page<ForumEntity> readAllByCategory(String category, int page, int size);

  public Page<ForumEntity> readAll(int page, int size);

  public boolean delete(Long id) throws AuthorizationException;

  public boolean canRead(Long id);
}
