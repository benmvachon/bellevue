package com.village.bellevue.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.id.FriendId;
import com.village.bellevue.model.SuggestedFriendModel;

@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, FriendId> {
  
  @Query("SELECT f.friend.id FROM FriendEntity f WHERE f.user = :user AND f.status = 'ACCEPTED'")
  @Transactional(readOnly = true)
  List<Long> findFriends(@Param("user") Long user);

  @Query(
    "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendEntity f " +
    "WHERE f.user IN :users AND f.friend.id IN :users AND (f.status = 'BLOCKED_YOU' OR f.status = 'BLOCKED_THEM')"
  )
  @Transactional(readOnly = true)
  Boolean containsBlockingUsers(@Param("users") List<Long> users);

  @Query(
    "SELECT f.friend FROM FriendEntity f " +
    "WHERE f.user = :user AND f.status = 'ACCEPTED' " +
    "AND (:excluded IS NULL OR (f.friend.id NOT IN :excluded " +
    "AND NOT EXISTS (" +
      "SELECT 1 FROM FriendEntity f2 " +
      "WHERE f2.user = f.friend.id AND f2.friend.id IN :excluded AND (f2.status = 'BLOCKED_YOU' OR f2.status = 'BLOCKED_THEM')" +
    "))) " +
    "AND (:query IS NULL " +
      "OR LOWER(f.friend.name) LIKE LOWER(CONCAT(:query, '%')) " +
      "OR LOWER(f.friend.username) LIKE LOWER(CONCAT(:query, '%'))) " +
    "ORDER BY f.score DESC"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findFriends(@Param("user") Long user, @Param("query") String query, @Param("excluded") List<Long> excluded, Pageable pageable);

  @Query(
    "SELECT f.friend.id FROM FriendEntity f " +
    "WHERE f.user = :user AND f.status = 'ACCEPTED' " +
    "AND EXISTS ( " +
    "SELECT 1 FROM FriendEntity f2 " +
    "WHERE f2.user = :friend AND f2.friend.id = f.friend.id AND f2.status = 'ACCEPTED') "
  )
  @Transactional(readOnly = true)
  List<Long> findMutualFriends(@Param("user") Long user, @Param("friend") Long friend);

  @Query(
    "SELECT f.friend FROM FriendEntity f " +
    "WHERE f.user = :friend AND f.status = 'ACCEPTED' AND f.friend.id NOT IN " +
    "(SELECT b.friend.id FROM FriendEntity b WHERE (b.user = :user AND b.status = 'BLOCKED_THEM') OR (b.user = :user AND b.status = 'BLOCKED_YOU')) " +
    "AND (:query IS NULL " +
      "OR LOWER(f.friend.name) LIKE LOWER(CONCAT(:query, '%')) " +
      "OR LOWER(f.friend.username) LIKE LOWER(CONCAT(:query, '%'))) " +
    "ORDER BY f.score DESC"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findFriendsExcludingBlocked(@Param("friend") Long friend, @Param("user") Long user, @Param("query") String query, Pageable pageable);

  @Query("""
    SELECT new com.village.bellevue.model.SuggestedFriendModel(f2.friend, SUM(f1.score + f2.score))
    FROM FriendEntity f1
    JOIN FriendEntity f2 ON f1.friend.id = f2.user
    WHERE f1.user = :user
      AND f1.status = 'ACCEPTED'
      AND f2.status = 'ACCEPTED'
      AND f2.friend.id != :user
      AND f2.friend.id NOT IN (
          SELECT f.friend.id FROM FriendEntity f
          WHERE f.user = :user AND (f.status = 'ACCEPTED' OR f.status = 'BLOCKED_YOU' OR f.status = 'BLOCKED_THEM')
      )
    GROUP BY f2.friend.id
    ORDER BY SUM(f1.score + f2.score) DESC
    """
  )
  Page<SuggestedFriendModel> findSuggestedFriends(@Param("user") Long user, Pageable pageable);

  @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendEntity f WHERE f.friend.id = :friend AND f.user = :user AND status = 'ACCEPTED'")
  @Transactional(readOnly = true)
  Boolean areFriends(@Param("friend") Long friend, @Param("user") Long user);

  @Modifying
  @Query("UPDATE FriendEntity f SET f.score = f.score + 1 WHERE f.friend.id = :friend AND f.user = :user AND status = 'ACCEPTED'")
  @Transactional
  int incrementFriendshipScore(@Param("friend") Long friend, @Param("user") Long user);
}
