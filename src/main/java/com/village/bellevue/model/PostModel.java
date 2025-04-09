package com.village.bellevue.model;

import java.sql.Timestamp;
import java.util.Optional;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.error.AuthorizationException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostModel {

  private Long id;

  private UserProfileEntity user;
  private PostModel parent;
  private ForumEntity forum;

  private String content;
  private Timestamp created;

  private Long children = 0L;
  private Double rating = 0.0;
  private Integer ratingCount = 0;

  public PostModel(PostEntity post, PostModelProvider helper) throws AuthorizationException {
    if (!helper.canReadPost(post)) {
      throw new AuthorizationException("Not authorized");
    }
    this.id = post.getId();
    this.user = post.getUser();
    this.forum = post.getForum();
    this.content = post.getContent();
    this.created = post.getCreated();

    this.children = helper.getChildrenCount(post.getId());

    Optional<AggregateRatingEntity> ratingOption = helper.getAggregateRating(post.getId());
    if (ratingOption.isPresent()) {
      this.rating = ratingOption.get().getRating();
      this.ratingCount = ratingOption.get().getRatingCount();
    }

    if (post.getParent() != null) {
      this.parent = new PostModel(post.getParent(), helper);
    }
  }

  public PostModel(PostEntity post, Optional<AggregateRatingEntity> rating, Long children) {
    this.id = post.getId();
    this.user = post.getUser();
    this.forum = post.getForum();
    this.content = post.getContent();
    this.created = post.getCreated();
    this.children = children;

    if (rating.isPresent()) {
      this.rating = rating.get().getRating();
      this.ratingCount = rating.get().getRatingCount();
    }
  }
}
