package com.village.bellevue.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.NotificationEntity;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

  @Query("SELECT n FROM NotificationEntity n WHERE n.notified = :user ORDER BY n.created DESC")
  Page<NotificationEntity> findAll(@Param("user") Long user, Pageable pageable);

  @Query("SELECT COUNT(n) FROM NotificationEntity n WHERE n.notified = :user AND n.read = false ORDER BY n.created DESC")
  Long countUnread(@Param("user") Long user);

  @Modifying
  @Transactional
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.notified = :user AND n.read = false")
  void markAllAsRead(@Param("user") Long user);

  @Modifying
  @Transactional
  @Query("UPDATE NotificationEntity n SET n.read = true WHERE n.id = :id AND n.notified = :user")
  void markAsRead(@Param("id") Long id, @Param("user") Long user);
}
