package com.village.bellevue.entity;

import com.village.bellevue.entity.id.RecipeSkillId;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "recipe_skill")
@IdClass(RecipeSkillId.class)
public class RecipeSkillEntity {

    @Id
    private Long recipe;

    @Id
    @ManyToOne
    @JoinColumn(name = "skill", nullable = false)
    private SkillEntity skill;
}
