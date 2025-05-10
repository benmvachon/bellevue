package com.village.bellevue.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user AND n.created < :cursor ORDER BY n.created DESC LIMIT :limit")
  List<NotificationEntity> findAll(@Param("user") Long user, @Param("cursor") Timestamp cursor, @Param("limit") Long limit);

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user AND n.created >= :cursor ORDER BY n.created DESC")
  List<NotificationEntity> refresh(@Param("user") Long user, @Param("cursor") Timestamp cursor);

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user AND n.id = :id")
  NotificationEntity findNotification(@Param("user") Long user, @Param("id") Long id);

  @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.notified = :user AND n.read = false")
  Long countUnread(@Param("user") Long user);

  @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.notified = :user")
  Long countTotal(@Param("user") Long user);

  @Modifying
  @Transactional
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.notified = :user AND n.read = false")
  void markAllAsRead(@Param("user") Long user);

  @Modifying
  @Transactional
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.id = :id AND n.notified = :user")
  void markAsRead(@Param("id") Long id, @Param("user") Long user);
}
