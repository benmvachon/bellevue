package com.village.bellevue.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {

  UserEntity findByUsername(String username);

  @Modifying
  @Query("UPDATE UserEntity u SET u.name = :name WHERE u.id = :user")
  @Transactional
  int changeName(@Param("user") Long user, @Param("name") String name);

  @Modifying
  @Query("UPDATE UserEntity u SET u.password = :password WHERE u.id = :user")
  @Transactional
  int changePassword(@Param("user") Long user, @Param("password") String password);
}
