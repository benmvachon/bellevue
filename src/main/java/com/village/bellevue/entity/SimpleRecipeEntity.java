package com.village.bellevue.entity;

import java.sql.Timestamp;

import org.springframework.data.annotation.Immutable;

import com.village.bellevue.entity.RecipeEntity.RecipeCategory;
import com.village.bellevue.model.RecipeModel;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Immutable
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "simple_recipe")
public class SimpleRecipeEntity {

    @Id
    private Long id;

    @ManyToOne
    @JoinColumn(name = "author", nullable = false)
    private ScrubbedUserEntity author;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private RecipeCategory category;

    private Timestamp created = new Timestamp(System.currentTimeMillis());
    private Timestamp updated = new Timestamp(System.currentTimeMillis());

    public SimpleRecipeEntity(RecipeEntity recipe) {
        this(
                recipe.getId(),
                recipe.getAuthor(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getCreated(),
                recipe.getUpdated()
        );
    }

    public SimpleRecipeEntity(RecipeModel recipe) {
        this(
                recipe.getId(),
                recipe.getAuthor(),
                recipe.getName(),
                recipe.getCategory(),
                recipe.getCreated(),
                recipe.getUpdated()
        );
    }
}
