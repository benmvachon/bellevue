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
import com.village.bellevue.entity.NotificationEntity.NotificationType;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  @Query("SELECT n.id FROM NotificationEntity n WHERE n.notified = :user AND n.type = :type AND n.entity = :entity")
  @Transactional(readOnly = true)
  Long findId(@Param("user") Long user, @Param("type") NotificationType type, @Param("entity") Long entity);

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user AND n.created < :cursor ORDER BY n.created DESC LIMIT :limit")
  @Transactional(readOnly = true)
  List<NotificationEntity> findAll(@Param("user") Long user, @Param("cursor") Timestamp cursor, @Param("limit") Long limit);

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user AND n.created >= :cursor ORDER BY n.created DESC")
  @Transactional(readOnly = true)
  List<NotificationEntity> refresh(@Param("user") Long user, @Param("cursor") Timestamp cursor);

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user AND n.id = :id")
  @Transactional(readOnly = true)
  NotificationEntity findNotification(@Param("user") Long user, @Param("id") Long id);

  @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.notified = :user AND n.read = false")
  @Transactional(readOnly = true)
  Long countUnread(@Param("user") Long user);

  @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.notified = :user")
  @Transactional(readOnly = true)
  Long countTotal(@Param("user") Long user);

  @Modifying
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.notified = :user AND n.read = false")
  @Transactional
  int markAllAsRead(@Param("user") Long user);

  @Modifying
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.id = :id AND n.notified = :user")
  @Transactional
  int markAsRead(@Param("id") Long id, @Param("user") Long user);

  @Modifying
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.entity = :post AND n.notified = :user AND n.type = 'POST'")
  @Transactional
  int markPostAsRead(@Param("post") Long post, @Param("user") Long user);
}
