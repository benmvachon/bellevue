package com.village.bellevue.repository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.MessageEntity;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

  @Query("SELECT m FROM MessageEntity m " +
    "LEFT JOIN FriendEntity f ON (m.receiver.id = f.friend.id OR m.sender.id = f.friend.id) AND f.status = 'accepted' AND f.user = :user " +
    "WHERE (m.receiver.id = :user OR m.sender.id = :user) " +
    "AND m.id = :id")
  public MessageEntity find(@Param("user") Long user, @Param("id") Long id);

  @Query(value = """
    SELECT m.*
    FROM message m
    JOIN (
      SELECT
        CASE
          WHEN sender = :user THEN receiver
          ELSE sender
        END AS other_user,
        MAX(created) AS last_message_time
      FROM message
      WHERE sender = :user OR receiver = :user
      GROUP BY other_user
    ) conv ON (
      ((m.sender = :user AND m.receiver = conv.other_user) OR
      (m.receiver = :user AND m.sender = conv.other_user))
      AND m.created = conv.last_message_time
    )
    WHERE m.created < :cursor
    ORDER BY m.created DESC
    LIMIT :limit
    """, nativeQuery = true)
  List<MessageEntity> findThreads(@Param("user") Long user, @Param("cursor") Timestamp cursor, @Param("limit") Long limit);

  @Query(value = """
    SELECT m.*
    FROM message m
    JOIN (
      SELECT
        CASE
          WHEN sender = :user THEN receiver
          ELSE sender
        END AS other_user,
        MAX(created) AS last_message_time
      FROM message
      WHERE sender = :user OR receiver = :user
      GROUP BY other_user
    ) conv ON (
      ((m.sender = :user AND m.receiver = conv.other_user) OR
      (m.receiver = :user AND m.sender = conv.other_user))
      AND m.created = conv.last_message_time
    )
    WHERE m.created >= :cursor
    ORDER BY m.created DESC
    """, nativeQuery = true)
  List<MessageEntity> refreshThreads(@Param("user") Long user, @Param("cursor") Timestamp cursor);

  @Query("SELECT COUNT(DISTINCT(m.sender)) FROM MessageEntity m WHERE m.receiver.id = :user AND m.read = false")
  Long countUnreadThreads(@Param("user") Long user);

  @Query(value = """
    SELECT COUNT(*) FROM (
      SELECT
        CASE
          WHEN sender = :user THEN receiver
          ELSE sender
        END AS other_user
      FROM message
      WHERE sender = :user OR receiver = :user
      GROUP BY other_user
    ) AS conversations
    """, nativeQuery = true)
  Long countThreads(@Param("user") Long user);

  @Query("SELECT m FROM MessageEntity m " +
    "WHERE ((m.receiver.id = :user AND m.sender.id = :friend) " +
    "OR (m.receiver.id = :friend AND m.sender.id = :user)) " +
    "AND m.created < :cursor " +
    "ORDER BY m.created DESC " +
    "LIMIT :limit")
  List<MessageEntity> findAll(@Param("user") Long user, @Param("friend") Long friend, @Param("cursor") Timestamp cursor, @Param("limit") Long limit);

  @Query("SELECT COUNT(DISTINCT(m)) FROM MessageEntity m " +
    "WHERE (m.receiver.id = :user AND m.sender.id = :friend) " +
    "OR (m.receiver.id = :friend AND m.sender.id = :user)")
  Long countAll(@Param("user") Long user, @Param("friend") Long friend);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver.id = :user AND m.read = false")
  void markAllAsRead(@Param("user") Long user);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver.id = :user AND m.sender.id = :friend AND m.read = false")
  void markThreadAsRead(@Param("user") Long user, @Param("friend") Long friend);

  @Modifying
  @Transactional
  @Query("UPDATE MessageEntity m SET m.read = true WHERE m.receiver.id = :user AND m.id = :id AND m.read = false")
  void markAsRead(@Param("user") Long user, @Param("id") Long id);
}
