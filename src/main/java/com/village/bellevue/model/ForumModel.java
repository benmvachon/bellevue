package com.village.bellevue.model;

import java.sql.Timestamp;
import java.util.List;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForumModel {

  private Long id;
  private String name;
  private String description;

  private ProfileModel user;
  private Timestamp created;
  private List<String> tags;
  private List<UserProfileEntity> users;

  private boolean favorite = false;

  private Long unreadCount;

  private boolean notify = false;

  public ForumModel(ForumEntity forum, ForumModelProvider helper) throws AuthorizationException {
    if (!helper.canReadForum(forum)) {
      throw new AuthorizationException("Not authorized");
    }
    this.id = forum.getId();
    this.name = forum.getName();
    this.description = forum.getDescription();
    this.user = helper.getProfile(forum.getUser()).orElse(null);
    this.created = forum.getCreated();
    this.favorite = helper.isFavorite(forum);
    this.unreadCount = helper.getUnreadCount(forum);
    this.notify = helper.isNotify(forum);
    this.tags = forum.getTags();
    this.users = forum.getUsers();
  }
}
