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
public class NotificationSettingId implements Serializable {

  private Long user;
  private Long forum;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof NotificationSettingId)) {
      return false;
    }
    NotificationSettingId that = (NotificationSettingId) o;
    return Objects.equals(user, that.user) && Objects.equals(forum, that.forum);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, forum);
  }
}
