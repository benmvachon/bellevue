package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.RatingEntity;
import com.village.bellevue.entity.id.RatingId;

@Repository
public interface RatingRepository extends JpaRepository<RatingEntity, RatingId> {

  @Query(
    "SELECT DISTINCT r FROM RatingEntity r " +
    "LEFT JOIN FriendEntity f ON r.user = f.friend.id " +
    "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.user = :user) " +
    "AND r.post = :post " +
    "ORDER BY r.updated DESC"
  )
  Page<RatingEntity> findByPostId(@Param("post") Long post, @Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT DISTINCT r FROM RatingEntity r " +
    "LEFT JOIN FriendEntity f ON r.user = f.friend.id " +
    "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.user = :user) " +
    "AND r.user = :friend " +
    "ORDER BY r.updated DESC"
  )
  Page<RatingEntity> findByFriendId(@Param("friend") Long friend, @Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT DISTINCT r FROM RatingEntity r " +
    "LEFT JOIN FriendEntity f ON r.user = f.friend.id " +
    "WHERE ((f.status = 'accepted' AND f.user = :user) OR r.user = :user) " +
    "ORDER BY r.updated DESC"
  )
  Page<RatingEntity> findAll(@Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END FROM RatingEntity r " +
    "JOIN FriendEntity f1 ON r.user = f1.friend.id " +
    "JOIN FriendEntity f2 ON r.post = f2.friend.id " +
    "WHERE ((" +
    "  f1.status = 'accepted' AND f1.user = :user AND " +
    "  f2.status = 'accepted' AND f2.user = :user" +
    ") OR " +
    "  r.user = :user OR r.post = :user" +
    ") AND " +
    "r.post = :post AND r.user = :friend"
  )
  Boolean canRead(@Param("post") Long post, @Param("friend") Long friend, @Param("user") Long user);
}
