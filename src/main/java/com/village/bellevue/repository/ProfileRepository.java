package com.village.bellevue.repository;

import java.sql.Timestamp;
import java.util.List;

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
  @Query("UPDATE ProfileEntity p SET p.status = 'ACTIVE' WHERE p.id = :user AND p.status != 'ACTIVE'")
  @Transactional
  int setStatusOnline(Long user);

  @Modifying
  @Query("UPDATE ProfileEntity p SET p.status = 'OFFLINE', p.location = null, p.locationType = null WHERE p.id = :user AND p.status != 'OFFLINE'")
  @Transactional
  int setStatusOffline(Long user);

  @Modifying
  @Query("UPDATE ProfileEntity p SET p.status = 'IDLE' WHERE p.id = :user AND p.status != 'IDLE'")
  @Transactional
  int setStatusIdle(Long user);

  @Modifying
  @Query("UPDATE ProfileEntity p SET p.lastSeen = :lastSeen WHERE p.id = :user")
  @Transactional
  int setLastSeen(Long user, Timestamp lastSeen);

  @Query("SELECT p.lastSeen FROM ProfileEntity p WHERE p.id = :user")
  @Transactional(readOnly = true)
  Timestamp getLastSeen(Long user);

  @Query("SELECT p.user FROM ProfileEntity p WHERE p.lastSeen < :lastSeen AND p.status = 'ACTIVE'")
  @Transactional(readOnly = true)
  List<Long> getUsersToMarkIdle(Timestamp lastSeen);

  @Modifying
  @Query("UPDATE ProfileEntity p SET p.location = :location, p.locationType = :locationType WHERE p.id = :user")
  @Transactional(readOnly = true)
  int setLocation(Long user, Long location, LocationType locationType);

  @Modifying
  @Query("UPDATE ProfileEntity p SET p.blackboard = :blackboard WHERE p.id = :user")
  @Transactional(readOnly = true)
  int setBlackboard(Long user, String blackboard);
}
