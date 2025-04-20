package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.ForumModel;

public interface ForumService {

  public ForumModel create(ForumEntity forum) throws AuthorizationException;

  public Optional<ForumModel> read(Long id) throws AuthorizationException;
  
  public Page<String> readAllCategories(int page, int size);

  public Page<ForumModel> readAllByCategory(String category, int page, int size);

  public Page<ForumModel> readAll(int page, int size);

  public boolean delete(Long id) throws AuthorizationException;

  public boolean canRead(Long id);
}
