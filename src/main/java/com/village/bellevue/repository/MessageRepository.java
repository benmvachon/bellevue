package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.MessageEntity;
import com.village.bellevue.entity.UserProfileEntity;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

  @Query("SELECT DISTINCT(m.sender) FROM MessageEntity m WHERE m.receiver = :user ORDER BY m.created DESC")
  Page<UserProfileEntity> findThreads(@Param("user") Long user, Pageable pageable);

  @Query("SELECT DISTINCT(m.sender) FROM MessageEntity m WHERE m.receiver = :user AND m.read = false ORDER BY m.created DESC")
  Page<UserProfileEntity> findUnreadThreads(@Param("user") Long user, Pageable pageable);

  @Query("SELECT m FROM MessageEntity m WHERE (m.receiver = :user AND m.sender.id = :friend) OR (m.receiver = :friend AND m.sender.id = :user) ORDER BY m.created DESC")
  Page<MessageEntity> findAll(@Param("user") Long user, @Param("friend") Long friend, Pageable pageable);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver = :user AND m.sender.id = :friend AND m.read = false")
  void markAllAsRead(@Param("user") Long user, @Param("friend") Long friend);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver = :user AND m.id = :id AND m.read = false")
  void markAsRead(@Param("id") Long id);
}
