package com.village.bellevue.repository;

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
    "SELECT DISTINCT f1 FROM ForumEntity f1 " +
    "LEFT JOIN FriendEntity f2 ON f1.user = f2.friend.id AND f2.status = 'accepted' AND f2.user = :user " +
    "WHERE f1.user IS NULL OR f1.user = :user OR f2.id IS NOT NULL"
  )
  @Transactional(readOnly = true)
  Page<ForumEntity> findAll(@Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT DISTINCT f1 FROM ForumEntity f1 " +
    "LEFT JOIN FriendEntity f2 ON f1.user = f2.friend.id AND f2.status = 'accepted' AND f2.user = :user " +
    "WHERE (f1.user IS NULL OR f1.user = :user OR f2.id IS NOT NULL) " +
    "AND f1.category = :category"
  )
  @Transactional(readOnly = true)
  Page<ForumEntity> findAllByCategory(@Param("user") Long user, @Param("category") String category, Pageable pageable);

  @Query(
    "SELECT DISTINCT f1.category FROM ForumEntity f1 " +
    "LEFT JOIN FriendEntity f2 ON f1.user = f2.friend.id AND f2.status = 'accepted' AND f2.user = :user " +
    "WHERE f1.user IS NULL OR f1.user = :user OR f2.id IS NOT NULL"
  )
  @Transactional(readOnly = true)
  Page<String> findAllCategories(@Param("user") Long user, Pageable pageable);

  @Query(
    "SELECT CASE WHEN COUNT(f1) > 0 THEN true ELSE false END " +
    "FROM ForumEntity f1 " +
    "LEFT JOIN FriendEntity f2 ON f1.user = f2.friend.id AND f2.status = 'accepted' AND f2.user = :user " +
    "WHERE f1.user IS NULL OR f1.id = :forum AND (f1.user = :user OR f2.id IS NOT NULL)"
  )
  @Transactional(readOnly = true)
  Boolean canRead(@Param("forum") Long forum, @Param("user") Long user);
}
