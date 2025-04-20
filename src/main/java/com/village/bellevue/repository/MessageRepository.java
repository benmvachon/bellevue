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

  @Query(
    "SELECT DISTINCT(u) FROM UserProfileEntity u " +
    "JOIN MessageEntity m ON (" +
    "(m.receiver.id = :user AND m.sender.id = u.user) OR " +
    "(m.sender.id = :user AND m.receiver.id = u.user)" +
    ")"
  )
  Page<UserProfileEntity> findThreads(@Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT m.sender FROM MessageEntity m " +
    "WHERE m.id IN (" +
    "SELECT MAX(m2.id) FROM MessageEntity m2 " +
    "WHERE m2.receiver.id = :user AND m2.read = false " +
    "GROUP BY m2.sender.id" +
    ") " +
    "ORDER BY m.created DESC"
  )
  Page<UserProfileEntity> findUnreadThreads(@Param("user") Long user, Pageable pageable);

  @Query("SELECT COUNT(DISTINCT(m.sender)) FROM MessageEntity m WHERE m.receiver.id = :user AND m.read = false")
  Long countUnreadThreads(@Param("user") Long user);

  @Query("SELECT m FROM MessageEntity m WHERE (m.receiver.id = :user AND m.sender.id = :friend) OR (m.receiver.id = :friend AND m.sender.id = :user) ORDER BY m.created ASC")
  Page<MessageEntity> findAll(@Param("user") Long user, @Param("friend") Long friend, Pageable pageable);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver.id = :user AND m.sender.id = :friend AND m.read = false")
  void markAllAsRead(@Param("user") Long user, @Param("friend") Long friend);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver.id = :user AND m.id = :id AND m.read = false")
  void markAsRead(@Param("user") Long user, @Param("id") Long id);
}
