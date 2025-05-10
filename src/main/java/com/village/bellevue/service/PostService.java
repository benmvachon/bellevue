package com.village.bellevue.service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

import com.village.bellevue.error.AuthorizationException;
import com.village.bellevue.model.PostModel;

public interface PostService {

  public PostModel post(Long forum, String content) throws AuthorizationException;

  public PostModel reply(Long forum, Long parent, String content) throws AuthorizationException;

  public Optional<PostModel> read(Long id) throws AuthorizationException;

  public List<PostModel> readAllByForum(Long forum, Timestamp cursor, Long limit) throws AuthorizationException;

  public Long countAllByForum(Long forum) throws AuthorizationException;

  public List<PostModel> readAllByParent(Long parent, Timestamp cursor, Long limit) throws AuthorizationException;

  public Long countAllByParent(Long parent) throws AuthorizationException;

  public List<PostModel> readOthersByParent(Long parent, Long child, Timestamp cursor, Long limit) throws AuthorizationException;

  public Long countOthersByParent(Long parent, Long child) throws AuthorizationException;

  public boolean delete(Long id) throws AuthorizationException;

  public boolean canRead(Long id);
}
