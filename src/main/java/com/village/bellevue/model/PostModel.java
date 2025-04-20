package com.village.bellevue.model;

import java.sql.Timestamp;
import java.util.Optional;

import com.village.bellevue.entity.AggregateRatingEntity;
import com.village.bellevue.entity.PostEntity;
import com.village.bellevue.error.AuthorizationException;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostModel {

  private Long id;

  private ProfileModel user;
  private PostModel parent;
  private ForumModel forum;

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
    this.user = helper.getProfile(post.getUser());
    this.forum = helper.getForum(post.getForum());
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
}
