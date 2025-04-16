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

  @Query(
    "SELECT u FROM UserProfileEntity u " +
    "WHERE (LOWER(u.name) LIKE LOWER(CONCAT(:prefix, '%')) " +
    "   OR LOWER(u.username) LIKE LOWER(CONCAT(:prefix, '%'))) " +
    "AND u.id NOT IN (" +
    "   SELECT f.friend.id FROM FriendEntity f " +
    "   WHERE f.user = :user AND f.status = 'blocked_them' " +
    "   UNION " +
    "   SELECT f.user FROM FriendEntity f " +
    "   WHERE f.friend.id = :user AND f.status = 'blocked_you'" +
    ")"
  )
  Page<UserProfileEntity> findByNameOrUsernameStartsWith(
    @Param("user") Long user,
    @Param("prefix") String prefix,
    Pageable pageable
  );
  

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
