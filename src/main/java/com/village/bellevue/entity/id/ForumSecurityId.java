package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.UserProfileEntity;

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
public class ForumSecurityId implements Serializable {

  private Long forum;
  private UserProfileEntity user;

  public ForumSecurityId(Long forum, Long user) {
    this.forum = forum;
    this.user = new UserProfileEntity();
    this.user.setUser(user);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ForumSecurityId)) {
      return false;
    }
    ForumSecurityId that = (ForumSecurityId) o;
    return Objects.equals(forum, that.forum) && Objects.equals(user.getUser(), that.user.getUser());
  }

  @Override
  public int hashCode() {
    return Objects.hash(forum, user.getUser());
  }
}
