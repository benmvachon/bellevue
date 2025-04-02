package com.village.bellevue.entity;

import com.village.bellevue.entity.id.FavoriteId;

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
@Table(name = "favorite")
@IdClass(FavoriteId.class)
public class FavoriteEntity {
  @Id
  private Long user;

  @Id
  @ManyToOne(optional = false)
  @JoinColumn(name = "recipe", nullable = false)
  private RecipeEntity recipe;
}
