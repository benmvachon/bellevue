package com.village.bellevue.model;

import java.sql.Timestamp;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.error.AuthorizationException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForumModel {

  private Long id;

  private String category;
  private String name;

  private ProfileModel user;
  private Timestamp created;

  private boolean favorite = false;

  public ForumModel(ForumEntity forum, ForumModelProvider helper) throws AuthorizationException {
    if (!helper.canReadForum(forum)) {
      throw new AuthorizationException("Not authorized");
    }
    this.id = forum.getId();
    this.category = forum.getCategory();
    this.name = forum.getName();
    this.user = helper.getProfile(forum.getUser()).orElse(null);
    this.created = forum.getCreated();
    this.favorite = helper.isFavorite(forum);
  }
}
