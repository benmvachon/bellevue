package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.FavoriteEntity.FavoriteType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class FavoriteId implements Serializable {
  private Long user;
  private FavoriteType type;
  private Long entity;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof FavoriteId)) return false;
    FavoriteId that = (FavoriteId) o;
    return Objects.equals(user, that.user) && Objects.equals(type, that.type) && Objects.equals(entity, that.entity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, type, entity);
  }
}
