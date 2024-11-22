package com.village.bellevue.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.RecipeEntity;
import com.village.bellevue.entity.SimpleRecipeEntity;

@Repository
public interface RecipeRepository extends JpaRepository<RecipeEntity, Long> {

    @Query("SELECT DISTINCT r "
            + "FROM RecipeEntity r "
            + "JOIN FriendEntity f ON r.author.id = f.friend.id "
            + "LEFT JOIN r.ingredients i " // Assuming there's a relationship in SimpleRecipeEntity
            + "LEFT JOIN r.equipment e " // Assuming there's a relationship in SimpleRecipeEntity
            + "LEFT JOIN r.skills s " // Assuming there's a relationship in SimpleRecipeEntity
            + "WHERE LOWER(r.name) LIKE LOWER(CONCAT(:prefix, '%')) "
            + "AND (r.author.id = :user OR (f.user = :user AND f.status = 'accepted')) "
            + "AND (:ingredients IS NULL OR i.id IN :ingredients) "
            + "AND (:equipment IS NULL OR e.id IN :equipment) "
            + "AND (:skills IS NULL OR s.id IN :skills)")
    Page<SimpleRecipeEntity> findByNameStartingWithIgnoreCase(
            @Param("prefix") String prefix,
            @Param("user") Long user,
            @Param("ingredients") List<Long> ingredients,
            @Param("equipment") List<Long> equipment,
            @Param("skills") List<Long> skills,
            Pageable pageable
    );

    @Query("SELECT r FROM SimpleRecipeEntity r WHERE r.author.id = :author")
    Page<SimpleRecipeEntity> findByAuthorId(@Param("author") Long author, Pageable pageable);

    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END "
            + "FROM RecipeEntity r "
            + "JOIN FriendEntity f ON f.friend.id = r.author.id "
            + "WHERE (r.author.id = :user OR f.user = :user) "
            + "AND f.status = 'accepted' "
            + "AND r.id = :recipe")
    boolean canRead(@Param("recipe") Long recipe, @Param("user") Long user);

    @Query("SELECT COUNT(r) > 0 "
            + "FROM RecipeEntity r "
            + "WHERE r.id = :recipe AND r.author.id = :user")
    boolean canUpdate(@Param("recipe") Long recipe, @Param("user") Long user);

}
