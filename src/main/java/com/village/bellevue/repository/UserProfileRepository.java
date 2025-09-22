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
    "AND u.id != 1"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findByNameOrUsernameStartsWith(@Param("user") Long user, @Param("prefix") String prefix, Pageable pageable);

  @Query(
    "SELECT u.id FROM UserProfileEntity u " +
    "JOIN FriendEntity f ON u.id = f.friend.id " +
    "WHERE u.location = :location " +
    "AND u.locationType = :location_type " +
    "AND f.user = :user " +
    "AND u.id != 1 " +
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
    "AND u.id != 1 " +
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
    "AND u.id != 1 " +
    "ORDER BY u.lastSeen DESC"
  )
  @Transactional(readOnly = true)
  Page<UserProfileEntity> findAllNonFriendsByLocation(@Param("user") Long user, @Param("location") Long location, @Param("location_type") LocationType locationType, Pageable pageable);
}
