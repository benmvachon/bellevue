package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.ForumEntity;
import com.village.bellevue.entity.ProfileEntity;

@Repository
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.status = 'active' WHERE p.id = :user")
  void setStatusOnline(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.status = 'offline', p.location = null WHERE p.id = :user")
  void setStatusOffline(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.status = 'idle' WHERE p.id = :user")
  void setStatusIdle(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.location = :location WHERE p.id = :user")
  void setLocation(Long user, ForumEntity location);

  @Modifying
  @Transactional
  @Query("UPDATE ProfileEntity p SET p.blackboard = :blackboard WHERE p.id = :user")
  void setBlackboard(Long user, String blackboard);
}
