package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

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
public class RatingId implements Serializable {

  private Long post;
  private Long user;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RatingId)) {
      return false;
    }
    RatingId that = (RatingId) o;
    return Objects.equals(post, that.post) && Objects.equals(user, that.user);
  }

  @Override
  public int hashCode() {
    return Objects.hash(post, user);
  }
}
