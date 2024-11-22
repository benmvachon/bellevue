package com.village.bellevue.repository;

import com.village.bellevue.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {

  @Query(
      "SELECT DISTINCT r FROM ReviewEntity r "
          + "LEFT JOIN FriendEntity f ON r.author.id = f.friend.id "
          + "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.author.id = :user) "
          + "AND r.recipe.id = :recipe "
          + "ORDER BY r.updated DESC")
  Page<ReviewEntity> findByRecipeId(
      @Param("recipe") Long recipe, @Param("user") Long user, Pageable pageable);

  @Query(
      "SELECT DISTINCT r FROM ReviewEntity r "
          + "LEFT JOIN FriendEntity f ON r.author.id = f.friend.id "
          + "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.author.id = :user) "
          + "AND r.author.id = :author "
          + "ORDER BY r.updated DESC")
  Page<ReviewEntity> findByAuthorId(
      @Param("author") Long author, @Param("user") Long user, Pageable pageable);

  @Query(
      "SELECT DISTINCT r FROM ReviewEntity r "
          + "LEFT JOIN FriendEntity f ON r.author.id = f.friend.id "
          + "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.author.id = :user) "
          + "AND r.author.id = :user "
          + "AND r.review IS NULL "
          + "ORDER BY r.updated DESC")
  Page<ReviewEntity> findIncomplete(@Param("user") Long user, Pageable pageable);

  @Query(
      "SELECT DISTINCT r FROM ReviewEntity r "
          + "LEFT JOIN FriendEntity f ON r.author.id = f.friend.id "
          + "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.author.id = :user) "
          + "ORDER BY r.updated DESC")
  Page<ReviewEntity> findAll(@Param("user") Long user, Pageable pageable);

  @Query(
      "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ReviewEntity r WHERE r.id = :review AND r.author.id = :user")
  boolean canUpdate(@Param("review") Long review, @Param("user") Long user);

  @Query(
      "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM ReviewEntity r "
          + "JOIN FriendEntity f1 ON r.author.id = f1.friend.id "
          + "JOIN FriendEntity f2 ON r.recipe.author.id = f2.friend.id "
          + "WHERE ((f1.status = 'accepted' AND f1.user = :user "
          + "AND f2.status = 'accepted' AND f2.user = :user) "
          + "OR r.author.id = :user OR r.recipe.author.id = :user) "
          + "AND r.id = :review")
  boolean canRead(@Param("review") Long review, @Param("user") Long user);
}
