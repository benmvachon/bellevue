package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.ForumEntity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class ForumTagId implements Serializable {

  private String tag;
  private ForumEntity forum;

  public ForumTagId(String tag, Long forum) {
    this.tag = tag;
    this.forum = new ForumEntity();
    this.forum.setId(forum);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ForumTagId)) {
      return false;
    }
    ForumTagId that = (ForumTagId) o;
    return Objects.equals(tag, that.tag) && Objects.equals(forum.getId(), that.forum.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(tag, forum.getId());
  }
}
