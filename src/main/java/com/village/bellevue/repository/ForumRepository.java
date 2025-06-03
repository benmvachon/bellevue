package com.village.bellevue.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.village.bellevue.entity.ForumEntity;

@Repository
public interface ForumRepository extends JpaRepository<ForumEntity, Long> {

  @Query(
    "SELECT f.id FROM ForumEntity f " +
    "WHERE f.user IS NULL OR f.user = :user " +
    "OR (EXISTS (SELECT 1 FROM FriendEntity ff WHERE ff.user = :user AND ff.friend.id = f.user AND ff.status = 'ACCEPTED') " +
    "AND EXISTS (SELECT 1 FROM ForumSecurityEntity fs WHERE fs.forum = f.id AND fs.user.id = :user))"
  )
  @Transactional(readOnly = true)
  List<Long> findAll(@Param("user") Long user);

  @Query(
    "SELECT DISTINCT f FROM ForumEntity f " +
    "LEFT JOIN f.tags t " +
    "LEFT JOIN PostEntity p ON p.forum.id = f.id AND p.parent IS NULL " +
      "AND (p.user.id = :user OR EXISTS ( " +
        "SELECT 1 FROM FriendEntity pf WHERE pf.user = :user AND pf.friend.id = p.user.id AND pf.status = 'ACCEPTED')) " +
    "WHERE (f.user IS NULL OR f.user = :user " +
      "OR (EXISTS (SELECT 1 FROM FriendEntity ff WHERE ff.user = :user AND ff.friend.id = f.user AND ff.status = 'ACCEPTED') " +
      "AND EXISTS (SELECT 1 FROM ForumSecurityEntity fs WHERE fs.forum = f.id AND fs.user.id = :user))) " +
    "AND (:query IS NULL OR LOWER(t) LIKE LOWER(CONCAT(:query, '%')) " +
      "OR LOWER(f.name) LIKE LOWER(CONCAT(:query, '%')) " +
      "OR LOWER(f.description) LIKE LOWER(CONCAT(:query, '%'))) " +
    "AND (:unreadOnly = false OR EXISTS ( " +
      "SELECT 1 FROM AggregateRatingEntity a WHERE a.post = p.id AND a.user = :user AND a.read = false)) " +
    "GROUP BY f.id " +
    "HAVING (:unreadOnly = false OR COUNT(p.id) > 0) " +
    "ORDER BY MAX(p.created) DESC, f.id ASC"
  )
  @Transactional(readOnly = true)
  Page<ForumEntity> searchForums(
    @Param("user") Long user,
    @Param("query") String query,
    @Param("unreadOnly") boolean unreadOnly,
    Pageable pageable
  );

  @Query(
    "SELECT CASE WHEN COUNT(f1) > 0 THEN true ELSE false END " +
    "FROM ForumEntity f1 " +
    "LEFT JOIN FriendEntity f2 ON f1.user = f2.friend.id AND f2.status = 'ACCEPTED' AND f2.user = :user " +
    "WHERE f1.id = :forum AND (f1.user IS NULL OR f1.user = :user OR (f2.id IS NOT NULL " +
    "AND EXISTS (SELECT 1 FROM ForumSecurityEntity fs WHERE fs.forum = f1.id AND fs.user.id = :user)))"
  )
  @Transactional(readOnly = true)
  Boolean canRead(@Param("forum") Long forum, @Param("user") Long user);

  @Query(
    "SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END " +
    "FROM ForumEntity f " +
    "WHERE f.id = :forum AND f.user = :user"
  )
  @Transactional(readOnly = true)
  Boolean canUpdate(@Param("forum") Long forum, @Param("user") Long user);

  @Query(
    "SELECT COUNT(DISTINCT(p.id)) " +
    "FROM PostEntity p " +
    "LEFT JOIN AggregateRatingEntity a ON p.id = a.post " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "JOIN ForumEntity fe ON p.forum.id = fe.id " +
    "WHERE p.parent IS NULL AND p.forum.id = :forum AND a.read = false AND a.user = :user " +
    "AND (f.status = 'ACCEPTED' OR p.user.id = :user) " +
    "AND (fe.user IS NULL OR fe.user = :user " +
      "OR (EXISTS (SELECT 1 FROM FriendEntity ff WHERE ff.user = :user AND ff.friend.id = fe.user AND ff.status = 'ACCEPTED') " +
      "AND EXISTS (SELECT 1 FROM ForumSecurityEntity fs WHERE fs.forum = fe.id AND fs.user.id = :user)))"
  )
  @Transactional(readOnly = true)
  Long getUnreadCount(@Param("forum") Long forum, @Param("user") Long user);

  @Query(
    "SELECT COUNT(DISTINCT(p.id)) " +
    "FROM PostEntity p " +
    "LEFT JOIN AggregateRatingEntity a ON p.id = a.post " +
    "LEFT JOIN FriendEntity f ON p.user.id = f.friend.id AND f.user = :user " +
    "JOIN ForumEntity fe ON p.forum.id = fe.id " +
    "WHERE p.parent IS NULL AND a.read = false AND a.user = :user " +
    "AND (f.status = 'ACCEPTED' OR p.user.id = :user) " +
    "AND (fe.user IS NULL OR fe.user = :user " +
      "OR (EXISTS (SELECT 1 FROM FriendEntity ff WHERE ff.user = :user AND ff.friend.id = fe.user AND ff.status = 'ACCEPTED') " +
      "AND EXISTS (SELECT 1 FROM ForumSecurityEntity fs WHERE fs.forum = fe.id AND fs.user.id = :user)))"
  )
  @Transactional(readOnly = true)
  Long getUnreadCount(@Param("user") Long user);
}
