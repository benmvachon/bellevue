package com.village.bellevue.repository;

import java.sql.Timestamp;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.ProfileEntity;
import com.village.bellevue.entity.ProfileEntity.LocationType;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.status = 'ACTIVE' WHERE p.id = :user AND p.status != 'ACTIVE'")
  int setStatusOnline(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.status = 'OFFLINE', p.location = null, p.locationType = null WHERE p.id = :user AND p.status != 'OFFLINE'")
  int setStatusOffline(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.status = 'IDLE' WHERE p.id = :user AND p.status != 'IDLE'")
  int setStatusIdle(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.lastSeen = :lastSeen WHERE p.id = :user")
  void setLastSeen(Long user, Timestamp lastSeen);

  @Query("SELECT p.lastSeen FROM ProfileEntity p WHERE p.id = :user")
  Timestamp getLastSeen(Long user);

  @Query("SELECT p.user FROM ProfileEntity p WHERE p.lastSeen < :lastSeen AND p.status = 'ACTIVE'")
  Stream<Long> getUsersToMarkIdle(Timestamp lastSeen);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.location = :location, p.locationType = :locationType WHERE p.id = :user")
  void setLocation(Long user, Long location, LocationType locationType);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.blackboard = :blackboard WHERE p.id = :user")
  void setBlackboard(Long user, String blackboard);
}
