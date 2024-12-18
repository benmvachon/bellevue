package com.village.bellevue.repository;

import com.village.bellevue.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  UserEntity findByUsername(String username);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.status = 'online' WHERE u.id = :user")
  void setUserStatusOnline(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.status = 'offline' WHERE u.id = :user")
  void setUserStatusOffline(Long user);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.name = :name WHERE u.id = :user")
  void changeName(@Param("user") Long user, @Param("name") String name);

  @Modifying
  @Transactional
  @Query("UPDATE UserEntity u SET u.password = :password WHERE u.id = :user")
  void changePassword(@Param("user") Long user, @Param("password") String password);
}
