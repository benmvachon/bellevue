package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.id.FriendId;

@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, FriendId> {

  @Query(
      "SELECT f FROM FriendEntity f WHERE f.user = :friend AND status = 'accepted' AND f.friend.id NOT IN "
          + "(SELECT b.friend.id FROM FriendEntity b WHERE (b.user = :user AND b.status = 'blocked_them') OR (b.user = :user AND b.status = 'blocked_you'))")
  Page<FriendEntity> findFriendsExcludingBlocked(
      @Param("friend") Long friend, @Param("user") Long user, Pageable pageable);

  @Query(
      "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendEntity f WHERE f.friend.id = :friend AND f.user = :user AND status = 'accepted'")
  Boolean areFriends(@Param("user") Long friend, @Param("user") Long user);
}
