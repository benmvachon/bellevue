package com.village.bellevue.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.UserProfileEntity;
import com.village.bellevue.entity.ProfileEntity.LocationType;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

  @Query(
    "SELECT u FROM UserProfileEntity u " +
    "WHERE (LOWER(u.name) LIKE LOWER(CONCAT(:prefix, '%')) " +
    "   OR LOWER(u.username) LIKE LOWER(CONCAT(:prefix, '%'))) " +
    "AND u.id NOT IN (" +
    "   SELECT f.friend.id FROM FriendEntity f " +
    "   WHERE f.user = :user AND (f.status = 'BLOCKED_THEM' OR f.status = 'ACCEPTED') " +
    "   UNION " +
    "   SELECT f.user FROM FriendEntity f " +
    "   WHERE f.friend.id = :user AND (f.status = 'BLOCKED_YOU' OR f.status = 'ACCEPTED')" +
    ")"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findByNameOrUsernameStartsWith(@Param("user") Long user, @Param("prefix") String prefix, Pageable pageable);

  @Query(
    "SELECT u.id FROM UserProfileEntity u " +
    "JOIN FriendEntity f ON u.id = f.friend.id " +
    "WHERE u.location = :location " +
    "AND u.locationType = :location_type " +
    "AND f.user = :user " +
    "AND f.status != 'BLOCKED_THEM' " +
    "AND f.status != 'BLOCKED_YOU' " +
    "ORDER BY u.lastSeen DESC"
  )
  @Transactional(readOnly = true)
  List<Long> findAllUsersByLocation(@Param("user") Long user, @Param("location") Long location, @Param("location_type") LocationType locationType);

  @Query(
    "SELECT u FROM UserProfileEntity u " +
    "JOIN FriendEntity f ON u.id = f.friend.id " +
    "WHERE u.location = :location " +
    "AND u.locationType = :location_type " +
    "AND f.user = :user " +
    "AND f.status = 'ACCEPTED' " +
    "ORDER BY u.lastSeen DESC"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findAllFriendsByLocation(@Param("user") Long user, @Param("location") Long location, @Param("location_type") LocationType locationType, Pageable pageable);

  @Query(
    "SELECT u FROM UserProfileEntity u " +
    "JOIN FriendEntity f ON u.id = f.friend.id " +
    "WHERE u.location = :location " +
    "AND u.locationType = :location_type " +
    "AND f.user = :user " +
    "AND f.status != 'ACCEPTED' " +
    "AND f.status != 'BLOCKED_THEM' " +
    "AND f.status != 'BLOCKED_YOU' " +
    "ORDER BY u.lastSeen DESC"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findAllNonFriendsByLocation(@Param("user") Long user, @Param("location") Long location, @Param("location_type") LocationType locationType, Pageable pageable);
}
