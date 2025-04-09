package com.village.bellevue.service;

import java.util.Optional;

import org.springframework.data.domain.Page;

import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.PostModel;

public interface PostService {

  public PostModel create(PostEntity post) throws AuthorizationException ;

  public Optional<PostModel> read(Long id) throws AuthorizationException;

  public Page<PostModel> readAllByForum(Long forum, int page, int size) throws AuthorizationException;

  public Page<PostModel> readAllByParent(Long parent, int page, int size) throws AuthorizationException;

  public boolean delete(Long id) throws AuthorizationException;

  public boolean canRead(Long id);
}
