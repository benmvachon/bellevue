package com.village.bellevue.entity.id;

import java.io.Serializable;
import java.util.Objects;

import com.village.bellevue.entity.SimpleRecipeEntity;

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
public class FavoriteId implements Serializable {

  private Long user;
  private SimpleRecipeEntity recipe;

  public FavoriteId(Long user, Long recipe) {
    this.user = user;
    this.recipe = new SimpleRecipeEntity();
    this.recipe.setId(recipe);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof FavoriteId)) {
      return false;
    }
    FavoriteId that = (FavoriteId) o;
    return Objects.equals(user, that.user) && Objects.equals(recipe.getId(), that.recipe.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(user, recipe.getId());
  }
}
