package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.village.bellevue.entity.UserProfileEntity;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

  UserProfileEntity findByUsername(String username);

  @Query(
    "SELECT u FROM UserProfileEntity u " +
    "JOIN FriendEntity f ON u.id = f.friend.id " +
    "WHERE u.location.id = :forum " +
    "AND f.user = :user " +
    "AND f.status = 'accepted' " +
    "ORDER BY u.lastSeen DESC")
  Page<UserProfileEntity> findAllFriendsByLocation(@Param("user") Long user, @Param("forum") Long forum, Pageable pageable);

  @Query(
    "SELECT u FROM UserProfileEntity u " +
    "JOIN FriendEntity f ON u.id = f.friend.id " +
    "WHERE u.location.id = :forum " +
    "AND f.user = :user " +
    "AND f.status != 'accepted' " +
    "AND f.status != 'blocked_them' " +
    "AND f.status != 'blocked_you' " +
    "ORDER BY u.lastSeen DESC")
  Page<UserProfileEntity> findAllNonFriendsByLocation(@Param("user") Long user, @Param("forum") Long forum, Pageable pageable);
}
