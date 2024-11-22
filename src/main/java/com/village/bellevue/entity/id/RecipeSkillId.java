package com.village.bellevue.entity.id;

import com.village.bellevue.entity.SkillEntity;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Embeddable
public class RecipeSkillId implements Serializable {

  private Long recipe;
  private SkillEntity skill;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RecipeSkillId)) {
      return false;
    }
    RecipeSkillId that = (RecipeSkillId) o;
    return Objects.equals(recipe, that.recipe) && Objects.equals(skill.getId(), that.skill.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(recipe, skill.getId());
  }
}
