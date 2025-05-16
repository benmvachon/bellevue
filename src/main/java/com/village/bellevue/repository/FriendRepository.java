package com.village.bellevue.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.FriendEntity;
import com.village.bellevue.entity.id.FriendId;

@Repository
public interface FriendRepository extends JpaRepository<FriendEntity, FriendId> {
  
  @Query("SELECT f.friend.id FROM FriendEntity f WHERE f.user = :user AND f.status = 'accepted'")
  @Transactional(readOnly = true)
  List<Long> findFriends(@Param("user") Long user);

  @Query(
    "SELECT f.friend.id FROM FriendEntity f " +
    "WHERE f.user = :user AND f.status = 'accepted' " +
    "AND EXISTS ( " +
    "SELECT 1 FROM FriendEntity f2 " +
    "WHERE f2.user = :friend AND f2.friend.id = f.friend.id AND f2.status = 'accepted')"
  )
  @Transactional(readOnly = true)
  List<Long> findMutualFriends(@Param("user") Long user, @Param("friend") Long friend);

  @Query("SELECT f FROM FriendEntity f WHERE status = 'accepted' AND f.user = :user ORDER BY f.updated DESC")
  @Transactional(readOnly = true)
  Page<FriendEntity> findFriends(@Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT f FROM FriendEntity f WHERE f.user = :friend AND status = 'accepted' AND f.friend.id NOT IN " +
    "(SELECT b.friend.id FROM FriendEntity b WHERE (b.user = :user AND b.status = 'blocked_them') OR (b.user = :user AND b.status = 'blocked_you')) " +
    "ORDER BY f.updated DESC"
  )
  @Transactional(readOnly = true)
  Page<FriendEntity> findFriendsExcludingBlocked(@Param("friend") Long friend, @Param("user") Long user, Pageable pageable);

  @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM FriendEntity f WHERE f.friend.id = :friend AND f.user = :user AND status = 'accepted'")
  @Transactional(readOnly = true)
  Boolean areFriends(@Param("friend") Long friend, @Param("user") Long user);
}
